package ua.kpi.practical_example_1.pw4.lab14.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ua.kpi.practical_example_1.pw4.lab14.data.GeothermalStation
import kotlin.collections.get

@Composable
fun AdvancedApp() {
    val stations = remember {
        listOf(
            GeothermalStation(
                1,
                "Берегівська станція №1",
                "Геотермальна (бінарна)",
                "Прогнозована потужність 1500 кВт",
                48.20,
                22.64,
                1500.0,
                175
            ),
            GeothermalStation(
                2,
                "Мукачівська станція №2",
                "Геотермальна (паротурбінна)",
                "Прогнозована потужність 2200 кВт",
                48.44,
                22.71,
                2200.0,
                198
            ),
            GeothermalStation(
                3,
                "Ужгородська станція №3",
                "Геотермальна (комбінована)",
                "Прогнозована потужність 1850 кВт",
                48.62,
                22.28,
                1850.0,
                184
            ),
            GeothermalStation(
                4,
                "Хустська станція №4",
                "Геотермальна (бінарна)",
                "Прогнозована потужність 1250 кВт",
                48.17,
                23.29,
                1250.0,
                162
            ),
            GeothermalStation(
                5,
                "Косівська станція №5",
                "Геотермальна (глибинна)",
                "Прогнозована потужність 3100 кВт",
                48.31,
                25.09,
                3100.0,
                215
            )
        )
    }

    var selectedStation by remember { mutableStateOf<GeothermalStation?>(stations[1]) }
    var detailScreenStation by remember { mutableStateOf<GeothermalStation?>(null) }
    var sortDescending by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val displayedStations = remember(stations, sortDescending) {
        if (sortDescending) stations.sortedByDescending { it.forecastPower }
        else stations
    }

    // Екран деталей замінює список і панель вибору до повернення
    if (detailScreenStation != null) {
        val st = detailScreenStation!!
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Деталі станції",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Назва: ${st.name}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Тип: ${st.type}")
                    Text("Опис: ${st.description}")
                    Text("Прогнозована потужність: ${st.forecastPower} кВт")
                    Text("Температура теплоносія: ${st.wellTempC} °C")
                    Text("Координати: ${st.latitude}, ${st.longitude}")
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = { detailScreenStation = null }) {
                Text("Назад")
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Геотермальні станції",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Станцій: ${displayedStations.size}", style = MaterialTheme.typography.bodyMedium)
            OutlinedButton(onClick = { sortDescending = !sortDescending }) {
                Text(if (sortDescending) "За потужністю ↓" else "За номером ↓")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (displayedStations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Станцій не знайдено")
            }
        } else {
            Row(modifier = Modifier.fillMaxSize()) {
                // Список і панель використовують спільний стан вибору
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    itemsIndexed(displayedStations, key = { _, item -> item.id }) { _, station ->
                        val isSelected = station == selectedStation
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable {
                                    selectedStation = if (isSelected) null else station
                                },
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = if (isSelected) {
                                CardDefaults.cardColors(containerColor = Color(0xFFE0F7FA))
                            } else {
                                CardDefaults.cardColors()
                            }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(station.name, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text(station.type, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                Text(station.description, style = MaterialTheme.typography.bodySmall, color = Color.Black)
                                Text(
                                    text = "Температура теплоносія: ${station.wellTempC} °C",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                TextButton(
                                    onClick = { detailScreenStation = station },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Детальніше")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Порядок у панелі відповідає порядку карток у списку
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(Color(0xFFF5F7FA), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFCFD8DC), RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Вибір станції",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    displayedStations.forEachIndexed { index, station ->
                        val isSelected = station == selectedStation
                        Box(
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .fillMaxWidth()
                                .background(
                                    color = if (isSelected) Color(0xFFFFEBEE) else Color.White,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color.Red else Color.LightGray,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    // Індекс у панелі вказує на відповідну картку в LazyColumn
                                    selectedStation = station
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(index)
                                    }
                                }
                                .padding(vertical = 8.dp, horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = station.name,
                                color = if (isSelected) Color.Red else Color.Black,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    selectedStation?.let { selected ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EAF6))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Вибрана станція: ${selected.name}",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1A237E)
                                )
                                Text("Координати: ${selected.latitude}, ${selected.longitude}", style = MaterialTheme.typography.bodySmall, color = Color.Black)
                                Text("Прогнозована потужність: ${selected.forecastPower} кВт", style = MaterialTheme.typography.bodySmall, color = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}
