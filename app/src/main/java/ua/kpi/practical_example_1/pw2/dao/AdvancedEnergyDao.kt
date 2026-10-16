package ua.kpi.practical_example_1.pw2.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import ua.kpi.practical_example_1.pw2.data.GeothermalPower
import ua.kpi.practical_example_1.pw2.data.GeothermalStation
import ua.kpi.practical_example_1.pw2.data.GeothermalTemperature

// Інтерфейс Data Access Object (DAO) для роботи з багатотабличною базою даних
// Усі методи є suspend-функціями для асинхронного виконання в корутинах
@Dao
interface AdvancedEnergyDao {

    // Робота зі станціями

    // Вставляє нову станцію та повертає її згенерований ID
    @Insert
    suspend fun insertStation(station: GeothermalStation): Long

    // Видаляє станцію
    @Delete
    suspend fun deleteStation(station: GeothermalStation)

    // Отримує всі станції, впорядковані за алфавітом (ASC)
    @Query("SELECT * FROM geothermal_station_table ORDER BY name ASC")
    suspend fun getAllStations(): List<GeothermalStation>

    // Фільтрує станції за введеним пошуковим запитом у назві
    @Query("SELECT * FROM geothermal_station_table WHERE name LIKE '%' || :searchQuery || '%' ORDER BY name ASC")
    suspend fun searchStationsByName(searchQuery: String): List<GeothermalStation>

    // Робота з потужністю
    @Insert
    suspend fun insertPower(power: GeothermalPower)

    // Отримує список показників потужності для заданої станції за спаданням (DESC)
    @Query("SELECT * FROM geothermal_power_table WHERE stationId = :stationId ORDER BY value DESC")
    suspend fun getPowersForStationDesc(stationId: Int): List<GeothermalPower>

    // Отримує список показників потужності для заданої станції за зростанням (ASC)
    @Query("SELECT * FROM geothermal_power_table WHERE stationId = :stationId ORDER BY value ASC")
    suspend fun getPowersForStationAsc(stationId: Int): List<GeothermalPower>

    // Розраховує середню потужність для заданої станції
    @Query("SELECT AVG(value) FROM geothermal_power_table WHERE stationId = :stationId")
    suspend fun getAveragePower(stationId: Int): Double?

    // Робота з температурою
    @Insert
    suspend fun insertTemperature(temperature: GeothermalTemperature)

    // Отримує показники температур для заданої станції за спаданням (DESC)
    @Query("SELECT * FROM geothermal_temperature_table WHERE stationId = :stationId ORDER BY value DESC")
    suspend fun getTemperaturesForStationDesc(stationId: Int): List<GeothermalTemperature>

    // Отримує показники температур для заданої станції за зростанням (ASC)
    @Query("SELECT * FROM geothermal_temperature_table WHERE stationId = :stationId ORDER BY value ASC")
    suspend fun getTemperaturesForStationAsc(stationId: Int): List<GeothermalTemperature>

    // Виконує вставку нової станції разом із її списками потужності та температури
    @Transaction
    suspend fun insertStationWithPowerAndTemperature(
        station: GeothermalStation,
        powers: List<GeothermalPower>,
        temperatures: List<GeothermalTemperature>
    ) {
        val stationId = insertStation(station).toInt()
        powers.forEach { insertPower(it.copy(stationId = stationId)) }
        temperatures.forEach { insertTemperature(it.copy(stationId = stationId)) }
    }
}