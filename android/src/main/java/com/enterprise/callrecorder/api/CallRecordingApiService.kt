package com.enterprise.callrecorder.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * API Service pour la communication avec le backend
 */
interface CallRecordingApiService {

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
