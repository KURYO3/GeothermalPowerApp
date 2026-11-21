package ua.kpi.practical_example_1.pw4.lab16.data

// Дані енергетичного об'єкта з координатами для списку й мапи
data class EnergyStation(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val type: String = "",
    val description: String = "",
    val power: Int = 0, // Потужність у кВт
    val status: String = ""
)
