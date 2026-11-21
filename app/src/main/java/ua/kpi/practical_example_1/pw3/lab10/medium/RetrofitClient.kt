package ua.kpi.practical_example_1.pw3.lab10.medium

import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ua.kpi.practical_example_1.pw3.lab10.basic.Station
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.random.Random

// Клієнт Retrofit із Gson для REST API геотермальних станцій
object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:5156/"
    private val gson = Gson()

    private val mockStations = mutableListOf(
        Station(1, "Закарпатська геотермальна станція № 1", "Берегово, Закарпатська обл.", 2500.0),
        Station(2, "Мукачівська геотермальна станція", "Мукачево, Закарпатська обл.", 3800.0)
    )

    private val mockForecasts = mutableMapOf(
        1 to mutableListOf(
            Forecast(101, 1, "2026-11-12T00:00:00+02:00", 2150.5, 194.2, 18),
            Forecast(102, 1, "2026-11-13T00:00:00+02:00", 2280.0, 201.5, 20)
        ),
        2 to mutableListOf(
            Forecast(201, 2, "2026-11-12T00:00:00+02:00", 3420.0, 212.0, 24),
            Forecast(202, 2, "2026-11-13T00:00:00+02:00", 3590.8, 218.4, 25)
        )
    )

    // Підставляє демонстраційні відповіді, якщо мережевий виклик завершується винятком
    private val fallbackApiInterceptor = Interceptor { chain ->
        val request = chain.request()
        try {
            chain.proceed(request)
        } catch (e: Exception) {
            val path = request.url.encodedPath
            val json = when {
                path == "/stations" && request.method == "GET" -> gson.toJson(mockStations)
                path == "/stations" && request.method == "POST" -> {
                    val newId = (mockStations.maxOfOrNull { it.id } ?: 0) + 1
                    val added = Station(newId, "Косівська геотермальна станція № $newId", "Івано-Франківська обл.", 1800.0)
                    mockStations.add(added)
                    mockForecasts[newId] = mutableListOf(
                        Forecast(newId * 100 + 1, newId, "2026-11-12T00:00:00+02:00", 1520.0, 185.0, 16)
                    )
                    gson.toJson(added)
                }
                path.matches(Regex("/stations/\\d+/forecasts")) -> {
                    val id = path.split("/")[2].toInt()
                    gson.toJson(mockForecasts[id] ?: emptyList<Forecast>())
                }
                path.matches(Regex("/stations/\\d+/forecasts/stats")) -> {
                    val id = path.split("/")[2].toInt()
                    val station = mockStations.find { it.id == id }
                    val list = mockForecasts[id] ?: emptyList()
                    val avgPower = if (list.isNotEmpty()) list.map { it.predictedPowerKw }.average() else 0.0
                    val avgTemp = if (list.isNotEmpty()) list.map { it.temperatureC }.average() else 0.0
                    val avgPress = if (list.isNotEmpty()) list.map { it.steamPressureBar }.average() else 0.0
                    val eff = if (station != null && station.capacityKw > 0) {
                        (avgPower / station.capacityKw) * 100.0
                    } else 0.0
                    gson.toJson(
                        ForecastStats(
                            stationId = id,
                            averagePowerKw = (avgPower * 100).roundToInt() / 100.0,
                            averageTemperatureC = (avgTemp * 100).roundToInt() / 100.0,
                            averagePressureBar = (avgPress * 10).roundToInt() / 10.0,
                            efficiency = (eff * 100).roundToInt() / 100.0
                        )
                    )
                }
                path.matches(Regex("/stations/\\d+/forecasts/generate/\\d+")) -> {
                    val parts = path.split("/")
                    val id = parts[2].toInt()
                    val days = parts[4].toInt()
                    val station = mockStations.find { it.id == id }
                    val baseCap = station?.capacityKw ?: 2000.0
                    val generated = (1..days).map { d ->
                        val power = String.format(Locale.US, "%.1f", baseCap * Random.nextDouble(0.65, 0.96)).toDouble()
                        val temp = String.format(Locale.US, "%.1f", Random.nextDouble(180.0, 225.0)).toDouble()
                        val press = Random.nextInt(15, 28)
                        Forecast(
                            id = Random.nextInt(1000, 9999),
                            stationId = id,
                            date = "2026-11-${13 + d}T00:00:00+02:00",
                            predictedPowerKw = power,
                            temperatureC = temp,
                            steamPressureBar = press
                        )
                    }
                    val currentList = mockForecasts.getOrPut(id) { mutableListOf() }
                    currentList.addAll(generated)
                    gson.toJson(generated)
                }
                else -> "[]"
            }

            Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(json.toResponseBody("application/json".toMediaType()))
                .build()
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(fallbackApiInterceptor)
        .build()

    val api: GeothermalApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeothermalApiService::class.java)
    }
}
