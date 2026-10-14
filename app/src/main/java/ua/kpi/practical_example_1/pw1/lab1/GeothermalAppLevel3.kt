package ua.kpi.practical_example_1.pw1.lab1

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

@Composable
fun GeothermalAppLevel3() {
    // Змінні для потужності та температури
    var powerForecast by remember { mutableStateOf(0) }
    var waterTemp by remember { mutableStateOf(0) }

    // Анімація зміни значень
    val animatedPower by animateIntAsState(
        targetValue = powerForecast,
        label = "PowerAnimation"
    )
    val animatedTemp by animateIntAsState(
        targetValue = waterTemp,
        label = "TempAnimation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Колір фону з MaterialTheme
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Геотермальна електростанція",
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.primary // Первинний колір теми
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Потужність: $animatedPower Вт",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "Температура: $animatedTemp °C",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = {
            powerForecast = Random.nextInt(1000, 5000)
            waterTemp = Random.nextInt(150, 300)
        }) {
            Text("Оновити дані")
        }
    }
}