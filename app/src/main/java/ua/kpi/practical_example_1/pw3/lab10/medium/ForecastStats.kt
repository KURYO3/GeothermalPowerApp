package ua.kpi.practical_example_1.pw3.lab10.medium

// Середні показники прогнозів і коефіцієнт використання потужності
data class ForecastStats(
    val stationId: Int,
    val averagePowerKw: Double,
    val averageTemperatureC: Double,
    val averagePressureBar: Double,
    val efficiency: Double
)
