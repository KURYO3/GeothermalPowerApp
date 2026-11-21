package ua.kpi.practical_example_1.pw3.lab12.advanced

import retrofit2.http.GET
import retrofit2.http.Path

// Запити для отримання прогнозу на сьогодні та імітації HTTP-помилок
interface ForecastApiService {

    @GET("forecast/today")
    suspend fun getTodayForecast(): GeothermalForecast

    @GET("forecast/error/{code}")
    suspend fun getError(@Path("code") code: Int): Message
}
