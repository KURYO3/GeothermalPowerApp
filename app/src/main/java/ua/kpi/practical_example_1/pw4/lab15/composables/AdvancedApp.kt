package ua.kpi.practical_example_1.pw4.lab15.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ua.kpi.practical_example_1.pw4.lab15.data.GeothermalStation
import ua.kpi.practical_example_1.pw4.lab15.viewModels.GeothermalStationViewModel

@Composable
fun AdvancedApp(viewModel: GeothermalStationViewModel = viewModel()) {
    var selectedStation by remember { mutableStateOf<GeothermalStation?>(null) }
    val currentStations = viewModel.filteredStations

    // Якщо станція зникає з результатів, скидаємо вибір
    LaunchedEffect(currentStations) {
        if (selectedStation != null && selectedStation !in currentStations) {
            selectedStation = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Пошук і сортування станцій",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = viewModel.searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            label = { Text("Пошук за вихідною назвою") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            listOf("All", "Binary", "FlashSteam", "DrySteam").forEach { type ->
                val isSelected = (type == "All" && viewModel.filterType == null) || viewModel.filterType == type
                Button(
                    onClick = { viewModel.updateFilterType(if (type == "All") null else type) },
                    modifier = Modifier.padding(end = 6.dp),
                    colors = if (isSelected) ButtonDefaults.buttonColors()
                    else ButtonDefaults.outlinedButtonColors()
                ) {
                    Text(stationTypeLabel(type))
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            listOf("All Status", "Active", "Maintenance").forEach { status ->
                val isSelected = (status == "All Status" && viewModel.filterStatus == null) || viewModel.filterStatus == status
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.updateFilterStatus(if (status == "All Status") null else status) },
                    label = { Text(stationStatusLabel(status)) },
                    modifier = Modifier.padding(end = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            listOf("Name", "Type", "Power", "Date").forEach { criteria ->
                val isSelected = viewModel.sortBy == criteria
                Button(
                    onClick = { viewModel.updateSortBy(criteria) },
                    modifier = Modifier.padding(end = 6.dp),
                    colors = if (isSelected) ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ) else ButtonDefaults.buttonColors()
                ) {
                    Text(sortCriterionLabel(criteria))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (currentStations.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Станцій не знайдено",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(currentStations) { station ->
                    val isCurrentSelected = station == selectedStation
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { selectedStation = station },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrentSelected) Color(0xFFE0F7FA)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "${stationNameLabel(station.name)}\nВихідна назва: ${station.name}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(text = "Тип: ${stationTypeLabel(station.type)}", color = Color.DarkGray)
                            Text(text = "Потужність: ${station.power} кВт", color = Color.Black)
                            Text(text = "Температура свердловини: ${station.wellTempC} °C", color = Color.Black)
                            Text(text = "Останнє оновлення: ${station.lastUpdate}", color = Color.Black)
                            Text(text = "Статус: ${stationStatusLabel(station.status)}", fontWeight = FontWeight.Medium, color = Color.Black)
                        }
                    }
                }
            }
        }

        selectedStation?.let { station ->
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Вибрана станція: ${stationNameLabel(station.name)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "Тип: ${stationTypeLabel(station.type)}")
                    Text(text = "Потужність: ${station.power} кВт")
                    Text(text = "Температура свердловини: ${station.wellTempC} °C")
                    Text(text = "Останнє оновлення: ${station.lastUpdate}")
                    Text(text = "Статус: ${stationStatusLabel(station.status)}")
                }
            }
        }
    }
}

// Підписи для інтерфейсу. Пошук і сортування використовують вихідні назви.
private fun stationNameLabel(name: String): String = when (name) {
    "GeoBinaryOne" -> "Бінарна станція №1"
    "GeoFlashMax" -> "Станція «Гео Флеш Макс»"
    "CarpathianDry" -> "Карпатська станція сухої пари"
    "BeregovoGeo" -> "Берегівська геотермальна станція"
    "MukachevoSteam" -> "Мукачівська парова станція"
    "KhustThermal" -> "Хустська геотермальна станція"
    else -> name
}

private fun stationTypeLabel(type: String): String = when (type) {
    "All" -> "Усі типи"
    "Binary" -> "Бінарна"
    "FlashSteam" -> "Миттєве випаровування"
    "DrySteam" -> "Суха пара"
    else -> type
}

private fun stationStatusLabel(status: String): String = when (status) {
    "All Status" -> "Усі статуси"
    "Active" -> "Активна"
    "Maintenance" -> "На обслуговуванні"
    else -> status
}

private fun sortCriterionLabel(criterion: String): String = when (criterion) {
    "Name" -> "За назвою"
    "Type" -> "За типом"
    "Power" -> "За потужністю"
    "Date" -> "За датою"
    else -> criterion
}
