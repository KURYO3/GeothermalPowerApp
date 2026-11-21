package ua.kpi.practical_example_1.pw3.lab11.advanced

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import ua.kpi.practical_example_1.pw3.lab11.medium.GeothermalStation
import ua.kpi.practical_example_1.pw3.lab11.medium.LoginRequest
import ua.kpi.practical_example_1.pw3.lab11.medium.LoginResponse
import ua.kpi.practical_example_1.pw3.lab11.medium.ProtectedMessage

// Запити для входу й доступу до захищених даних станції
interface AuthApi {

    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("register")
    suspend fun register(@Body request: LoginRequest): Response<Unit>

    @GET("protected")
    suspend fun getProtectedData(): ProtectedMessage

    @GET("stations")
    suspend fun getStations(): List<GeothermalStation>

    // Список геотермальних об'єктів для адміністратора
    @GET("admin-only")
    suspend fun getAdminOnly(): List<GeothermalStation>
}
