package ua.kpi.practical_example_1.pw3.lab12.advanced

import android.content.Context
import android.util.Log
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Формує повідомлення про помилки та записує їх у Logcat і локальний журнал
object ErrorHandler {

    private const val LOG_FILE_NAME = "error_log.txt"

    fun logError(context: Context, message: String) {
        try {
            val file = File(context.filesDir, LOG_FILE_NAME)
            val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            FileOutputStream(file, true).bufferedWriter().use { writer ->
                writer.write("[$timestamp] $message\n")
            }
        } catch (e: Exception) {
            Log.e("AdvancedErrorHandler", "Не вдалося записати помилку до журналу: ${e.localizedMessage}")
        }
    }

    // Повертає до шести останніх записів журналу
    fun readLogFile(context: Context): List<String> {
        return try {
            val file = File(context.filesDir, LOG_FILE_NAME)
            if (file.exists()) file.readLines().takeLast(6) else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clearLogFile(context: Context) {
        try {
            val file = File(context.filesDir, LOG_FILE_NAME)
            if (file.exists()) file.writeText("")
        } catch (_: Exception) {}
    }

    fun handleException(context: Context, e: Throwable): String {
        val message = when (e) {
            is HttpException -> {
                when (e.code()) {
                    400 -> "HTTP 400: Некоректний запит параметрів свердловини"
                    404 -> "HTTP 404: Прогноз геотермальної станції не знайдено"
                    500 -> "HTTP 500: Помилка сервера. Спробуйте пізніше."
                    503 -> "HTTP 503: Сервіс телеметрії тимчасово перевантажений"
                    else -> "Помилка сервера (HTTP ${e.code()})"
                }
            }
            else -> "Мережева помилка: ${e.localizedMessage ?: "невідомий збій"}"
        }

        Log.e("AdvancedErrorHandler", message)
        logError(context, message)

        return message
    }
}
