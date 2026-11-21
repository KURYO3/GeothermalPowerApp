package ua.kpi.practical_example_1.pw3.lab9.advanced

// Кешує прогнози на 5 хвилин і обчислює середні показники
class GeothermalRepository(
    private val local: LocalGeothermalDataSource,
    private val remote: RemoteGeothermalDataSource
) {
    // Час життя кешу в мілісекундах
    private val cacheTTL = 5 * 60 * 1000L
    private var lastFetchTime = 0L

    suspend fun getAll(forceRefresh: Boolean = false): Result<List<GeothermalForecast>> {
        val currentTime = System.currentTimeMillis()
        val localData = local.getAll()

        if (!forceRefresh && localData.isNotEmpty() && (currentTime - lastFetchTime) < cacheTTL) {
            return Result.success(localData)
        }

        return try {
            val forecasts = remote.fetchAll().getOrThrow()
            local.clear()
            forecasts.forEach { local.insert(it) }
            lastFetchTime = currentTime

            Result.success(local.getAll())
        } catch (e: Exception) {
            if (localData.isNotEmpty()) Result.success(localData)
            else Result.failure(e)
        }
    }

    suspend fun add(forecast: GeothermalForecast): Result<GeothermalForecast> = try {
        val added = remote.addForecast(forecast).getOrNull()
            ?: throw Exception("Не вдалося додати прогноз")
        local.insert(added)
        Result.success(added)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun update(forecast: GeothermalForecast): Result<GeothermalForecast> = try {
        val updated = remote.updateForecast(forecast.id, forecast).getOrNull()
            ?: throw Exception("Не вдалося оновити прогноз")
        local.update(updated)
        Result.success(updated)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun delete(id: Int): Result<Boolean> = try {
        val success = remote.deleteForecast(id).isSuccess
        if (success) local.delete(id)
        Result.success(success)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun filterByDay(day: String): List<GeothermalForecast> = local.filterByDay(day)

    fun sortByPower(descending: Boolean): List<GeothermalForecast> = local.sortByPower(descending)

    fun calculateAveragePower(items: List<GeothermalForecast>): Double {
        if (items.isEmpty()) return 0.0
        return items.map { it.predictedPower }.average()
    }

    fun calculateAverageTemperature(items: List<GeothermalForecast>): Double {
        if (items.isEmpty()) return 0.0
        return items.map { it.wellTemperatureC }.average()
    }
}
