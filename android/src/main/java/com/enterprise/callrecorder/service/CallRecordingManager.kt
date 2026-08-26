package com.enterprise.callrecorder.service

import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Gère la création et la configuration des enregistrements
 */
class CallRecordingManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Vérifie si l'enregistrement d'appel est supporté sur l'appareil
     */
    fun isRecordingSupported(): Boolean {
        return try {
            // Vérifier les permissions
            val hasRecordAudio = context.checkSelfPermission(
                android.Manifest.permission.RECORD_AUDIO
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            
            val hasPhoneState = context.checkSelfPermission(
                android.Manifest.permission.READ_PHONE_STATE
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            
            // Vérifier les APIs disponibles
            val apiLevel = Build.VERSION.SDK_INT
            val hasMediaRecorder = hasMediaRecorderSupport()
            
            hasRecordAudio && hasPhoneState && hasMediaRecorder && apiLevel >= Build.VERSION_CODES.LOLLIPOP
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Vérifie si MediaRecorder est disponible
     */
    private fun hasMediaRecorderSupport(): Boolean {
        return try {
            val recorderClass = Class.forName("android.media.MediaRecorder")
            recorderClass != null
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Vérifie si l'appareil a un microphone
     */
    fun hasMicrophone(): Boolean {
        return context.packageManager.hasSystemFeature(
            android.content.pm.PackageManager.FEATURE_MICROPHONE
        )
    }

    /**
     * Retourne les informations de support de l'appareil
     */
    fun getDeviceCapabilities(): DeviceCapabilities {
        return DeviceCapabilities(
            isRecordingSupported = isRecordingSupported(),
            hasMicrophone = hasMicrophone(),
            androidVersion = Build.VERSION.SDK_INT,
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            device = Build.DEVICE
        )
    }
}

/**
 * Capacités de l'appareil pour l'enregistrement
 */
data class DeviceCapabilities(
    val isRecordingSupported: Boolean,
    val hasMicrophone: Boolean,
    val androidVersion: Int,
    val manufacturer: String,
    val model: String,
    val device: String
)
