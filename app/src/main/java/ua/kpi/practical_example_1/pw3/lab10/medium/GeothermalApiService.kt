package ua.kpi.practical_example_1.pw3.lab10.medium

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import ua.kpi.practical_example_1.pw3.lab10.basic.Station

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

    // Запит на формування прогнозу на задану кількість днів
    @GET("stations/{id}/forecasts/generate/{days}")
    suspend fun generateForecasts(
        @Path("id") stationId: Int,
        @Path("days") days: Int
    ): List<Forecast>
}
