package com.example.healthmanagecenter.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.healthmanagecenter.R
import com.example.healthmanagecenter.MainActivity

class MedicationReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("MedicationReminderReceiver", "MedicationReminderReceiver onReceive START")
        Log.d("MedicationReminderReceiver", "BroadcastReceiver onReceive triggered.")

        val reminderId = intent.getLongExtra("reminder_id", -1)
        val medicationName = intent.getStringExtra("medication_name") ?: return
        val instructions = intent.getStringExtra("instructions") ?: return
        val time = intent.getStringExtra("time") ?: return
        val isEarly = intent.getBooleanExtra("is_early", false)

        Log.d("MedicationReminderReceiver", "Received reminder data: ID=$reminderId, Name=$medicationName, Time=$time, isEarly=$isEarly")

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "用药提醒", // 中文频道名称
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "用药提醒通知渠道" // 中文描述
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create intent for notification tap action
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId.toInt() + if (isEarly) 10000 else 0, // 使用不同的requestCode区分主闹钟和提前闹钟
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val notificationTitle = if (isEarly) "用药提醒 (提前)" else "用药提醒"
        val notificationContent = "${medicationName} - 服用时间: ${time}\n说明: ${instructions}"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_medication) // 确保 ic_medication 图标存在
            .setContentTitle(notificationTitle)
            .setContentText(notificationContent)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(notificationContent))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            notificationManager.notify(reminderId.toInt() + if (isEarly) 10000 else 0, notification) // 使用不同的notificationId区分
            Log.d("MedicationReminderReceiver", "Medication reminder notification sent for ID: ${reminderId}")
        } catch (e: SecurityException) {
            Log.e("MedicationReminderReceiver", "SecurityException sending medication reminder notification: ${e.message}")
        }
    }

    companion object {
        const val CHANNEL_ID = "medication_reminder_channel"
    }
} 