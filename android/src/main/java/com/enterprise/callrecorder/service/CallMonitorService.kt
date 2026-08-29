package com.enterprise.callrecorder.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.enterprise.callrecorder.data.CallRecordingDatabase
import com.enterprise.callrecorder.auth.DeviceAuthManager
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
class CallMonitorService : LifecycleService() {

    @Inject
    lateinit var database: CallRecordingDatabase
    
    @Inject
    lateinit var recordingManager: CallRecordingManager

    @Inject
    lateinit var deviceAuthManager: DeviceAuthManager
    
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

    /**
     * Callback moderne (API 31+) remplaçant PhoneStateListener, qui est déprécié.
     */
    private val callStateCallback = object : TelephonyCallback(), TelephonyCallback.CallStateListener {
        override fun onCallStateChanged(state: Int) {
            when (state) {
                TelephonyManager.CALL_STATE_RINGING -> handleIncomingCall("Unknown")
                TelephonyManager.CALL_STATE_OFFHOOK -> handleCallAccepted()
                TelephonyManager.CALL_STATE_IDLE -> handleCallEnded()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForegroundWithNotification()

        lifecycleScope.launch {
            deviceAuthManager.ensureRegistered()
        }

        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            telephonyManager.registerTelephonyCallback(
                mainExecutor,
                callStateCallback
            )
        } else {
            @Suppress("DEPRECATION")
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    /**
     * Obligatoire depuis Android 8 (API 26) : un service longue durée doit
     * passer en foreground avec une notification visible sous 5 secondes,
     * sinon le système le tue (ForegroundServiceDidNotStartInTimeException
     * sur API 31+).
     */
    private fun startForegroundWithNotification() {
        val channelId = "call_recording_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Enregistrement d'appels",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = Notification.Builder(this, channelId)
            .setContentTitle("Agent d'enregistrement actif")
            .setContentText("Surveillance des appels en cours")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                1,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            startForeground(1, notification)
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return super.onBind(intent)
    }

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
    private fun getDeviceId(): String = deviceAuthManager.deviceId

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
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            telephonyManager.unregisterTelephonyCallback(callStateCallback)
        } else {
            @Suppress("DEPRECATION")
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
        }
    }
}
