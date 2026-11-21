package ua.kpi.practical_example_1.pw4.lab14.data

// Дані геотермальної станції для списку й панелі вибору
data class GeothermalStation(
    val id: Int,
    val name: String,
    val type: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val forecastPower: Double, // Прогнозована потужність у кВт
    val wellTempC: Int // Температура теплоносія у °C
)
