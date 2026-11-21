package ua.kpi.practical_example_1.pw4.lab16.composables

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import ua.kpi.practical_example_1.pw4.lab16.data.EnergyStation

@SuppressLint("MissingPermission")
@Composable
fun AdvancedApp() {
    val context = LocalContext.current

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val stations = remember {
        mutableStateListOf(
            EnergyStation(
                "Геотермальна станція №1",
                50.4501,
                30.5234,
                "Геотермальна",
                "Потужність 150 кВт • Температура свердловини 180 °C",
                150,
                "Активна"
            ),
            EnergyStation(
                "Геотермальна станція №2",
                50.4547,
                30.5238,
                "Геотермальна",
                "Потужність 250 кВт • Температура свердловини 210 °C",
                250,
                "Активна"
            ),
            EnergyStation(
                "Геотермальна станція №3",
                50.4495,
                30.5210,
                "Геотермальна",
                "Потужність 180 кВт • Температура свердловини 195 °C",
                180,
                "На ремонті"
            ),
            EnergyStation(
                "Бінарна станція №1",
                50.4520,
                30.5250,
                "Бінарна",
                "Потужність 320 кВт • Бінарний контур",
                320,
                "Активна"
            )
        )
    }

    // Координати відсутні, доки служба геолокації їх не поверне
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var hasLocationPermission by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(50.4501, 30.5234), 13f)
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
    }

    // Перевіряємо дозвіл на точне місцеположення під час входу в композицію
    LaunchedEffect(Unit) {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) -> {
                hasLocationPermission = true
            }
            else -> locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // DisposableEffect підписується після надання дозволу й знімає підписку в onDispose
    DisposableEffect(hasLocationPermission) {
        if (!hasLocationPermission) {
            return@DisposableEffect onDispose { }
        }

        // Останні відомі координати можуть надійти до першого оновлення
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                userLocation = LatLng(it.latitude, it.longitude)
            }
        }

        // Запитуємо оновлення з бажаним інтервалом 3 с і мінімальним інтервалом 1,5 с
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L)
            .setMinUpdateIntervalMillis(1500L)
            .build()

        // FusedLocationProviderClient передає оновлення в LocationCallback.
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { loc ->
                    userLocation = LatLng(loc.latitude, loc.longitude)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    var selectedType by remember { mutableStateOf("Всі") }
    var selectedStatus by remember { mutableStateOf("Всі") }
    var sortByPowerAsc by remember { mutableStateOf(true) }
    var selectedStation by remember { mutableStateOf<EnergyStation?>(null) }

    // Список і маркери використовують однакові фільтри й порядок за потужністю.
    val filteredStations by remember(stations, selectedType, selectedStatus, sortByPowerAsc) {
        derivedStateOf {
            stations
                .filter { selectedType == "Всі" || it.type == selectedType }
                .filter { selectedStatus == "Всі" || it.status == selectedStatus }
                .sortedBy { if (sortByPowerAsc) it.power else -it.power }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val gpsStatusText = when {
                    !hasLocationPermission -> "Дозвіл на точне місцеположення не надано"
                    userLocation == null -> "Очікування координат…"
                    else -> "Координати: ${"%.4f".format(userLocation!!.latitude)}, ${"%.4f".format(userLocation!!.longitude)}"
                }
                Text(
                    text = gpsStatusText,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterDropdown(
                label = "Тип",
                options = listOf("Всі", "Геотермальна", "Бінарна"),
                selectedOption = selectedType,
                onOptionSelected = { selectedType = it }
            )

            FilterDropdown(
                label = "Статус",
                options = listOf("Всі", "Активна", "На ремонті"),
                selectedOption = selectedStatus,
                onOptionSelected = { selectedStatus = it }
            )

            Button(onClick = { sortByPowerAsc = !sortByPowerAsc }) {
                Text(if (sortByPowerAsc) "Потужність ↑" else "Потужність ↓")
            }
        }

        Row(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .weight(0.35f)
                    .fillMaxHeight()
                    .background(Color(0xFFEFEFEF))
            ) {
                items(filteredStations) { station ->
                    val isSelected = station == selectedStation
                    Card(
                        modifier = Modifier
                            .padding(6.dp)
                            .fillMaxWidth()
                            .clickable {
                                selectedStation = station
                                cameraPositionState.position = CameraPosition.fromLatLngZoom(
                                    LatLng(station.latitude, station.longitude), 15f
                                )
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFFBBDEFB) else Color.White
                        )
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = station.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = "${station.type} • ${station.status}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.DarkGray
                            )
                            Text(
                                text = "Потужність: ${station.power} кВт",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Black
                            )
                            Text(
                                text = station.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Black
                            )
                        }
                    }
                }
            }

            GoogleMap(
                modifier = Modifier.weight(0.65f),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = true)
            ) {
                // Маркер вибирає картку, а дотик до картки переміщує камеру.
                filteredStations.forEach { station ->
                    val markerState = remember(station.name) {
                        MarkerState(position = LatLng(station.latitude, station.longitude))
                    }
                    Marker(
                        state = markerState,
                        title = station.name,
                        snippet = "${station.type} • ${station.status} | Потужність: ${station.power} кВт",
                        onClick = {
                            selectedStation = station
                            false
                        }
                    )
                }

                // Маркер «Ви тут» з'являється, коли доступні координати пристрою
                userLocation?.let { currentGps ->
                    val userMarkerState = remember(currentGps) {
                        MarkerState(position = currentGps)
                    }
                    Marker(
                        state = userMarkerState,
                        title = "Ви тут",
                        snippet = "Місцеположення пристрою"
                    )
                }
            }
        }
    }
}

@Composable
fun FilterDropdown(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Button(onClick = { expanded = true }) {
            Text("$label: ${mapFilterOptionLabel(selectedOption)}")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(mapFilterOptionLabel(option)) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun mapFilterOptionLabel(value: String): String = if (value == "Всі") "Усі" else value
