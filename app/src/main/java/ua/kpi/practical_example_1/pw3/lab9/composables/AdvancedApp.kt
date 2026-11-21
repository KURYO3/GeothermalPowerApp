package ua.kpi.practical_example_1.pw3.lab9.composables

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ua.kpi.practical_example_1.pw3.lab9.advanced.GeothermalViewModel
import java.util.Locale

// Список прогнозів з фільтром за днем і сортуванням за потужністю
@Composable
fun AdvancedApp(viewModel: GeothermalViewModel = viewModel()) {
    val forecasts by viewModel.forecasts.collectAsState()
    val avgPower by viewModel.averagePower.collectAsState()
    val avgTemp by viewModel.averageTemp.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    var filterDay by remember { mutableStateOf("") }
    var sortDescending by remember { mutableStateOf(false) }

    var newDay by remember { mutableStateOf("") }
    var newPower by remember { mutableStateOf("") }
    var newTemp by remember { mutableStateOf("200") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "Прогнози генерації електроенергії",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = statusMessage,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = String.format(Locale.US, "Сер. потужність:\n%.2f МВт", avgPower),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = String.format(Locale.US, "Сер. температура:\n%.1f °C", avgTemp),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = filterDay,
                onValueChange = {
                    filterDay = it
                    viewModel.filterByDay(it)
                },
                label = { Text("Фільтр за днем або свердловиною") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    sortDescending = !sortDescending
                    viewModel.sortByPower(sortDescending)
                }
            ) {
                Text(if (sortDescending) "Сортувати ↓" else "Сортувати ↑")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = newDay,
                onValueChange = { newDay = it },
                label = { Text("День або свердловина") },
                singleLine = true,
                modifier = Modifier.weight(1.2f)
            )
            Spacer(modifier = Modifier.width(6.dp))
            OutlinedTextField(
                value = newPower,
                onValueChange = { newPower = it },
                label = { Text("МВт") },
                singleLine = true,
                modifier = Modifier.weight(0.7f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.width(6.dp))
            OutlinedTextField(
                value = newTemp,
                onValueChange = { newTemp = it },
                label = { Text("°C") },
                singleLine = true,
                modifier = Modifier.weight(0.7f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val power = newPower.toDoubleOrNull()
                    val temp = newTemp.toDoubleOrNull() ?: 195.0
                    if (newDay.isNotBlank() && power != null && power > 0) {
                        viewModel.addForecast(newDay, power, temp)
                        newDay = ""
                        newPower = ""
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Додати\nпрогноз")
            }

            OutlinedButton(
                onClick = { viewModel.loadForecasts(forceRefresh = true) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Оновити з сервера")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(forecasts, key = { it.id }) { forecast ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${forecast.day}: ${String.format(Locale.US, "%.1f", forecast.predictedPower)} МВт",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Температура свердловини: ${forecast.wellTemperatureC} °C | ${forecast.notes ?: ""}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        TextButton(onClick = { viewModel.boostForecastPower(forecast) }) {
                            Text("+1.5 МВт")
                        }

                        IconButton(onClick = { viewModel.deleteForecast(forecast.id) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Видалити",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}
