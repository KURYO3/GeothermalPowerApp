package ua.kpi.practical_example_1.pw2.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ua.kpi.practical_example_1.pw2.data.GeothermalPower
import ua.kpi.practical_example_1.pw2.data.GeothermalStation
import ua.kpi.practical_example_1.pw2.data.GeothermalTemperature
import ua.kpi.practical_example_1.pw2.repositories.AdvancedEnergyRepository

class AdvancedEnergyViewModel(
    private val repository: AdvancedEnergyRepository
) : ViewModel() {

    // Відфільтрований список геотермальних станцій
    private val _stations = MutableStateFlow<List<GeothermalStation>>(emptyList())
    val stations: StateFlow<List<GeothermalStation>> = _stations.asStateFlow()

    // ID обраної станції для перегляду деталей
    private val _selectedStationId = MutableStateFlow<Int?>(null)
    val selectedStationId: StateFlow<Int?> = _selectedStationId.asStateFlow()

    // Список показників потужності обраної станції
    private val _powers = MutableStateFlow<List<GeothermalPower>>(emptyList())
    val powers: StateFlow<List<GeothermalPower>> = _powers.asStateFlow()

    // Список показників температури обраної станції
    private val _temperatures = MutableStateFlow<List<GeothermalTemperature>>(emptyList())
    val temperatures: StateFlow<List<GeothermalTemperature>> = _temperatures.asStateFlow()

    // Потоки стану для середньої та прогнозованої потужності
    private val _averagePower = MutableStateFlow(0.0)
    val averagePower: StateFlow<Double> = _averagePower.asStateFlow()

    private val _forecastPower = MutableStateFlow(0.0)
    val forecastPower: StateFlow<Double> = _forecastPower.asStateFlow()

    // Стан пошуку та напрямку сортування списку (true = за спаданням)
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortDescending = MutableStateFlow(true)
    val sortDescending: StateFlow<Boolean> = _sortDescending.asStateFlow()

    // Повідомлення про помилку або статус операції
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadStations()
    }

    // Оновлення списку станцій відповідно до фільтра пошуку
    fun loadStations() {
        viewModelScope.launch {
            try {
                _stations.value = repository.getStations(_searchQuery.value)
            } catch (e: Exception) {
                _errorMessage.value = "Помилка читання БД: ${e.localizedMessage}"
            }
        }
    }

    // Оновлення рядка пошуку
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        loadStations()
    }

    // Перемикач сортування
    fun toggleSortOrder() {
        _sortDescending.value = !_sortDescending.value
        _selectedStationId.value?.let { selectStation(it) }
    }

    // Функція для асинхронного підвантаження даних обраної станції
    fun selectStation(id: Int) {
        _selectedStationId.value = id
        viewModelScope.launch {
            try {
                val desc = _sortDescending.value
                val loadedPowers = repository.getPowersForStation(id, desc)
                val loadedTemps = repository.getTemperaturesForStation(id, desc)
                val avg = repository.getAveragePower(id)

                _powers.value = loadedPowers
                _temperatures.value = loadedTemps
                _averagePower.value = avg
                _forecastPower.value = repository.calculateForecastPower(avg, loadedTemps)
            } catch (e: Exception) {
                _errorMessage.value = "Помилка завантаження деталей станції: ${e.localizedMessage}"
            }
        }
    }

    // Перевірка даних та збереження в Room Database
    fun addStationWithData(
        name: String,
        powersList: List<Double>,
        temperaturesList: List<Double>
    ) {
        if (name.isBlank()) {
            _errorMessage.value = "Введіть назву геотермальної станції."
            return
        }
        if (powersList.isEmpty() || temperaturesList.isEmpty()) {
            _errorMessage.value = "Введіть коректні числові значення потужності та температури через кому."
            return
        }

        viewModelScope.launch {
            try {
                _errorMessage.value = null
                val station = GeothermalStation(name = name.trim())
                val powers = powersList.map { GeothermalPower(value = it, stationId = 0) }
                val temps = temperaturesList.map { GeothermalTemperature(value = it, stationId = 0) }

                repository.insertStationWithData(station, powers, temps)
                loadStations()
            } catch (e: Exception) {
                _errorMessage.value = "Помилка збереження у базу даних: ${e.localizedMessage}"
            }
        }
    }

    // Видалення станції з БД
    fun deleteStation(station: GeothermalStation) {
        viewModelScope.launch {
            try {
                repository.deleteStation(station)
                if (_selectedStationId.value == station.id) {
                    _selectedStationId.value = null
                }
                loadStations()
            } catch (e: Exception) {
                _errorMessage.value = "Помилка видалення станції: ${e.localizedMessage}"
            }
        }
    }
}