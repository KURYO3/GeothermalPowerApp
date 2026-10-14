package ua.kpi.practical_example_1.pw1.lab1

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

@Composable
fun GeothermalAppLevel2() {
    // Оголошення змінних
    var powerForecast by remember { mutableStateOf(0) }
    var waterTemp by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE8F5E9))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Текст заголовок
        Text(
            text = "Геотермальна електростанція",
            fontSize = 24.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Row для компонування в рядок
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Потужність: $powerForecast Вт",
                fontSize = 18.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "Температура: $waterTemp °C",
                fontSize = 18.sp,
                color = Color.DarkGray
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Кнопка
        Button(onClick = {
            // Генерації рандомних чисел
            powerForecast = Random.nextInt(1000, 5000)
            waterTemp = Random.nextInt(150, 300)
        }) {
            Text("Оновити дані")
        }
    }
}