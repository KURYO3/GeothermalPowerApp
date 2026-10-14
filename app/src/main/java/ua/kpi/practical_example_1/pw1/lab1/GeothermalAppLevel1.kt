package ua.kpi.practical_example_1.pw1.lab1

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GeothermalAppLevel1() {
    // Column для вертикального розташування
    Column(
        modifier = Modifier
            .fillMaxSize() // заповнити весь простір екрана
            .background(Color(0xFFE8F5E9)) // колір фону
            .padding(16.dp), // відступ
        horizontalAlignment = Alignment.CenterHorizontally, // центрування по горизонталі
        verticalArrangement = Arrangement.Center // центрування по вертикалі
    ) {
        // Текст заголовок
        Text(
            text = "Hello Geothermal Power",
            fontSize = 28.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(24.dp)) // відступ між елементами

        // Кнопка
        Button(onClick = { }) {
            Text("Оновити дані") // текст на кнопці
        }
    }
}