package com.enterprise.callrecorder.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * Entité CallRecording pour la base de données
 */
@Entity(tableName = "call_recordings")
data class CallRecording(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    
    // 🔴 Identification
    val deviceId: String,
    val callLogId: String? = null,
    val phoneNumber: String,
    
    // 🔴 Type et timing
    val callType: CallType,
    val startedAt: Long,
    val endedAt: Long? = null,
    val duration: Long? = null,
    
    // 🔴 Fichier
    val fileName: String? = null,
    val fileSize: Long? = null,
    val storageKey: String? = null,
    
    // 🔴 Statut
    val status: CallStatus = CallStatus.RECORDING,
    val uploadAttempts: Int = 0,
    val lastUploadError: String? = null,
    
    // 🔴 Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val uploadedAt: Long? = null,
    val deletedAt: Long? = null
)

/**
 * Type d'appel
 */
enum class CallType {
    INCOMING,
    OUTGOING,
    MISSED,
    REJECTED
}

/**
 * Statuts de l'enregistrement
 */
enum class CallStatus {
    RECORDING,          // En cours d'enregistrement
    PROCESSING,         // Traitement post-appel
    PENDING_UPLOAD,     // En attente d'upload
    UPLOADING,          // Upload en cours
    UPLOADED,           // Uploadé avec succès
    UPLOAD_FAILED,      // Erreur d'upload
    DELETED             // Supprimé
}
