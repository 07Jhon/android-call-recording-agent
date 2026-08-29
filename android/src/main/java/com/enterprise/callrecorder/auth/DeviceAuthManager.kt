package com.enterprise.callrecorder.auth

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.provider.Settings
import com.enterprise.callrecorder.api.CallRecordingApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gère le cycle de vie du token d'authentification du device :
 *  - au premier lancement, appelle POST /api/v1/devices/register (public,
 *    sans authentification) pour obtenir un token
 *  - stocke ce token localement (SharedPreferences)
 *  - le fournit à AuthInterceptor pour chaque requête suivante
 *
 * ⚠️ SharedPreferences classique n'est pas chiffré. Pour une vraie mise en
 * production, remplacer par EncryptedSharedPreferences (androidx.security).
 */
@Singleton
class DeviceAuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: CallRecordingApiService
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("device_auth", Context.MODE_PRIVATE)

    val deviceId: String
        get() = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?: "unknown-device"

    fun getStoredToken(): String? = prefs.getString(KEY_TOKEN, null)

    /**
     * Retourne le token existant, ou en obtient un nouveau auprès du backend
     * si aucun n'est stocké localement. À appeler avant toute requête
     * authentifiée (voir CallMonitorService.onCreate).
     */
    suspend fun ensureRegistered(): String? = withContext(Dispatchers.IO) {
        getStoredToken()?.let { return@withContext it }

        try {
            val response = apiService.registerDevice(
                deviceId = deviceId,
                deviceName = Build.MODEL,
                androidVersion = Build.VERSION.SDK_INT,
                manufacturer = Build.MANUFACTURER,
                model = Build.MODEL
            )

            if (response.isSuccessful) {
                val token = response.body()?.apiToken
                if (token != null) {
                    prefs.edit().putString(KEY_TOKEN, token).apply()
                }
                token
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        private const val KEY_TOKEN = "api_token"
    }
}
