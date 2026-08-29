package com.enterprise.callrecorder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.enterprise.callrecorder.service.CallMonitorService

/**
 * Reçoit les changements d'état téléphonique système et s'assure que
 * CallMonitorService tourne. La logique de détection d'appel elle-même
 * vit dans CallMonitorService (TelephonyCallback / PhoneStateListener) ;
 * ce receiver sert de filet de sécurité pour redémarrer le service s'il a
 * été tué par le système.
 */
class CallStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != "android.intent.action.PHONE_STATE") return

        val serviceIntent = Intent(context, CallMonitorService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
