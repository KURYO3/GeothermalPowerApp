package ua.kpi.practical_example_1.pw3.lab9.advanced

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Керує списком прогнозів і середніми показниками на екрані
class GeothermalViewModel : ViewModel() {

    private val repository = GeothermalRepository(
        local = LocalGeothermalDataSource(),
        remote = RemoteGeothermalDataSource()
    )

    private val _forecasts = MutableStateFlow<List<GeothermalForecast>>(emptyList())
    val forecasts: StateFlow<List<GeothermalForecast>> = _forecasts.asStateFlow()

    private val _averagePower = MutableStateFlow(0.0)
    val averagePower: StateFlow<Double> = _averagePower.asStateFlow()

    private val _averageTemp = MutableStateFlow(0.0)
    val averageTemp: StateFlow<Double> = _averageTemp.asStateFlow()

    private val _statusMessage = MutableStateFlow("Завантаження прогнозів…")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    init {
        loadForecasts()
    }

    fun loadForecasts(forceRefresh: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.getAll(forceRefresh)
            result.onSuccess { list ->
                updateStateWithAnalytics(list)
                _statusMessage.value = if (forceRefresh) {
                    "Список прогнозів оновлено"
                } else {
                    "Список прогнозів завантажено"
                }
            }.onFailure { e ->
                _statusMessage.value = "Не вдалося завантажити прогнози: ${e.message}"
            }
        }
    }

    fun addForecast(day: String, power: Double, tempC: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val nextId = (_forecasts.value.maxOfOrNull { it.id } ?: 0) + 1
            val item = GeothermalForecast(
                id = nextId,
                day = day,
                predictedPower = power,
                wellTemperatureC = tempC,
                notes = if (tempC >= 200) "Висока ентальпія" else "Нормальний режим"
            )
            repository.add(item).onSuccess {
                loadForecasts(forceRefresh = false)
                _statusMessage.value = "Додано прогноз: $day ($power МВт)"
            }
        }
    }

    fun boostForecastPower(item: GeothermalForecast) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = item.copy(predictedPower = item.predictedPower + 1.5)
            repository.update(updated).onSuccess {
                loadForecasts(forceRefresh = false)
                _statusMessage.value = "Оновлено прогноз №${item.id} (+1.5 МВт)"
            }
        }
    }

    fun deleteForecast(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(id).onSuccess {
                loadForecasts(forceRefresh = false)
                _statusMessage.value = "Видалено прогноз №$id"
            }
        }
    }

    fun filterByDay(day: String) {
        val filtered = repository.filterByDay(day)
        updateStateWithAnalytics(filtered)
    }

    fun sortByPower(descending: Boolean = false) {
        val sorted = repository.sortByPower(descending)
        updateStateWithAnalytics(sorted)
    }

    private fun updateStateWithAnalytics(list: List<GeothermalForecast>) {
        _forecasts.value = list
        _averagePower.value = repository.calculateAveragePower(list)
        _averageTemp.value = repository.calculateAverageTemperature(list)
    }
}
