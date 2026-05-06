package com.elsewhere.eyris.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.elsewhere.eyris.data.local.dao.ContactedDao
import com.elsewhere.eyris.data.local.dao.LeadDao
import com.elsewhere.eyris.data.local.entities.ContactedEntity
import com.elsewhere.eyris.data.local.entities.LeadEntity

@Database(entities = [LeadEntity::class, ContactedEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val leadDao: LeadDao
    abstract val contactedDao: ContactedDao
}
