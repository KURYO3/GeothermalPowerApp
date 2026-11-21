package ua.kpi.practical_example_1.pw3.lab11.advanced

import android.content.Context
import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ua.kpi.practical_example_1.pw3.lab11.medium.AuthDataStore
import ua.kpi.practical_example_1.pw3.lab11.medium.AuthInterceptor
import ua.kpi.practical_example_1.pw3.lab11.medium.GeothermalStation
import ua.kpi.practical_example_1.pw3.lab11.medium.LoginRequest
import ua.kpi.practical_example_1.pw3.lab11.medium.LoginResponse
import ua.kpi.practical_example_1.pw3.lab11.medium.ProtectedMessage

// Керує станом входу, роллю та завантаженням захищених даних
class AuthViewModel(
    private val authDataStore: AuthDataStore,
    private val authApi: AuthApi
) : ViewModel() {

    var token by mutableStateOf<String?>(null)
        private set

    var role by mutableStateOf<String?>(null)
        private set

    var protectedData by mutableStateOf<ProtectedMessage?>(null)
        private set

    var adminData by mutableStateOf<List<GeothermalStation>?>(null)
        private set

    var statusInfo by mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch {
            authDataStore.tokenFlow.collect { savedToken ->
                token = savedToken
            }
        }
        viewModelScope.launch {
            authDataStore.roleFlow.collect { savedRole ->
                role = savedRole
            }
        }
    }

    fun login(username: String, password: String, onError: (String) -> Unit) {
        if (username.isBlank() || password.isBlank()) {
            onError("Введіть логін і пароль")
            return
        }
        viewModelScope.launch {
            try {
                val response = authApi.login(LoginRequest(username.trim(), password.trim()))
                authDataStore.saveAuthData(response.token, response.role)
                token = response.token
                role = response.role
                statusInfo = "Вхід виконано. Роль: ${response.role}"
            } catch (e: HttpException) {
                onError("Не вдалося ввійти (HTTP ${e.code()}). Перевірте логін і пароль.")
            } catch (e: Exception) {
                onError(e.message ?: "Не вдалося ввійти")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authDataStore.clearToken()
            token = null
            role = null
            protectedData = null
            adminData = null
            statusInfo = null
        }
    }

    fun loadProtectedData(onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                // Демонстраційний прострочений токен
                if (token?.contains("EXPIRED") == true) {
                    logout()
                    onError("Термін дії токена минув. Увійдіть знову.")
                    return@launch
                }
                protectedData = authApi.getProtectedData()
            } catch (e: HttpException) {
                if (e.code() == 401) {
                    logout()
                    onError("HTTP 401: Токен недійсний або строк його дії минув. Увійдіть знову.")
                } else {
                    onError("HTTP ${e.code()}: Доступ заборонено")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Не вдалося завантажити захищені дані")
            }
        }
    }

    fun loadAdminData(onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                adminData = authApi.getAdminOnly()
            } catch (e: HttpException) {
                onError("HTTP ${e.code()}: Доступ лише для адміністратора")
            } catch (e: Exception) {
                onError(e.message ?: "Не вдалося завантажити реєстр геотермальних об'єктів")
            }
        }
    }
}

// Створює ViewModel і налаштовує мережевий клієнт
class AuthViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    private val gson = Gson()

    // Повертає демонстраційні відповіді, якщо мережевий виклик завершується винятком
    private val mockAuthServerInterceptor = Interceptor { chain ->
        val request = chain.request()
        try {
            chain.proceed(request)
        } catch (e: Exception) {
            val path = request.url.encodedPath
            val authHeader = request.header("Authorization")

            when {
                path == "/login" && request.method == "POST" -> {
                    val buffer = Buffer()
                    request.body?.writeTo(buffer)
                    val bodyStr = buffer.readUtf8()
                    val loginReq = gson.fromJson(bodyStr, LoginRequest::class.java)

                    when {
                        loginReq.username == "admin" && loginReq.password == "admin123" -> {
                            val token = Base64.encodeToString("admin:ADMIN:VALID".toByteArray(), Base64.NO_WRAP)
                            buildResponse(request, 200, gson.toJson(LoginResponse(token, "ADMIN")))
                        }
                        loginReq.username == "user" && loginReq.password == "user123" -> {
                            val token = Base64.encodeToString("user:USER:VALID".toByteArray(), Base64.NO_WRAP)
                            buildResponse(request, 200, gson.toJson(LoginResponse(token, "USER")))
                        }
                        loginReq.username == "expired" && loginReq.password == "expired123" -> {
                            val token = "jwt_mock_EXPIRED_token_777"
                            buildResponse(request, 200, gson.toJson(LoginResponse(token, "USER")))
                        }
                        else -> buildResponse(request, 401, """{"error":"Unauthorized"}""")
                    }
                }

                path == "/protected" && request.method == "GET" -> {
                    if (authHeader.isNullOrBlank() || !authHeader.startsWith("Bearer ")) {
                        buildResponse(request, 401, """{"error":"Missing Bearer token"}""")
                    } else if (authHeader.contains("EXPIRED")) {
                        buildResponse(request, 401, """{"error":"Token expired"}""")
                    } else {
                        val isAdmin = authHeader.contains(
                            Base64.encodeToString("admin:ADMIN:VALID".toByteArray(), Base64.NO_WRAP)
                        )
                        val msg = if (isAdmin) {
                            "Вітаємо, головний диспетчере (ADMIN)! Тиск у контурі станції — 21.4 бар, температура пари — 206 °C."
                        } else {
                            "Вітаємо, операторе зміни (USER)! Стан свердловин у нормі (194 °C)."
                        }
                        buildResponse(request, 200, gson.toJson(ProtectedMessage(msg)))
                    }
                }

                path == "/admin-only" && request.method == "GET" -> {
                    val adminToken = Base64.encodeToString("admin:ADMIN:VALID".toByteArray(), Base64.NO_WRAP)
                    if (authHeader == "Bearer $adminToken") {
                        val stations = listOf(
                            GeothermalStation(1, "Геотермальний блок № 1 (Берегово)", 12.5, 2600),
                            GeothermalStation(2, "Геотермальний блок № 2 (Мукачево)", 18.0, 2850),
                            GeothermalStation(3, "Бінарна геотермальна установка № 3", 8.4, 2100)
                        )
                        buildResponse(request, 200, gson.toJson(stations))
                    } else {
                        buildResponse(request, 403, """{"error":"Forbidden: Admin role required"}""")
                    }
                }

                else -> buildResponse(request, 404, """{"error":"Not Found"}""")
            }
        }
    }

    private fun buildResponse(request: Request, code: Int, jsonBody: String): Response {
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(if (code == 200) "OK" else "Error")
            .body(jsonBody.toResponseBody("application/json".toMediaType()))
            .build()
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            val authDataStore = AuthDataStore(context)
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(authDataStore))
                .addInterceptor(mockAuthServerInterceptor)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl("http://10.0.2.2:5156/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val authApi = retrofit.create(AuthApi::class.java)
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(authDataStore, authApi) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
