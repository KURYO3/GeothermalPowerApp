package ua.kpi.practical_example_1.pw1.lab4

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GeothermalApp4Level1() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Авторизація: Геотермальна станція",
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Змінні
        var login by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var message by remember { mutableStateOf("") }

        // Поле логіну
        OutlinedTextField(
            value = login,
            onValueChange = { login = it },
            label = { Text("Логін") },
            singleLine = true, // Дозволяє вводити лише один рядок
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Поле пароля
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль") },
            visualTransformation = PasswordVisualTransformation(), // Приховує символи пароля
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), // Клавіатура для пароля
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                // Перевірка на заповненість полів
                message = if (login.isBlank() || password.isBlank()) {
                    "Будь ласка, заповніть усі поля"
                } else {
                    "Авторизація успішна!"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Увійти")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            color = if (message.contains("успішна"))
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.error
        )
    }
}