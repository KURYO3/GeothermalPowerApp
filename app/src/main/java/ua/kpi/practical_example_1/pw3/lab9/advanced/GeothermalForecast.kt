package ua.kpi.practical_example_1.pw3.lab9.advanced

// Прогноз генерації
data class GeothermalForecast(
    val id: Int,
    val day: String, // Поле day містить назву дня, зміни або свердловини
    val predictedPower: Double,
    val wellTemperatureC: Double = 195.0,
    val notes: String? = null
)
