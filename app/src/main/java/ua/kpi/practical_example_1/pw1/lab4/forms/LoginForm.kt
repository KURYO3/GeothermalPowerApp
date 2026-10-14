package ua.kpi.practical_example_1.pw1.lab4.forms

import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun LoginForm(
    onLogin: (String, String) -> String,
    onSwitchToRegister: () -> Unit
) {
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Text(
        text = "Авторизація: Геотермальна станція",
        fontSize = 24.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 16.dp)
    )

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
            if (!isLoading) {
                // Імітація обробки
                message = ""
                val result = onLogin(login, password)

                if (result.contains("успішн", ignoreCase = true)) {
                    isLoading = true

                    scope.launch {
                        delay(1000.milliseconds)
                        message = result
                        isLoading = false
                    }
                } else {
                    message = result
                }
            }
        },
        enabled = !isLoading,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        } else {
            Text("Увійти")
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    TextButton(onClick = onSwitchToRegister) {
        Text("Ще немає акаунту? Зареєструватися")
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = message,
        color = if (message.contains("успішна")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    )
}