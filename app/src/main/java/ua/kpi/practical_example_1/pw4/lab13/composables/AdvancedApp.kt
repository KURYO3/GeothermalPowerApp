package ua.kpi.practical_example_1.pw4.lab13.composables

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlin.random.Random

// Телеметрія геотермальної свердловини
data class GeothermalSensorData(
    val powerKw: Float,
    val steamTempC: Int,
    val pressureBar: Float
)

// Зберігає показники моніторингу й лічильники у стані Compose
class GeothermalFlowViewModel : ViewModel() {

    var powerSensor by mutableStateOf(0f)
        private set
    var steamTemp by mutableStateOf(0)
        private set
    var steamPressure by mutableStateOf(0f)
        private set
    var powerForecast by mutableStateOf(0f)
        private set
    var combinedPower by mutableStateOf(0f)
        private set
    var status by mutableStateOf("Очікування даних зі свердловини…")
        private set
    var retryCount by mutableStateOf(0)
        private set
    var filteredNoiseCount by mutableStateOf(0)
        private set

    @Volatile
    private var simulateError = false

    fun triggerSensorError() {
        simulateError = true
    }

    // Холодний потік імітує показники датчика. map калібрує потужність, filter відсіює шум
    // retryWhen повторно запускає джерело після збою, а shareIn ділить потік між підписниками
    val sensorFlow: SharedFlow<GeothermalSensorData> = flow {
        while (true) {
            delay(1000)
            if (simulateError) {
                simulateError = false
                throw RuntimeException("Втрата зв'язку з датчиком тиску свердловини №1")
            }

            // Імітуємо шум датчика у 20% вимірювань
            val isNoise = Random.nextInt(100) < 20
            val rawPower = if (isNoise) {
                10f + Random.nextFloat() * 12f
            } else {
                50f + Random.nextFloat() * 100f
            }

            val tempC = Random.nextInt(145, 225)
            val pressure = 12f + Random.nextFloat() * 15f
            emit(GeothermalSensorData(rawPower, tempC, pressure))
        }
    }
        .map { data ->
            data.copy(powerKw = data.powerKw * 1.05f)
        }
        .filter { data ->
            val isValid = data.powerKw >= 25f
            if (!isValid) {
                filteredNoiseCount++
                status = "Відсіяно шум датчика (${"%.1f".format(data.powerKw)} кВт < 25 кВт)"
            }
            isValid
        }
        .retryWhen { cause, attempt ->
            if (cause is CancellationException) return@retryWhen false
            retryCount++
            status = "Збій: ${cause.message}. Повторна спроба №$retryCount через 1 с…"
            delay(1000)
            true
        }
        // shareIn запускає спільну корутину у viewModelScope. Останнє значення зберігається, а джерело зупиняється через 5 с без підписників
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            replay = 1
        )

    // Гарячий потік прогнозу додаткової потужності бінарного циклу
    val forecastFlow: SharedFlow<Float> = flow {
        while (true) {
            delay(1500)
            val forecast = 20f + Random.nextFloat() * 60f // 20..80 кВт
            emit(forecast)
        }
    }
        .retryWhen { cause, _ ->
            if (cause is CancellationException) return@retryWhen false
            delay(1000)
            true
        }
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            replay = 1
        )

    // combine використовує останнє значення з кожного потоку після першої пари показників
    val combinedTelemetryFlow: Flow<Pair<GeothermalSensorData, Float>> =
        combine(sensorFlow, forecastFlow) { sensor, forecast ->
            sensor to forecast
        }.onEach { (sensorData, forecastValue) ->
            powerSensor = sensorData.powerKw
            steamTemp = sensorData.steamTempC
            steamPressure = sensorData.pressureBar
            powerForecast = forecastValue
            combinedPower = sensorData.powerKw + forecastValue
            status = "Дані отримано й об'єднано"
        }
}

@Composable
fun AdvancedApp(viewModel: GeothermalFlowViewModel = viewModel()) {
    // LaunchedEffect збирає потік, поки екран перебуває в композиції
    LaunchedEffect(viewModel) {
        viewModel.combinedTelemetryFlow.collect()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "Асинхронний моніторинг геотермальної станції",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Телеметрія свердловини та прогноз для бінарного циклу",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Поточні показники",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "Потужність за даними датчика (коефіцієнт 1,05): ${"%.2f".format(viewModel.powerSensor)} кВт")
                Text(text = "Температура пари: ${viewModel.steamTemp} °C")
                Text(text = "Тиск у паропроводі: ${"%.1f".format(viewModel.steamPressure)} бар")

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text(text = "Прогнозована потужність бінарного циклу: ${"%.2f".format(viewModel.powerForecast)} кВт")

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Сумарна потужність станції: ${"%.2f".format(viewModel.combinedPower)} кВт",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (viewModel.status.startsWith("Збій:"))
                    MaterialTheme.colorScheme.errorContainer
                else
                    MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Стан потоку даних", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = viewModel.status)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Повторних спроб: ${viewModel.retryCount} • Відсіяно показників нижче 25 кВт: ${viewModel.filteredNoiseCount}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.triggerSensorError() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Імітувати збій датчика")
        }
    }
}
