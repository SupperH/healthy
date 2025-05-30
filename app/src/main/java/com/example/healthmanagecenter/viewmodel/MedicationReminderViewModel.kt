package com.example.healthmanagecenter.viewmodel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthmanagecenter.data.dao.MedicationReminderDao
import com.example.healthmanagecenter.data.entity.MedicationReminderEntity
import com.example.healthmanagecenter.receiver.MedicationReminderReceiver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.Manifest

class MedicationReminderViewModel(
    private val reminderDao: MedicationReminderDao,
    private val context: Context
) : ViewModel() {

    val reminders: Flow<List<MedicationReminderEntity>> = reminderDao.getAllReminders()

    fun addReminder(name: String, instructions: String, timeList: List<String>) {
        viewModelScope.launch {
            val reminder = MedicationReminderEntity(
                name = name,
                instructions = instructions,
                timeList = timeList
            )
            val id = reminderDao.insertReminder(reminder)
            scheduleReminders(id, name, instructions, timeList)
        }
    }

    fun updateReminder(reminder: MedicationReminderEntity) {
        viewModelScope.launch {
            cancelReminders(reminder.id)
            reminderDao.updateReminder(reminder)
            scheduleReminders(reminder.id, reminder.name, reminder.instructions, reminder.timeList)
        }
    }

    fun deleteReminder(reminder: MedicationReminderEntity) {
        viewModelScope.launch {
            cancelReminders(reminder.id)
            reminderDao.deleteReminder(reminder)
        }
    }

    private fun scheduleReminders(id: Long, name: String, instructions: String, timeList: List<String>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        // Check for necessary permissions
        val postNotificationsGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Permissions not needed on older versions
        }

        // setAlarmClock does not require SCHEDULE_EXACT_ALARM permission, but needs to set full-screen Intent (if lock screen reminder is needed)
        // Here we only check POST_NOTIFICATIONS permission, because sending notifications requires it.
        if (!postNotificationsGranted) {
            Log.w("MedicationReminder", "Notification permission not granted. Cannot schedule reminder.")
            return
        }

        timeList.forEach { timeStr ->
            val time = dateFormat.parse(timeStr)
            val calendar = Calendar.getInstance().apply {
                time?.let {
                    set(Calendar.HOUR_OF_DAY, it.hours)
                    set(Calendar.MINUTE, it.minutes)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                // If the time has already passed today, schedule for tomorrow
                if (before(Calendar.getInstance())) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }

            val reminderIntent = Intent("com.example.healthmanagecenter.ACTION_SHOW_MEDICATION_REMINDER").apply {
                component = ComponentName(context.packageName, MedicationReminderReceiver::class.java.name)
                putExtra("reminder_id", id)
                putExtra("medication_name", name)
                putExtra("instructions", instructions)
                putExtra("time", timeStr)
                putExtra("is_early", false)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                (id.toString() + timeStr).hashCode(),
                reminderIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Add log to check SCHEDULE_EXACT_ALARM permission status
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val canScheduleExactAlarms = alarmManager.canScheduleExactAlarms()
                Log.d("MedicationReminder", "Can schedule exact alarms (API ${Build.VERSION.SDK_INT}): $canScheduleExactAlarms")
            } else {
                 Log.d("MedicationReminder", "SCHEDULE_EXACT_ALARM not required below API 31 (API ${Build.VERSION.SDK_INT})")
            }

            // Use setAlarmClock to schedule alarm
            // Optionally add a fullScreenIntent for lock screen reminders
            val alarmClockInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                AlarmManager.AlarmClockInfo(calendar.timeInMillis, null)
            } else {
                // Fallback for older versions if needed, though setAlarmClock is API 21+
                null
            }

            if (alarmClockInfo != null) {
                try {
                    alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                    Log.d("MedicationReminder", "Scheduled alarm clock for ID: $id at ${calendar.time}")
                } catch (e: SecurityException) {
                    Log.e("MedicationReminder", "SecurityException scheduling alarm clock: ${e.message}")
                }
            } else {
                 // Handle older API versions if necessary
                 Log.w("MedicationReminder", "setAlarmClock not available on this API level.")
                 // Optionally, use setExactAndAllowWhileIdle as a fallback on older APIs
                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                      try {
                           alarmManager.setExactAndAllowWhileIdle(
                               AlarmManager.RTC_WAKEUP,
                               calendar.timeInMillis,
                               pendingIntent
                           )
                            Log.d("MedicationReminder", "Scheduled exact and allow while idle alarm for ID: $id at ${calendar.time}")
                      } catch (e: SecurityException) {
                            Log.e("MedicationReminder", "SecurityException scheduling exact alarm: ${e.message}")
                      }
                 } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                      // setExact on older APIs
                      try {
                           alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                           Log.d("MedicationReminder", "Scheduled exact alarm for ID: $id at ${calendar.time}")
                      } catch (e: SecurityException) {
                           Log.e("MedicationReminder", "SecurityException scheduling exact alarm: ${e.message}")
                      }
                 }
            }
            
            // Note: setAlarmClock does not support early alarms directly like setExactAndAllowWhileIdle
            // If an early alarm is still needed, it would require a separate setExact or similar call,
            // potentially subject to the same background restrictions.
        }
    }

    private suspend fun cancelReminders(id: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val reminder = reminderDao.getReminderById(id)

        reminder?.timeList?.forEach { timeStr ->
            val reminderIntent = Intent("com.example.healthmanagecenter.ACTION_SHOW_MEDICATION_REMINDER")
            reminderIntent.component = ComponentName(context.packageName, MedicationReminderReceiver::class.java.name)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                (id.toString() + timeStr).hashCode(),
                reminderIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            // Cancel the alarm clock
            alarmManager.cancel(pendingIntent)
            Log.d("MedicationReminder", "Cancelled alarm clock for ID: ${id}")

            // If you were also setting an early alarm with setExact, you would need to cancel it here as well.
        }
    }
} 