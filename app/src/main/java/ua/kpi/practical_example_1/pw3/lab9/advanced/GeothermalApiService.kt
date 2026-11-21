package ua.kpi.practical_example_1.pw3.lab9.advanced

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// Прогнози генерації через REST API
interface GeothermalApiService {

    @GET("forecasts")
    suspend fun getForecasts(): List<GeothermalForecast>

    @POST("forecasts")
    suspend fun addForecast(@Body forecast: GeothermalForecast): Response<GeothermalForecast>

    @PUT("forecasts/{id}")
    suspend fun updateForecast(
        @Path("id") id: Int,
        @Body forecast: GeothermalForecast
    ): Response<GeothermalForecast>

    @DELETE("forecasts/{id}")
    suspend fun deleteForecast(@Path("id") id: Int): Response<Unit>
}
