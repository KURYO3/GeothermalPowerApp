package ua.kpi.practical_example_1.pw1.lab3

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.kpi.practical_example_1.R

@Composable
fun GeothermalApp3Level3() {
    // Змінні
    var power by remember { mutableStateOf(0) }
    var mode by remember { mutableStateOf("Очікування") }
    var comments by remember { mutableStateOf("Оберіть режим роботи станції") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Геотермальна станція",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Кастомна кнопка
            GeothermalModeButton("Еко", 1500) { label, value ->
                mode = label
                power = value
                comments = "Економія ресурсу свердловини"
            }
            GeothermalModeButton("Стандарт", 3000) { label, value ->
                mode = label
                power = value
                comments = "Нормальна генерація енергії"
            }
            GeothermalModeButton("Пік", 4800) { label, value ->
                mode = label
                power = value
                comments = "Максимальне навантаження на турбіни."
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Image(
            painter = painterResource(id = R.drawable.geothermal_plant),
            contentDescription = "Геотермальна електростанція",
            modifier = Modifier
                .height(200.dp)
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Кастомна картка
        GeothermalInfoCard(power = power, mode = mode, comments = comments)
    }
}

// Кастомний Compasable для кнопки
@Composable
fun GeothermalModeButton(
    label: String,
    value: Int,
    onClick: (String, Int) -> Unit
) {
    Button(
        onClick = { onClick(label, value) }
    ) {
        Text(text = label)
    }
}

// Кастомний Compasable для картки
@Composable
fun GeothermalInfoCard(power: Int, mode: String, comments: String) {
    Card(
        shape = RoundedCornerShape(12.dp), // Заокруглені кути
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Режим: $mode",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Прогнозована потужність: $power кВт",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Коментар: $comments",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}