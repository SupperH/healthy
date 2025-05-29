package com.example.healthmanagecenter.viewmodel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthmanagecenter.data.dao.MedicationReminderDao
import com.example.healthmanagecenter.data.entity.MedicationReminderEntity
import com.example.healthmanagecenter.receiver.MedicationReminderReceiver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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

        timeList.forEach { timeStr ->
            val time = dateFormat.parse(timeStr)
            val calendar = Calendar.getInstance().apply {
                time?.let {
                    set(Calendar.HOUR_OF_DAY, it.hours)
                    set(Calendar.MINUTE, it.minutes)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                if (before(Calendar.getInstance())) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }

            val mainIntent = Intent(context, MedicationReminderReceiver::class.java).apply {
                putExtra("reminder_id", id)
                putExtra("medication_name", name)
                putExtra("instructions", instructions)
                putExtra("time", timeStr)
                putExtra("is_early", false)
            }
            val mainPendingIntent = PendingIntent.getBroadcast(
                context,
                (id.toString() + timeStr).hashCode(),
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                mainPendingIntent
            )

            val earlyCalendar = calendar.clone() as Calendar
            earlyCalendar.add(Calendar.MINUTE, -1)

            if (earlyCalendar.before(Calendar.getInstance())) {
                earlyCalendar.add(Calendar.DAY_OF_MONTH, 1)
            }

            val earlyIntent = Intent(context, MedicationReminderReceiver::class.java).apply {
                putExtra("reminder_id", id)
                putExtra("medication_name", name)
                putExtra("instructions", instructions)
                putExtra("time", timeStr)
                putExtra("is_early", true)
            }
            val earlyPendingIntent = PendingIntent.getBroadcast(
                context,
                (id.toString() + timeStr + "_early").hashCode(),
                earlyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                earlyCalendar.timeInMillis,
                earlyPendingIntent
            )
        }
    }

    private suspend fun cancelReminders(id: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val reminder = reminderDao.getReminderById(id)
        
        reminder?.timeList?.forEach { timeStr ->
            val mainIntent = Intent(context, MedicationReminderReceiver::class.java)
            val mainPendingIntent = PendingIntent.getBroadcast(
                context,
                (id.toString() + timeStr).hashCode(),
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(mainPendingIntent)

            val earlyIntent = Intent(context, MedicationReminderReceiver::class.java)
            val earlyPendingIntent = PendingIntent.getBroadcast(
                context,
                (id.toString() + timeStr + "_early").hashCode(),
                earlyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(earlyPendingIntent)
        }
    }
} 