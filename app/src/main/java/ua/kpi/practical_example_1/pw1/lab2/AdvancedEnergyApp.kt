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
import ua.kpi.practical_example_1.pw1.lab2.data.Calculator // Імпортуємо наш клас калькулятора

@Composable
fun AdvancedEnergyApp() {
    // Змінні стану
    var waterTemp by remember { mutableStateOf("") }
    var waterFlow by remember { mutableStateOf("") }
    var turbineEfficiency by remember { mutableStateOf("") }
    var result by remember { mutableStateOf(0.0) }

    // Екземаляр класу для обчислень
    val calculator = Calculator()

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
            text = "Просунутий рівень: масштабування проєкту",
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

        TextField(
            value = turbineEfficiency,
            onValueChange = { turbineEfficiency = it },
            label = { Text("ККД турбіни (%)") }
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = {
            // Виклик методу класу
            result = calculator.calculatePowerAdvanced(waterTemp, waterFlow, turbineEfficiency)
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