package ua.kpi.practical_example_1.pw3.lab9.advanced

// Зберігає прогнози в пам'яті застосунку
class LocalGeothermalDataSource {
    private val data = mutableListOf<GeothermalForecast>()

    fun getAll(): List<GeothermalForecast> = data.toList()

    fun insert(item: GeothermalForecast) {
        data.add(item)
    }

    fun update(item: GeothermalForecast) {
        data.replaceAll { if (it.id == item.id) item else it }
    }

    fun delete(id: Int) {
        data.removeIf { it.id == id }
    }

    fun findById(id: Int): GeothermalForecast? = data.find { it.id == id }

    fun clear() {
        data.clear()
    }

    // Шукає збіг за частиною назви дня без урахування регістру
    fun filterByDay(day: String): List<GeothermalForecast> =
        data.filter { it.day.contains(day, ignoreCase = true) }

    fun sortByPower(descending: Boolean = false): List<GeothermalForecast> =
        if (descending) data.sortedByDescending { it.predictedPower }
        else data.sortedBy { it.predictedPower }
}
