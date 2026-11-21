package ua.kpi.practical_example_1.pw3.lab11.medium

// Токен доступу та роль, які повертає сервер після входу
data class LoginResponse(
    val token: String,
    val role: String
)
