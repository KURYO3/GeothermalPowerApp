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
fun BasicEnergyApp() {
    // Збереження параметра у стані Compose
    var parameter by remember { mutableStateOf("") }

    // Збереження результату у стані Compose
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
            text = "Введіть температуру для розрахунку:",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Поле для введення
        TextField(
            value = parameter,
            onValueChange = { parameter = it },
            label = { Text("Температура води (°C)") }
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = {
            result = calculatePowerBasic(parameter)
        }) {
            Text("Розрахувати")
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Відображення результату
        Text(
            text = "Розрахована потужність: $result Вт",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

// Математика
fun calculatePowerBasic(input: String): Double {
    // Перетворення вхідного рядка в число, якщо не можливо - 0
    val value = input.toDoubleOrNull() ?: 0.0
    // Випадковий коефіцієнт
    return value * 15.5
}