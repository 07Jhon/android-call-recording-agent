package com.enterprise.callrecorder.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * API Service pour la communication avec le backend
 */
interface CallRecordingApiService {

    /**
     * Enregistre le device et récupère son token API. Doit être appelé une
     * seule fois (ou tant qu'aucun token n'est stocké localement) — voir
     * DeviceAuthManager. N'a pas besoin de token, c'est justement cette
     * route qui en délivre un.
     */
    @FormUrlEncoded
    @POST("/api/v1/devices/register")
    suspend fun registerDevice(
        @Field("deviceId") deviceId: String,
        @Field("deviceName") deviceName: String?,
        @Field("androidVersion") androidVersion: Int?,
        @Field("manufacturer") manufacturer: String?,
        @Field("model") model: String?
    ): Response<DeviceRegistrationResponse>

    @Multipart
    @POST("/api/v1/call-recordings/upload")
    suspend fun uploadRecording(
        @Part deviceId: MultipartBody.Part,
        @Part phoneNumber: MultipartBody.Part,
        @Part callType: MultipartBody.Part,
        @Part duration: MultipartBody.Part,
        @Part fileSize: MultipartBody.Part,
        @Part file: MultipartBody.Part
    ): Response<UploadResponse>
}

/**
 * Réponse du serveur lors d'un upload
 */
data class UploadResponse(
    val success: Boolean,
    val storageKey: String?,
    val message: String?
)

/**
 * Réponse du serveur lors de l'enregistrement d'un device
 */
data class DeviceRegistrationResponse(
    val deviceId: String,
    val apiToken: String,
    val message: String?
)
