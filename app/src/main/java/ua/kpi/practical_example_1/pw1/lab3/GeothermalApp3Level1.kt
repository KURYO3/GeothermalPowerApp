package ua.kpi.practical_example_1.pw1.lab3

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.kpi.practical_example_1.R // Імпортуємо ресурси вашого проєкту

@Composable
fun GeothermalApp3Level1() {
    // Змінна для тексту
    var powerText by remember { mutableStateOf("Поточна потужність: 0 кВт") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Геотермальна електростанція",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Відображення зображення з drawable
        Image(
            // Назва файлу без .jpg
            painter = painterResource(id = R.drawable.geothermal_plant),
            contentDescription = "Геотермальна електростанція",
            modifier = Modifier
                .height(200.dp)
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Змінюваний текст
        Text(
            text = powerText,
            fontSize = 18.sp,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                // Зміна тексту
                powerText = "Прогнозована потужність: 3500 кВт"
            }
        ) {
            Text(text = "Оновити прогноз")
        }
    }
}