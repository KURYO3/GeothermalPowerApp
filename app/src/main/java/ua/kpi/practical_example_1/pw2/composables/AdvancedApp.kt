package ua.kpi.practical_example_1.pw2.composables

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room.databaseBuilder
import ua.kpi.practical_example_1.pw2.data.GeothermalStation
import ua.kpi.practical_example_1.pw2.dbs.AdvancedAppDatabase
import ua.kpi.practical_example_1.pw2.repositories.AdvancedEnergyRepository
import ua.kpi.practical_example_1.pw2.viewModels.AdvancedEnergyViewModel
import java.util.Locale

// Головна Composable-функція
// Ініціалізує Room Database, Repository та ViewModel, забезпечує додавання, фільтрацію, сортування та обробку параметрів електростанцій
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun AdvancedApp() {
    val context = LocalContext.current

    // Створення екземпляра локальної бази даних Room
    val db = remember {
        databaseBuilder(
            context.applicationContext,
            AdvancedAppDatabase::class.java,
            "advanced_geothermal_db"
        ).build()
    }

    // Створення репозиторію для роботи з DAO
    val repository = remember { AdvancedEnergyRepository(db.advancedDao()) }

    // Ініціалізація ViewModel
    val viewModel: AdvancedEnergyViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AdvancedEnergyViewModel(repository) as T
            }
        }
    )

    // Локальні стани для полів введення
    var stationName by remember { mutableStateOf("") }
    var powerInput by remember { mutableStateOf("") }
    var temperatureInput by remember { mutableStateOf("") }

    // Відстежування потоків StateFlow із ViewModel
    val stations by viewModel.stations.collectAsState()
    val powers by viewModel.powers.collectAsState()
    val temperatures by viewModel.temperatures.collectAsState()
    val selectedStationId by viewModel.selectedStationId.collectAsState()
    val averagePower by viewModel.averagePower.collectAsState()
    val forecastPower by viewModel.forecastPower.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortDescending by viewModel.sortDescending.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth > 600.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Локальне сховище ГЕОТЕС",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Відображення повідомлення про помилку валідації або БД
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Поля введення даних нової геотермальної станції
            OutlinedTextField(
                value = stationName,
                onValueChange = { stationName = it },
                label = { Text("Назва геотермальної станції") },
                placeholder = { Text("Наприклад: ГЕОТЕС-1") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = powerInput,
                onValueChange = { powerInput = it },
                label = { Text("Потужності свердловин у МВт (через ,)") },
                placeholder = { Text("Наприклад: 12.5, 15.0, 9.8") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = temperatureInput,
                onValueChange = { temperatureInput = it },
                label = { Text("Температури теплоносія у °C (через ,)") },
                placeholder = { Text("Наприклад: 185, 210, 195") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Кнопка збереження станції та її показників у транзакції Room
            Button(
                onClick = {
                    val powersList = powerInput.split(",")
                        .mapNotNull { it.trim().toDoubleOrNull() }
                    val tempsList = temperatureInput.split(",")
                        .mapNotNull { it.trim().toDoubleOrNull() }

                    viewModel.addStationWithData(stationName, powersList, tempsList)

                    if (stationName.isNotBlank() && powersList.isNotEmpty() && tempsList.isNotEmpty()) {
                        stationName = ""
                        powerInput = ""
                        temperatureInput = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Додати геотермальну станцію з даними")
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            // Пошук за назвою (фільтрація) та перемикання сортування
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    label = { Text("Пошук станції за назвою") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                OutlinedButton(
                    onClick = { viewModel.toggleSortOrder() }
                ) {
                    Text(if (sortDescending) "Сорт: Спадання ↓" else "Сорт: Зростання ↑")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Список збережених геотермальних станцій
            Text(
                text = "Список геотермальних станцій:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (stations.isEmpty()) {
                Text(
                    text = "Немає збережених станцій у базі даних.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                stations.forEach { station ->
                    StationItemCard(
                        station = station,
                        isSelected = station.id == selectedStationId,
                        onSelect = { viewModel.selectStation(station.id) },
                        onDelete = { viewModel.deleteStation(station) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Деталі по обраній геотермальній станції
            selectedStationId?.let {
                val selectedName = stations.find { s -> s.id == it }?.name ?: "Обрана станція"

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Аналітика станції: $selectedName",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format(
                                Locale.US,
                                "Середня потужність: %.2f МВт",
                                averagePower
                            )
                        )
                        Text(
                            text = String.format(
                                Locale.US,
                                "Прогнозована потужність (з урахуванням t° пари): %.2f МВт",
                                forecastPower
                            ),
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Адаптивне розміщення списків параметрів
                        if (isWideScreen) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                ParameterListBlock(
                                    title = "Потужності станції:",
                                    values = powers.map { p -> "${p.value} МВт" },
                                    modifier = Modifier.weight(1f)
                                )
                                ParameterListBlock(
                                    title = "Температури свердловин:",
                                    values = temperatures.map { t -> "${t.value} °C" },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        } else {
                            ParameterListBlock(
                                title = "Потужності станції:",
                                values = powers.map { p -> "${p.value} МВт" }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            ParameterListBlock(
                                title = "Температури свердловин:",
                                values = temperatures.map { t -> "${t.value} °C" }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Повторно використовуваний Composable-компонент для відображення картки станції у списку
@Composable
fun StationItemCard(
    station: GeothermalStation,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = station.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            TextButton(onClick = onDelete) {
                Text("Видалити", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

// Повторно використовуваний Composable-компонент для виведення відсортованого списку параметрів
@Composable
fun ParameterListBlock(
    title: String,
    values: List<String>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        values.forEach { item ->
            Text(text = "• $item", style = MaterialTheme.typography.bodyMedium)
        }
    }
}