package ua.kpi.practical_example_1.pw3.lab12.advanced

// Добовий прогноз генерації. Обсяг електроенергії подано в кВт·год
data class GeothermalForecast(
    val date: String,
    val powerKwh: Int,
    val wellTempC: Int = 204,
    val status: String
)
