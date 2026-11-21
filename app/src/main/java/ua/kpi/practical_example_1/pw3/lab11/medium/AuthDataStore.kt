package ua.kpi.practical_example_1.pw3.lab11.medium

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.authDataStore by preferencesDataStore(name = "geothermal_auth_prefs")

// Зберігає токен і роль користувача в Preferences DataStore
class AuthDataStore(private val context: Context) {

    private val dataStore = context.authDataStore
    private val TOKEN_KEY = stringPreferencesKey("token")
    private val ROLE_KEY = stringPreferencesKey("role")

    val tokenFlow: Flow<String?> = dataStore.data.map { prefs ->
        prefs[TOKEN_KEY]
    }

    val roleFlow: Flow<String?> = dataStore.data.map { prefs ->
        prefs[ROLE_KEY]
    }

    suspend fun saveAuthData(token: String, role: String) {
        dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[ROLE_KEY] = role
        }
    }

    suspend fun clearToken() {
        dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
            prefs.remove(ROLE_KEY)
        }
    }
}
