package ua.kpi.practical_example_1.pw3.lab12.advanced

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException
import ua.kpi.practical_example_1.pw3.lab12.composables.isInternetAvailable

// Завантажує прогноз і веде стан запиту та журналу помилок
class ForecastViewModel(private val context: Context) : ViewModel() {

    var forecast by mutableStateOf<GeothermalForecast?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var retryStatus by mutableStateOf<String?>(null)
        private set

    var errorLogs by mutableStateOf<List<String>>(emptyList())
        private set

    private val maxRetries = 3
    // Затримка між спробами в мілісекундах
    private val retryDelay = 1200L

    init {
        refreshLogs()
    }

    fun fetchForecastWithRetry() {
        if (!isInternetAvailable(context)) {
            val msg = "Немає підключення до інтернету"
            errorMessage = msg
            ErrorHandler.logError(context, msg)
            refreshLogs()
            return
        }

        viewModelScope.launch {
            var attempt = 0
            while (attempt < maxRetries) {
                try {
                    retryStatus = "Спроба ${attempt + 1} із $maxRetries…"
                    val response = RetrofitClient.api.getTodayForecast()
                    forecast = response
                    errorMessage = null
                    retryStatus = "Прогноз отримано. Спроба № ${attempt + 1}"
                    refreshLogs()
                    return@launch
                } catch (e: Throwable) {
                    attempt++
                    errorMessage = ErrorHandler.handleException(context, e)
                    refreshLogs()

                    // Повторюємо запит після HTTP 500, 503 та винятків інших типів
                    if (e is HttpException && (e.code() == 500 || e.code() == 503)) {
                        delay(retryDelay)
                        continue
                    } else if (e !is HttpException) {
                        delay(retryDelay)
                        continue
                    } else {
                        break
                    }
                }
            }
            retryStatus = "Не вдалося отримати прогноз (ліміт спроб: $maxRetries)"
        }
    }

    fun fetchError(code: Int) {
        if (!isInternetAvailable(context)) {
            val msg = "Немає підключення до інтернету"
            errorMessage = msg
            ErrorHandler.logError(context, msg)
            refreshLogs()
            return
        }

        viewModelScope.launch {
            try {
                RetrofitClient.api.getError(code)
            } catch (e: Throwable) {
                errorMessage = ErrorHandler.handleException(context, e)
                retryStatus = "Збій під час перевірки помилки HTTP $code"
                refreshLogs()
            }
        }
    }

    fun clearLogs() {
        ErrorHandler.clearLogFile(context)
        refreshLogs()
    }

    private fun refreshLogs() {
        errorLogs = ErrorHandler.readLogFile(context)
    }
}
