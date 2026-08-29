package com.enterprise.callrecorder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.enterprise.callrecorder.service.CallMonitorService
import com.enterprise.callrecorder.service.RecordingUploadService

/**
 * Redémarre les services de surveillance d'appel et d'upload après
 * un redémarrage de l'appareil (nécessite RECEIVE_BOOT_COMPLETED,
 * déjà déclaré dans le manifest).
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val monitorIntent = Intent(context, CallMonitorService::class.java)
        val uploadIntent = Intent(context, RecordingUploadService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(monitorIntent)
            context.startForegroundService(uploadIntent)
        } else {
            context.startService(monitorIntent)
            context.startService(uploadIntent)
        }
    }
}
