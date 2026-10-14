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
import androidx.compose.material3.TextButton
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
fun GeothermalApp4Level2() {
    // Змінна для перемикання міє логіном і реєстрацією
    var isRegisterMode by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Реєстрація
        if (isRegisterMode) {
            Text(
                text = "Реєстрація: Геотермальна станція",
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Змінні
            var name by remember { mutableStateOf("") }
            var email by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }
            var confirmPassword by remember { mutableStateOf("") }
            var message by remember { mutableStateOf("") }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Ім'я") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Підтвердження пароля") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    message = when {
                        name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() ->
                            "Заповніть усі поля"
                        password != confirmPassword ->
                            "Паролі не співпадають"
                        else -> "Реєстрація успішна!"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Зареєструватися")
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = { isRegisterMode = false }) {
                Text("Вже є акаунт? Увійти")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = message,
                color = if (message.contains("успішна")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )

        } else {
            // Логін
            Text(
                text = "Авторизація: Геотермальна станція",
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Змінні
            var login by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }
            var message by remember { mutableStateOf("") }

            OutlinedTextField(
                value = login,
                onValueChange = { login = it },
                label = { Text("Логін") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
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

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = { isRegisterMode = true }) {
                Text("Ще немає акаунту? Зареєструватися")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = message,
                color = if (message.contains("успішна")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}