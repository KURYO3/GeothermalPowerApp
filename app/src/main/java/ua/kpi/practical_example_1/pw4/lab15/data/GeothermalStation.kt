package ua.kpi.practical_example_1.pw4.lab15.data

import java.time.LocalDate

// Дані геотермальної станції для пошуку, фільтрації та сортування
data class GeothermalStation(
    val name: String,
    val type: String,
    val power: Double, // Потужність у кВт
    val lastUpdate: LocalDate,
    val status: String,
    val wellTempC: Int // Температура свердловини у °C
)
