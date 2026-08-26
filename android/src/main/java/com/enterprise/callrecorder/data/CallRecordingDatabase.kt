package com.enterprise.callrecorder.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.enterprise.callrecorder.model.CallRecording

/**
 * Base de données Room pour les enregistrements d'appels
 */
@Database(
    entities = [CallRecording::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class CallRecordingDatabase : RoomDatabase() {

    abstract fun callRecordingDao(): CallRecordingDao

    companion object {
        @Volatile
        private var INSTANCE: CallRecordingDatabase? = null

        fun getDatabase(context: Context): CallRecordingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CallRecordingDatabase::class.java,
                    "call_recording_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
}
