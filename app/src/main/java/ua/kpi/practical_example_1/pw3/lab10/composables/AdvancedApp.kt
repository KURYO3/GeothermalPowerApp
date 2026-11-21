package ua.kpi.practical_example_1.pw3.lab10.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ua.kpi.practical_example_1.pw3.lab10.advanced.Forecast
import ua.kpi.practical_example_1.pw3.lab10.advanced.ForecastStats
import ua.kpi.practical_example_1.pw3.lab10.advanced.GeothermalViewModel
import ua.kpi.practical_example_1.pw3.lab10.advanced.Station

// Список станцій, прогнози генерації та зведені показники
@Composable
fun AdvancedApp(viewModel: GeothermalViewModel = viewModel()) {
    val stations: List<Station> by viewModel.stations.collectAsState()
    val forecasts: Map<Int, List<Forecast>> by viewModel.forecasts.collectAsState()
    val stats: Map<Int, ForecastStats> by viewModel.stats.collectAsState()
    val error: String? by viewModel.error.collectAsState()
    val loading: Boolean by viewModel.loading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadStations()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "Моніторинг геотермальних станцій",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { viewModel.addSampleStation() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Додати станцію")
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(8.dp)
            )
        }

        error?.let { errorMsg ->
            Text(
                text = errorMsg,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = stations,
                key = { station: Station -> station.id }
            ) { station: Station ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "${station.name}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Розташування: ${station.location}")
                        Text("Встановлена потужність: ${station.capacityKw} кВт")

                        Spacer(modifier = Modifier.height(8.dp))

                        val stationStats: ForecastStats? = stats[station.id]
                        if (stationStats != null) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            Text("Середня потужність: ${stationStats.averagePowerKw} кВт")
                            Text("Середня температура пари: ${stationStats.averageTemperatureC} °C")
                            Text("Середній тиск пари: ${stationStats.averagePressureBar} бар")
                            Text(
                                text = "Коефіцієнт використання потужності (КВП): ${stationStats.efficiency}%",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { viewModel.loadForecasts(station.id) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Завантажити прогнози")
                            }
                            OutlinedButton(
                                onClick = { viewModel.generateForecasts(station.id, 3) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Прогноз\nна 3 дні")
                            }
                        }

                        val stationForecasts: List<Forecast> = forecasts[station.id] ?: emptyList()
                        if (stationForecasts.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                stationForecasts.forEach { f: Forecast ->
                                    Text(
                                        text = "${f.date.take(10)}: ${f.predictedPowerKw} кВт | ${f.temperatureC} °C | ${f.steamPressureBar} бар",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
