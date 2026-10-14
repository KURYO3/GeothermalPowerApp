package ua.kpi.practical_example_1.pw1.lab2

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MediumEnergyApp() {
    // Створення змінних
    var waterTemp by remember { mutableStateOf("") }
    var waterFlow by remember { mutableStateOf("") }
    var result by remember { mutableStateOf(0.0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Геотермальна електростанція",
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Середній рівень: декілька параметрів",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(10.dp))

        TextField(
            value = waterTemp,
            onValueChange = { waterTemp = it },
            label = { Text("Температура води (°C)") }
        )

        Spacer(modifier = Modifier.height(10.dp))

        TextField(
            value = waterFlow,
            onValueChange = { waterFlow = it },
            label = { Text("Витрата води (кг/с)") }
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = {
            result = calculatePowerMedium(waterTemp, waterFlow)
        }) {
            Text("Розрахувати")
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Розрахована потужність: $result кВт",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

// Математика
fun calculatePowerMedium(temp: String, flow: String): Double {
    // Перетворення вхідного рядка в число, якщо не можливо - 0
    val t = temp.toDoubleOrNull() ?: 0.0
    val f = flow.toDoubleOrNull() ?: 0.0

    // Формула теплової потужності: дельта температур * витрата * питома теплоємність
    // Припускаємо, що температура закачування назад у пласт становить 50 градусів
    val deltaT = if (t > 50) t - 50 else 0.0

    // Результат у кВт
    return deltaT * f * 4.18
}