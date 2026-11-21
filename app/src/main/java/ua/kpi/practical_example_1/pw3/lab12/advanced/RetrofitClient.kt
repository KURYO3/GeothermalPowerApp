package ua.kpi.practical_example_1.pw3.lab12.advanced

import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Клієнт Retrofit з імітацією відповідей у разі винятку під час запиту
object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:5156/"
    private val gson = Gson()

    // Лічильник імітованих запитів спільний для всіх завантажень прогнозу
    private var retrySimulationCounter = 0

    private val simulationInterceptor = Interceptor { chain ->
        val request = chain.request()
        try {
            chain.proceed(request)
        } catch (e: Exception) {
            val path = request.url.encodedPath
            when {
                path == "/forecast/today" -> {
                    retrySimulationCounter++
                    // Непарні запити повертають HTTP 503, парні — прогноз
                    if (retrySimulationCounter % 2 == 1) {
                        Response.Builder()
                            .request(request)
                            .protocol(Protocol.HTTP_1_1)
                            .code(503)
                            .message("Service Temporarily Unavailable")
                            .body("""{"msg":"Тимчасовий збій телеметрії свердловини"}""".toResponseBody("application/json".toMediaType()))
                            .build()
                    } else {
                        val forecast = GeothermalForecast(
                            date = "2026-11-12",
                            powerKwh = 18450,
                            wellTempC = 208,
                            status = "Штатний режим, тиск пари стабільний"
                        )
                        Response.Builder()
                            .request(request)
                            .protocol(Protocol.HTTP_1_1)
                            .code(200)
                            .message("OK")
                            .body(gson.toJson(forecast).toResponseBody("application/json".toMediaType()))
                            .build()
                    }
                }

                path.startsWith("/forecast/error/") -> {
                    val code = path.substringAfterLast("/").toIntOrNull() ?: 500
                    Response.Builder()
                        .request(request)
                        .protocol(Protocol.HTTP_1_1)
                        .code(code)
                        .message("Simulated HTTP $code")
                        .body("""{"msg":"Simulated error $code"}""".toResponseBody("application/json".toMediaType()))
                        .build()
                }

                else -> Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(404)
                    .message("Not Found")
                    .body("{}".toResponseBody("application/json".toMediaType()))
                    .build()
            }
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(simulationInterceptor)
        .build()

    val api: ForecastApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ForecastApiService::class.java)
    }
}
