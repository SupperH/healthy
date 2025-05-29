package com.example.healthmanagecenter.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.healthmanagecenter.data.converter.TimeListConverter
import com.example.healthmanagecenter.data.dao.AlertDao
import com.example.healthmanagecenter.data.dao.HealthRecordDao
import com.example.healthmanagecenter.data.dao.MedicationReminderDao
import com.example.healthmanagecenter.data.dao.UserDao
import com.example.healthmanagecenter.data.entity.AlertEntity
import com.example.healthmanagecenter.data.entity.HealthRecordEntity
import com.example.healthmanagecenter.data.entity.MedicationReminderEntity
import com.example.healthmanagecenter.data.entity.UserEntity
import com.example.healthmanagecenter.data.entity.DoctorFeedbackEntity
import com.example.healthmanagecenter.data.dao.DoctorFeedbackDao

@Database(
    entities = [
        UserEntity::class,
        HealthRecordEntity::class,
        AlertEntity::class,
        MedicationReminderEntity::class,
        DoctorFeedbackEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(TimeListConverter::class)
abstract class HealthDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun healthRecordDao(): HealthRecordDao
    abstract fun alertDao(): AlertDao
    abstract fun medicationReminderDao(): MedicationReminderDao
    abstract fun doctorFeedbackDao(): DoctorFeedbackDao

    companion object {
        @Volatile
        private var INSTANCE: HealthDatabase? = null

        fun getDatabase(context: Context): HealthDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HealthDatabase::class.java,
                    "health_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 