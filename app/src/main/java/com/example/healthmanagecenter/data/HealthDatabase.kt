package com.example.healthmanagecenter.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 6,
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
                .addMigrations(MIGRATION_4_5, MIGRATION_5_6)
                .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE users ADD COLUMN email TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE users ADD COLUMN dateOfBirth INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Since the schema is already updated in MIGRATION_4_5, nothing needs to be done here.
            }
        }
    }
} 