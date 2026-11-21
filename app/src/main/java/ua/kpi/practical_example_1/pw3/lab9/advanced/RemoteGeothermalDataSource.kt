package ua.kpi.practical_example_1.pw3.lab9.advanced

import kotlinx.coroutines.delay
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Звертається до REST API через Retrofit і використовує демонстраційні дані, якщо запит завершується винятком
class RemoteGeothermalDataSource {

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:5156/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(GeothermalApiService::class.java)

    private val remoteSimulatedStorage = mutableListOf(
        GeothermalForecast(1, "День 1 (Свердловина №1)", 14.5, 198.0, "Стабільний тиск пари"),
        GeothermalForecast(2, "День 2 (Свердловина №2)", 18.2, 215.0, "Підвищена ентальпія"),
        GeothermalForecast(3, "День 3 (Бінарний цикл)", 12.0, 175.0, "Планове охолодження")
    )

    suspend fun fetchAll(): Result<List<GeothermalForecast>> = try {
        Result.success(service.getForecasts())
    } catch (e: Exception) {
        // Імітує мережеву затримку під час читання демонстраційних даних
        delay(350)
        Result.success(remoteSimulatedStorage.toList())
    }

    suspend fun addForecast(forecast: GeothermalForecast): Result<GeothermalForecast> = try {
        val response = service.addForecast(forecast)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("Не вдалося додати прогноз. HTTP ${response.code()}"))
        }
    } catch (e: Exception) {
        delay(200)
        remoteSimulatedStorage.add(forecast)
        Result.success(forecast)
    }

    suspend fun updateForecast(id: Int, forecast: GeothermalForecast): Result<GeothermalForecast> = try {
        val response = service.updateForecast(id, forecast)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("Не вдалося оновити прогноз. HTTP ${response.code()}"))
        }
    } catch (e: Exception) {
        delay(200)
        remoteSimulatedStorage.replaceAll { if (it.id == id) forecast else it }
        Result.success(forecast)
    }

    suspend fun deleteForecast(id: Int): Result<Boolean> = try {
        val response = service.deleteForecast(id)
        if (response.isSuccessful) {
            Result.success(true)
        } else {
            Result.failure(Exception("Не вдалося видалити прогноз. HTTP ${response.code()}"))
        }
    } catch (e: Exception) {
        delay(200)
        val removed = remoteSimulatedStorage.removeIf { it.id == id }
        Result.success(removed)
    }
}
