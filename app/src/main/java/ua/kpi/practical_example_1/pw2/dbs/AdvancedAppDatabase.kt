package ua.kpi.practical_example_1.pw2.dbs

import androidx.room.Database
import androidx.room.RoomDatabase
import ua.kpi.practical_example_1.pw2.dao.AdvancedEnergyDao
import ua.kpi.practical_example_1.pw2.data.GeothermalPower
import ua.kpi.practical_example_1.pw2.data.GeothermalStation
import ua.kpi.practical_example_1.pw2.data.GeothermalTemperature

// Об'єднує таблиці станції, потужності та температури
@Database(
    entities = [
        GeothermalStation::class,
        GeothermalPower::class,
        GeothermalTemperature::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AdvancedAppDatabase : RoomDatabase() {
    // Повертає екземпляр DAO для виконання асинхронних запитів до таблиць
    abstract fun advancedDao(): AdvancedEnergyDao
}