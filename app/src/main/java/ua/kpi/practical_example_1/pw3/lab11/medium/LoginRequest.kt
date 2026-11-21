package ua.kpi.practical_example_1.pw3.lab11.medium

import com.google.gson.annotations.SerializedName

// Дані для входу користувача
data class LoginRequest(
    @SerializedName("Username") val username: String,
    @SerializedName("Password") val password: String
)
