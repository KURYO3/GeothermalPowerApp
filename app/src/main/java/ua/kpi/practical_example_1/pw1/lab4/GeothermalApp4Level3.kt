package ua.kpi.practical_example_1.pw1.lab4

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ua.kpi.practical_example_1.pw1.lab4.forms.LoginForm
import ua.kpi.practical_example_1.pw1.lab4.forms.RegistrationForm

@Composable
fun GeothermalApp4Level3() {
    var isRegisterMode by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isRegisterMode) {
            RegistrationForm(
                onRegister = { name, email, password, confirmPassword ->
                    // Імітація обробки
                    when {
                        name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() ->
                            "Заповніть усі поля"
                        password != confirmPassword ->
                            "Паролі не співпадають"
                        else -> "Реєстрація успішна!"
                    }
                },
                onSwitchToLogin = { isRegisterMode = false }
            )
        } else {
            LoginForm(
                onLogin = { login, password ->
                    // Імітація обробки
                    if (login.isBlank() || password.isBlank()) "Заповніть усі поля" else "Авторизація успішна!"
                },
                onSwitchToRegister = { isRegisterMode = true }
            )
        }
    }
}