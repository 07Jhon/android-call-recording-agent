package com.enterprise.callrecorder.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import com.enterprise.callrecorder.data.CallRecordingDatabase
import com.enterprise.callrecorder.model.CallStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * 🔴 Service pour l'upload automatique des enregistrements
 */
@AndroidEntryPoint
class RecordingUploadService : Service() {

    @Inject
    lateinit var database: CallRecordingDatabase

    @Inject
    lateinit var uploader: RecordingUploader

    override fun onCreate() {
        super.onCreate()
        schedulePeriodicUpload()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lifecycleScope.launch {
            uploadPendingRecordings()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Upload les enregistrements en attente
     */
    private suspend fun uploadPendingRecordings() {
        val pendingUploads = database.callRecordingDao().getPendingUploads()

        for (recording in pendingUploads) {
            try {
                uploader.uploadRecording(recording)
                database.callRecordingDao().markAsUploaded(
                    recording.id!!,
                    CallStatus.UPLOADED,
                    System.currentTimeMillis()
                )
            } catch (e: Exception) {
                database.callRecordingDao().incrementUploadAttempts(
                    recording.id!!,
                    e.message ?: "Unknown error"
                )
            }
        }
    }

    /**
     * Programme l'upload périodique
     */
    private fun schedulePeriodicUpload() {
        val uploadWork = PeriodicWorkRequestBuilder<UploadWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "call_recording_upload",
            ExistingPeriodicWorkPolicy.KEEP,
            uploadWork
        )
    }
}

/**
 * Worker pour l'upload en arrière-plan
 */
class UploadWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    @Inject
    lateinit var database: CallRecordingDatabase

    @Inject
    lateinit var uploader: RecordingUploader

    override suspend fun doWork(): Result {
        return try {
            val pendingUploads = database.callRecordingDao().getPendingUploads()

            for (recording in pendingUploads) {
                try {
                    uploader.uploadRecording(recording)
                    database.callRecordingDao().markAsUploaded(
                        recording.id!!,
                        CallStatus.UPLOADED,
                        System.currentTimeMillis()
                    )
                } catch (e: Exception) {
                    database.callRecordingDao().incrementUploadAttempts(
                        recording.id!!,
                        e.message ?: "Unknown error"
                    )
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
