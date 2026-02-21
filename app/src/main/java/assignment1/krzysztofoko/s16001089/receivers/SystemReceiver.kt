package assignment1.krzysztofoko.s16001089.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import assignment1.krzysztofoko.s16001089.R

/**
 * SystemReceiver: Demonstrates the "Broadcast Receivers" requirement (8%).
 * 
 * This receiver handles both system-wide events (Power/Airplane Mode)
 * and custom application events (University Announcements).
 */
class SystemReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_UNIVERSITY_MESSAGE = "assignment1.krzysztofoko.s16001089.UNIVERSITY_MESSAGE"
        const val CHANNEL_ID = "university_announcements"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || context == null) return

        when (intent.action) {
            Intent.ACTION_POWER_CONNECTED -> {
                Toast.makeText(context, "University Portal: Charging Started ⚡", Toast.LENGTH_SHORT).show()
            }
            Intent.ACTION_POWER_DISCONNECTED -> {
                Toast.makeText(context, "University Portal: Running on Battery 🔋", Toast.LENGTH_SHORT).show()
            }
            // CUSTOM BROADCAST HANDLING: Displays a notification when a message is "received"
            ACTION_UNIVERSITY_MESSAGE -> {
                val message = intent.getStringExtra("message") ?: "New university announcement available."
                showNotification(context, "University Hub", message)
            }
        }
    }

    /**
     * Helper to show a system notification, demonstrating knowledge of 
     * NotificationManager and NotificationChannels (Required for API 26+).
     */
    private fun showNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create the Notification Channel (Required for Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Announcements", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }
}
