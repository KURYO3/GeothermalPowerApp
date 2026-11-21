package ua.kpi.practical_example_1.pw3.lab10.advanced

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.random.Random

// Дані геотермальної станції
data class Station(
    val id: Int,
    val name: String,
    val location: String,
    val capacityKw: Double
)

// Прогноз генерації для станції
data class Forecast(
    val id: Int,
    val stationId: Int,
    val date: String,
    val predictedPowerKw: Double,
    val temperatureC: Double,
    val steamPressureBar: Int
)

// Середні показники прогнозів і коефіцієнт використання потужності
data class ForecastStats(
    val stationId: Int,
    val averagePowerKw: Double,
    val averageTemperatureC: Double,
    val averagePressureBar: Double,
    val efficiency: Double
)

// Запити до REST API геотермальних станцій
interface GeothermalApiService {

    @GET("stations")
    suspend fun getStations(): List<Station>

    @GET("stations/{id}")
    suspend fun getStation(@Path("id") id: Int): Station

    @POST("stations")
    suspend fun addStation(@Body station: Station)

    @GET("stations/{id}/forecasts")
    suspend fun getForecasts(@Path("id") stationId: Int): List<Forecast>

    @GET("stations/{id}/forecasts/stats")
    suspend fun getForecastStats(@Path("id") stationId: Int): ForecastStats

    @GET("stations/{id}/forecasts/generate/{days}")
    suspend fun generateForecasts(
        @Path("id") stationId: Int,
        @Path("days") days: Int
    ): List<Forecast>
}

// Клієнт Retrofit із Gson і резервними відповідями в разі винятку під час запиту
object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:5156/"
    private val gson = Gson()

    private val mockStations = mutableListOf(
        Station(1, "Закарпатська геотермальна станція № 1", "Берегово, Закарпатська обл.", 2500.0),
        Station(2, "Мукачівська геотермальна станція", "Мукачево, Закарпатська обл.", 3800.0)
    )

    private val mockForecasts = mutableMapOf<Int, MutableList<Forecast>>(
        1 to mutableListOf(
            Forecast(101, 1, "2026-11-12T00:00:00+02:00", 2150.5, 194.2, 18),
            Forecast(102, 1, "2026-11-13T00:00:00+02:00", 2280.0, 201.5, 20)
        ),
        2 to mutableListOf(
            Forecast(201, 2, "2026-11-12T00:00:00+02:00", 3420.0, 212.0, 24),
            Forecast(202, 2, "2026-11-13T00:00:00+02:00", 3590.8, 218.4, 25)
        )
    )

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
                    val eff = if (station != null && station.capacityKw > 0.0) {
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
                    val days = parts[5].toInt()
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

// Завантажує станції, прогнози та статистику для екрана
class GeothermalViewModel : ViewModel() {

    private val api: GeothermalApiService = RetrofitClient.api

    private val stationsCache: MutableMap<Int, Station> = mutableMapOf()
    private val forecastsCache: MutableMap<Int, List<Forecast>> = mutableMapOf()
    private val statsCache: MutableMap<Int, ForecastStats> = mutableMapOf()

    private val _stations: MutableStateFlow<List<Station>> = MutableStateFlow(emptyList())
    val stations: StateFlow<List<Station>> = _stations.asStateFlow()

    private val _forecasts: MutableStateFlow<Map<Int, List<Forecast>>> =
        MutableStateFlow(emptyMap())
    val forecasts: StateFlow<Map<Int, List<Forecast>>> =
        _forecasts.asStateFlow()

    private val _stats: MutableStateFlow<Map<Int, ForecastStats>> =
        MutableStateFlow(emptyMap())
    val stats: StateFlow<Map<Int, ForecastStats>> =
        _stats.asStateFlow()

    private val _error: MutableStateFlow<String?> = MutableStateFlow(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _loading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private suspend fun <T> retryRequest(times: Int = 3, block: suspend () -> T): T {
        var currentAttempt = 0
        var lastError: Exception? = null
        while (currentAttempt < times) {
            try {
                return block()
            } catch (e: Exception) {
                lastError = e
                currentAttempt++
            }
        }
        throw lastError ?: Exception("Невідома мережева помилка")
    }

    fun loadStations() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val data: List<Station> = retryRequest { api.getStations() }
                _stations.value = data.sortedBy { it.name }
                data.forEach { station -> stationsCache[station.id] = station }
            } catch (e: Exception) {
                if (stationsCache.isNotEmpty()) {
                    _stations.value = stationsCache.values.sortedBy { it.name }
                } else {
                    _error.value = "Не вдалося завантажити станції: ${e.localizedMessage}"
                }
            } finally {
                _loading.value = false
            }
        }
    }

    fun addSampleStation() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val nextId = (_stations.value.maxOfOrNull { it.id } ?: 0) + 1
                val newStation = Station(
                    id = nextId,
                    name = "Косівська геотермальна станція № $nextId",
                    location = "Івано-Франківська обл.",
                    capacityKw = 1800.0
                )
                retryRequest { api.addStation(newStation) }
                loadStations()
            } catch (e: Exception) {
                _error.value = "Не вдалося додати станцію: ${e.localizedMessage}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadForecasts(stationId: Int) {
        viewModelScope.launch {
            try {
                val data: List<Forecast> = retryRequest { api.getForecasts(stationId) }
                val sorted: List<Forecast> = data.sortedBy { it.date }
                val updatedMap = _forecasts.value.toMutableMap()
                updatedMap[stationId] = sorted
                _forecasts.value = updatedMap
                forecastsCache[stationId] = sorted
                loadStats(stationId)
            } catch (e: Exception) {
                val cached = forecastsCache[stationId]
                if (cached != null) {
                    val updatedMap = _forecasts.value.toMutableMap()
                    updatedMap[stationId] = cached
                    _forecasts.value = updatedMap
                } else {
                    _error.value = "Не вдалося завантажити прогнози: ${e.localizedMessage}"
                }
            }
        }
    }

    fun loadStats(stationId: Int) {
        viewModelScope.launch {
            try {
                val data: ForecastStats = retryRequest { api.getForecastStats(stationId) }
                val updatedMap = _stats.value.toMutableMap()
                updatedMap[stationId] = data
                _stats.value = updatedMap
                statsCache[stationId] = data
            } catch (e: Exception) {
                _error.value = "Не вдалося завантажити статистику: ${e.localizedMessage}"
            }
        }
    }

    fun generateForecasts(stationId: Int, days: Int) {
        viewModelScope.launch {
            try {
                val newForecasts: List<Forecast> = retryRequest { api.generateForecasts(stationId, days) }
                val currentList: List<Forecast> = _forecasts.value[stationId] ?: emptyList()
                val combined: List<Forecast> = (currentList + newForecasts)
                    .distinctBy { item -> item.id }
                    .sortedBy { item -> item.date }

                val updatedMap = _forecasts.value.toMutableMap()
                updatedMap[stationId] = combined
                _forecasts.value = updatedMap
                forecastsCache[stationId] = combined
                loadStats(stationId)
            } catch (e: Exception) {
                _error.value = "Не вдалося сформувати прогнози: ${e.localizedMessage}"
            }
        }
    }
}
