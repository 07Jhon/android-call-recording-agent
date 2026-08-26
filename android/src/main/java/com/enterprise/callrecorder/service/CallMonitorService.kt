package com.enterprise.callrecorder.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import androidx.lifecycle.lifecycleScope
import com.enterprise.callrecorder.data.CallRecordingDatabase
import com.enterprise.callrecorder.model.CallRecording
import com.enterprise.callrecorder.model.CallStatus
import com.enterprise.callrecorder.model.CallType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * 🔴 CRITICAL SERVICE - Call Recording Monitor
 * Détecte et enregistre les appels téléphoniques
 */
@AndroidEntryPoint
class CallMonitorService : Service() {

    @Inject
    lateinit var database: CallRecordingDatabase
    
    @Inject
    lateinit var recordingManager: CallRecordingManager
    
    private lateinit var telephonyManager: TelephonyManager
    private var mediaRecorder: MediaRecorder? = null
    private var currentCallRecording: CallRecording? = null
    
    private val phoneStateListener = object : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            super.onCallStateChanged(state, phoneNumber)
            
            when (state) {
                TelephonyManager.CALL_STATE_RINGING -> {
                    // Appel entrant
                    handleIncomingCall(phoneNumber ?: "Unknown")
                }
                TelephonyManager.CALL_STATE_OFFHOOK -> {
                    // Appel accepté
                    handleCallAccepted()
                }
                TelephonyManager.CALL_STATE_IDLE -> {
                    // Appel terminé
                    handleCallEnded()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            telephonyManager.registerTelephonyCallback(
                mainExecutor,
                TelephonyCallback()
            )
        } else {
            @Suppress("DEPRECATION")
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Gère la détection d'un appel entrant
     */
    private fun handleIncomingCall(phoneNumber: String) {
        val now = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault())
        val timestamp = dateFormat.format(Date(now))
        
        currentCallRecording = CallRecording(
            deviceId = getDeviceId(),
            phoneNumber = phoneNumber,
            callType = CallType.INCOMING,
            startedAt = now,
            status = CallStatus.RECORDING
        )
    }

    /**
     * Gère l'acceptation d'un appel
     */
    private fun handleCallAccepted() {
        if (currentCallRecording == null) return
        
        // Vérifier si l'enregistrement est supporté
        if (!recordingManager.isRecordingSupported()) {
            currentCallRecording = currentCallRecording?.copy(
                status = CallStatus.UPLOAD_FAILED
            )
            return
        }
        
        // Démarrer l'enregistrement audio
        try {
            startAudioRecording()
        } catch (e: Exception) {
            android.util.Log.e("CallMonitor", "Recording error", e)
            currentCallRecording = currentCallRecording?.copy(
                status = CallStatus.UPLOAD_FAILED
            )
        }
    }

    /**
     * Gère la fin d'un appel
     */
    private fun handleCallEnded() {
        if (currentCallRecording == null) return
        
        currentCallRecording = currentCallRecording?.copy(
            endedAt = System.currentTimeMillis(),
            status = CallStatus.PROCESSING
        )
        
        // Arrêter l'enregistrement
        stopAudioRecording()
        
        // Sauvegarder en BDD
        lifecycleScope.launch {
            currentCallRecording?.let { recording ->
                database.callRecordingDao().insert(recording)
                
                // Mettre en queue pour upload
                updateCallStatus(recording.id!!, CallStatus.PENDING_UPLOAD)
            }
        }
        
        currentCallRecording = null
    }

    /**
     * Démarre l'enregistrement audio de l'appel
     */
    private fun startAudioRecording() {
        if (mediaRecorder != null) return
        
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.VOICE_CALL)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioChannels(1)
            setAudioSamplingRate(16000)
            setAudioEncodingBitRate(128000)
            
            val fileName = generateFileName()
            val recordingFile = File(getRecordingsDir(), fileName)
            
            setOutputFile(recordingFile.absolutePath)
            
            try {
                prepare()
                start()
                
                currentCallRecording = currentCallRecording?.copy(
                    fileName = fileName,
                    status = CallStatus.RECORDING
                )
            } catch (e: Exception) {
                android.util.Log.e("CallMonitor", "Failed to start recording", e)
                throw e
            }
        }
    }

    /**
     * Arrête l'enregistrement audio
     */
    private fun stopAudioRecording() {
        mediaRecorder?.apply {
            try {
                stop()
                release()
            } catch (e: Exception) {
                android.util.Log.e("CallMonitor", "Error stopping recording", e)
            }
        }
        mediaRecorder = null
    }

    /**
     * Génère un nom de fichier pour l'enregistrement
     */
    private fun generateFileName(): String {
        val timestamp = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault())
        return "CALL-${dateFormat.format(Date(timestamp))}-${UUID.randomUUID()}.m4a"
    }

    /**
     * Retourne le répertoire des enregistrements
     */
    private fun getRecordingsDir(): File {
        return File(filesDir, "recordings").apply {
            if (!exists()) mkdirs()
        }
    }

    /**
     * Retourne l'ID de l'appareil
     */
    private fun getDeviceId(): String {
        // À implémenter selon la configuration
        return android.os.Build.DEVICE
    }

    /**
     * Met à jour le statut d'un enregistrement
     */
    private fun updateCallStatus(recordingId: Long, status: CallStatus) {
        lifecycleScope.launch {
            database.callRecordingDao().updateStatus(recordingId, status)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAudioRecording()
        
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            @Suppress("DEPRECATION")
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
        }
    }
}
