package com.enterprise.callrecorder.auth

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Attache "Authorization: Bearer <token>" à chaque requête sortante, si un
 * token est déjà stocké localement. La route d'enregistrement
 * (/api/v1/devices/register) n'a pas besoin de token et fonctionne même
 * sans header (le backend l'autorise explicitement).
 *
 * Lit SharedPreferences directement plutôt que d'injecter DeviceAuthManager,
 * pour éviter un cycle de dépendances : DeviceAuthManager -> ApiService ->
 * Retrofit -> OkHttpClient -> Interceptor -> DeviceAuthManager.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    @ApplicationContext private val context: Context
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val prefs = context.getSharedPreferences("device_auth", Context.MODE_PRIVATE)
        val token = prefs.getString("api_token", null)

        val request = chain.request().let { original ->
            if (token != null) {
                original.newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            } else {
                original
            }
        }

        return chain.proceed(request)
    }
}
