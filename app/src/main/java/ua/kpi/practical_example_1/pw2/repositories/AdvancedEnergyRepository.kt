package ua.kpi.practical_example_1.pw2.repositories

import ua.kpi.practical_example_1.pw2.dao.AdvancedEnergyDao
import ua.kpi.practical_example_1.pw2.data.GeothermalPower
import ua.kpi.practical_example_1.pw2.data.GeothermalStation
import ua.kpi.practical_example_1.pw2.data.GeothermalTemperature

class AdvancedEnergyRepository(private val dao: AdvancedEnergyDao) {

    // Внутрішній кеш списку станцій для зменшення кількості звернень до БД
    private var cachedStations: List<GeothermalStation>? = null

    // Додає станцію разом із її показниками та очищує кеш
    suspend fun insertStationWithData(
        station: GeothermalStation,
        powers: List<GeothermalPower>,
        temperatures: List<GeothermalTemperature>
    ) {
        dao.insertStationWithPowerAndTemperature(station, powers, temperatures)
        cachedStations = null // Інвалідуємо кеш після оновлення БД
    }

    // Видаляє станцію та очищує кеш
    suspend fun deleteStation(station: GeothermalStation) {
        dao.deleteStation(station)
        cachedStations = null
    }

    // Отримує станції з урахуванням фільтра пошуку та кешування
    suspend fun getStations(searchQuery: String = ""): List<GeothermalStation> {
        return if (searchQuery.isBlank()) {
            cachedStations ?: dao.getAllStations().also { cachedStations = it }
        } else {
            dao.searchStationsByName(searchQuery.trim())
        }
    }

    // Отримує відсортовані показники потужності для обраної станції
    suspend fun getPowersForStation(stationId: Int, descending: Boolean): List<GeothermalPower> {
        return if (descending) {
            dao.getPowersForStationDesc(stationId)
        } else {
            dao.getPowersForStationAsc(stationId)
        }
    }

    // Отримує відсортовані показники температури для обраної станції
    suspend fun getTemperaturesForStation(stationId: Int, descending: Boolean): List<GeothermalTemperature> {
        return if (descending) {
            dao.getTemperaturesForStationDesc(stationId)
        } else {
            dao.getTemperaturesForStationAsc(stationId)
        }
    }

    // Повертає середню потужність станції
    suspend fun getAveragePower(stationId: Int): Double {
        return dao.getAveragePower(stationId) ?: 0.0
    }

    // Обчислює прогнозовану потужність станції на основі середньої потужності та середньої температури
    fun calculateForecastPower(
        avgPower: Double,
        temperatures: List<GeothermalTemperature>
    ): Double {
        if (avgPower <= 0.0) return 0.0
        val avgTemp = if (temperatures.isNotEmpty()) {
            temperatures.map { it.value }.average()
        } else {
            180.0
        }
        // Якщо середня температура перевищує 180 коефіцієнт теплової ефективності зростає
        val thermalFactor = (avgTemp / 180.0).coerceIn(0.75, 1.35)
        return avgPower * thermalFactor
    }
}