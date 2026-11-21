package ua.kpi.practical_example_1.pw4.lab15.viewModels

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import ua.kpi.practical_example_1.pw4.lab15.data.GeothermalStation
import java.time.LocalDate

// Керує пошуком, фільтрами й сортуванням станцій
class GeothermalStationViewModel : ViewModel() {

    @RequiresApi(Build.VERSION_CODES.O)
    private val _stations = mutableStateListOf(
        GeothermalStation("GeoBinaryOne", "Binary", 1500.0, LocalDate.of(2025, 9, 10), "Active", 168),
        GeothermalStation("GeoFlashMax", "FlashSteam", 2800.0, LocalDate.of(2025, 9, 12), "Active", 215),
        GeothermalStation("CarpathianDry", "DrySteam", 1200.0, LocalDate.of(2025, 9, 11), "Maintenance", 240),
        GeothermalStation("BeregovoGeo", "Binary", 1950.0, LocalDate.of(2025, 9, 9), "Active", 178),
        GeothermalStation("MukachevoSteam", "FlashSteam", 3200.0, LocalDate.of(2025, 9, 14), "Active", 225),
        GeothermalStation("KhustThermal", "Binary", 950.0, LocalDate.of(2025, 9, 8), "Maintenance", 154)
    )

    var searchQuery by mutableStateOf("")

    var sortBy by mutableStateOf("Name")

    // null означає, що фільтр за типом вимкнено
    var filterType by mutableStateOf<String?>(null)

    // null означає, що фільтр за статусом вимкнено
    var filterStatus by mutableStateOf<String?>(null)

    // Compose перечитує список при зміні стану, від якого залежить результат
    val filteredStations: List<GeothermalStation>
        @RequiresApi(Build.VERSION_CODES.O)
        get() {
            var result = _stations.toList()

            filterType?.let { type ->
                result = result.filter { it.type.equals(type, ignoreCase = true) }
            }

            filterStatus?.let { status ->
                result = result.filter { it.status.equals(status, ignoreCase = true) }
            }

            // Пошук за частиною вихідної назви без урахування регістру
            if (searchQuery.isNotEmpty()) {
                result = result.filter { it.name.contains(searchQuery, ignoreCase = true) }
            }

            result = when (sortBy) {
                "Name" -> result.sortedBy { it.name }
                "Type" -> result.sortedBy { it.type }
                "Power" -> result.sortedByDescending { it.power }
                "Date" -> result.sortedByDescending { it.lastUpdate }
                else -> result
            }

            return result
        }

    fun updateSearchQuery(query: String) { searchQuery = query }
    fun updateSortBy(criteria: String) { sortBy = criteria }
    fun updateFilterType(type: String?) { filterType = type }
    fun updateFilterStatus(status: String?) { filterStatus = status }
}
