package com.enterprise.callrecorder.data

import androidx.room.TypeConverter
import com.enterprise.callrecorder.model.CallStatus
import com.enterprise.callrecorder.model.CallType

/**
 * Convertisseurs de types pour Room
 */
class Converters {

    @TypeConverter
    fun fromCallType(value: CallType?): String? = value?.name

    @TypeConverter
    fun toCallType(value: String?): CallType? = value?.let { CallType.valueOf(it) }

    @TypeConverter
    fun fromCallStatus(value: CallStatus?): String? = value?.name

    @TypeConverter
    fun toCallStatus(value: String?): CallStatus? = value?.let { CallStatus.valueOf(it) }
}
