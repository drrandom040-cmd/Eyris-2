package com.elsewhere.eyris.data.local

import androidx.room.TypeConverter
import com.elsewhere.eyris.domain.models.LeadStatus

class Converters {
    @TypeConverter
    fun fromStatus(status: LeadStatus): String {
        return status.name
    }

    @TypeConverter
    fun toStatus(value: String): LeadStatus {
        return LeadStatus.valueOf(value)
    }
}
