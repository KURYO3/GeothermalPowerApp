package ua.kpi.practical_example_1.pw3.lab11.medium

// Дані геотермального об'єкта в реєстрі адміністратора
data class GeothermalStation(
    val Id: Int,
    val Name: String,
    val CapacityMW: Double,
    val WellDepthM: Int = 2450
)
