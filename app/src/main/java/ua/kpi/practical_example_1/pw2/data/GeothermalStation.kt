package ua.kpi.practical_example_1.pw2.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// Сутність геотермальної станції у базі даних Room
@Entity(tableName = "geothermal_station_table")
data class GeothermalStation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)