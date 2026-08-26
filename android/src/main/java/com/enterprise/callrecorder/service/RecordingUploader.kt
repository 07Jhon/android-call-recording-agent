package com.enterprise.callrecorder.service

import android.content.Context
import com.enterprise.callrecorder.api.CallRecordingApiService
import com.enterprise.callrecorder.model.CallRecording
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.parse
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

/**
 * Gère l'upload des enregistrements vers le backend
 */
class RecordingUploader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: CallRecordingApiService
) {

    /**
     * Upload un enregistrement vers le serveur
     */
    suspend fun uploadRecording(recording: CallRecording) {
        // Vérifier que le fichier existe
        val audioFile = File(context.filesDir, "recordings/${recording.fileName}")
        if (!audioFile.exists()) {
            throw IllegalArgumentException("Recording file not found: ${recording.fileName}")
        }

        // Créer la requête multipart
        val audioBody = audioFile.asRequestBody("audio/mp4".parse())
        val audioPart = MultipartBody.Part.createFormData(
            "file",
            audioFile.name,
            audioBody
        )

        // Métadonnées
        val deviceId = MultipartBody.Part.createFormData("deviceId", recording.deviceId)
        val phoneNumber = MultipartBody.Part.createFormData("phoneNumber", recording.phoneNumber)
        val callType = MultipartBody.Part.createFormData("callType", recording.callType.name)
        val duration = MultipartBody.Part.createFormData("duration", "${recording.duration ?: 0}")
        val fileSize = MultipartBody.Part.createFormData("fileSize", "${audioFile.length()}")

        // Envoyer vers le backend
        val response = apiService.uploadRecording(
            deviceId,
            phoneNumber,
            callType,
            duration,
            fileSize,
            audioPart
        )

        if (!response.isSuccessful) {
            throw Exception("Upload failed: ${response.code()} - ${response.message()}")
        }

        // La réponse contient l'ID de stockage
        val storageKey = response.body()?.storageKey
            ?: throw Exception("No storage key returned from server")

        // Mettre à jour la BD locale avec la clé de stockage
        // (À implémenter)
    }

    /**
     * Supprime un fichier d'enregistrement local après confirmation du serveur
     */
    fun deleteLocalRecording(fileName: String) {
        val file = File(context.filesDir, "recordings/$fileName")
        if (file.exists()) {
            file.delete()
        }
    }
}
