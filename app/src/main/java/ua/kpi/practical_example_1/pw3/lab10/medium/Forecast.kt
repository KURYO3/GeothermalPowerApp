package ua.kpi.practical_example_1.pw3.lab10.medium

// Прогноз генерації для станції
data class Forecast(
    val id: Int,
    val stationId: Int,
    val date: String,
    val predictedPowerKw: Double,
    val temperatureC: Double,
    val steamPressureBar: Int
)
