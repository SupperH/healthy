package com.example.healthmanagecenter.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.healthmanagecenter.data.dao.AlertDao
import com.example.healthmanagecenter.data.dao.HealthRecordDao
import com.example.healthmanagecenter.data.dao.MedicationReminderDao
import com.example.healthmanagecenter.data.dao.UserDao
import com.example.healthmanagecenter.data.entity.AlertEntity
import com.example.healthmanagecenter.data.entity.HealthRecordEntity
import com.example.healthmanagecenter.data.entity.MedicationReminderEntity
import com.example.healthmanagecenter.data.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        HealthRecordEntity::class,
        AlertEntity::class,
        MedicationReminderEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class HealthDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun healthRecordDao(): HealthRecordDao
    abstract fun alertDao(): AlertDao
    abstract fun medicationReminderDao(): MedicationReminderDao

    companion object {
        @Volatile
        private var INSTANCE: HealthDatabase? = null

        fun getDatabase(context: Context): HealthDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HealthDatabase::class.java,
                    "health_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
} 