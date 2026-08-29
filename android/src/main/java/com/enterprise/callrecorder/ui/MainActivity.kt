package com.enterprise.callrecorder.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.enterprise.callrecorder.service.CallMonitorService
import com.enterprise.callrecorder.service.RecordingUploadService
import dagger.hilt.android.AndroidEntryPoint

/**
 * Écran unique : demande les permissions requises puis démarre les
 * services de surveillance/upload d'appels.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val requiredPermissions = mutableListOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CALL_LOG
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }.toTypedArray()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.all { it }) {
            startServices()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val missing = requiredPermissions.filter {
            checkSelfPermission(it) != android.content.pm.PackageManager.PERMISSION_GRANTED
        }

        if (missing.isEmpty()) {
            startServices()
        } else {
            permissionLauncher.launch(missing.toTypedArray())
        }
    }

    private fun startServices() {
        val monitorIntent = Intent(this, CallMonitorService::class.java)
        val uploadIntent = Intent(this, RecordingUploadService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(monitorIntent)
            startForegroundService(uploadIntent)
        } else {
            startService(monitorIntent)
            startService(uploadIntent)
        }
    }
}
