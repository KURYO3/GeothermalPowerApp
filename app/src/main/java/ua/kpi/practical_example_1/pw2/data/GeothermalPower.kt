package ua.kpi.practical_example_1.pw2.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Сутність для збереження потужності
// Пов'язана з таблицею GeothermalStation через зовнішній ключ stationID
@Entity(
    tableName = "geothermal_power_table",
    foreignKeys = [
        ForeignKey(
            entity = GeothermalStation::class,
            parentColumns = ["id"],
            childColumns = ["stationId"],
            onDelete = ForeignKey.CASCADE // При видаленні станції автоматично видаляються всі її показники
        )
    ],
    indices = [Index("stationId")] // Індексація
)
data class GeothermalPower(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val value: Double, // Потужність
    val stationId: Int // ID станції
)