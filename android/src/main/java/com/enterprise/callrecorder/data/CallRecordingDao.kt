package com.enterprise.callrecorder.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.enterprise.callrecorder.model.CallRecording
import com.enterprise.callrecorder.model.CallStatus
import kotlinx.coroutines.flow.Flow

/**
 * DAO pour les enregistrements d'appels
 */
@Dao
interface CallRecordingDao {

    @Insert
    suspend fun insert(callRecording: CallRecording): Long

    @Update
    suspend fun update(callRecording: CallRecording)

    @Query("UPDATE call_recordings SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: CallStatus)

    @Query("UPDATE call_recordings SET uploadAttempts = uploadAttempts + 1, lastUploadError = :error WHERE id = :id")
    suspend fun incrementUploadAttempts(id: Long, error: String)

    @Query("UPDATE call_recordings SET status = :status, uploadedAt = :timestamp WHERE id = :id")
    suspend fun markAsUploaded(id: Long, status: CallStatus, timestamp: Long)

    @Query("SELECT * FROM call_recordings WHERE id = :id")
    suspend fun getById(id: Long): CallRecording?

    @Query("SELECT * FROM call_recordings WHERE status = :status")
    suspend fun getByStatus(status: CallStatus): List<CallRecording>

    @Query("SELECT * FROM call_recordings WHERE status = 'PENDING_UPLOAD' OR status = 'UPLOAD_FAILED' ORDER BY createdAt DESC")
    suspend fun getPendingUploads(): List<CallRecording>

    @Query("SELECT * FROM call_recordings WHERE deviceId = :deviceId ORDER BY startedAt DESC")
    fun getByDeviceId(deviceId: String): Flow<List<CallRecording>>

    @Query("SELECT * FROM call_recordings WHERE phoneNumber = :phoneNumber ORDER BY startedAt DESC")
    fun getByPhoneNumber(phoneNumber: String): Flow<List<CallRecording>>

    @Query("SELECT * FROM call_recordings WHERE startedAt >= :startDate AND startedAt <= :endDate ORDER BY startedAt DESC")
    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<CallRecording>>

    @Query("SELECT * FROM call_recordings ORDER BY startedAt DESC LIMIT :limit")
    fun getRecent(limit: Int = 50): Flow<List<CallRecording>>

    @Query("DELETE FROM call_recordings WHERE deletedAt IS NOT NULL AND deletedAt < :cutoffTime")
    suspend fun deleteOldRecords(cutoffTime: Long)

    @Query("SELECT COUNT(*) FROM call_recordings WHERE status = :status")
    suspend fun countByStatus(status: CallStatus): Int
}
