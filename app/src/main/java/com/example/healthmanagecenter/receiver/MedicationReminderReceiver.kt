package com.example.healthmanagecenter.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.healthmanagecenter.R
import com.example.healthmanagecenter.MainActivity

class MedicationReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra("reminder_id", -1)
        val medicationName = intent.getStringExtra("medication_name") ?: return
        val instructions = intent.getStringExtra("instructions") ?: return
        val time = intent.getStringExtra("time") ?: return

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Medication Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for medication reminders"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create intent for notification tap action
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId.toInt(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_medication)
            .setContentTitle("Medication Reminder: $medicationName")
            .setContentText("Time to take: $medicationName")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Time: $time\nInstructions: $instructions"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(reminderId.toInt(), notification)
    }

    companion object {
        const val CHANNEL_ID = "medication_reminder_channel"
    }
} 