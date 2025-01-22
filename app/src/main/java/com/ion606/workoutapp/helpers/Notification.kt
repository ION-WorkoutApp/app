package com.ion606.workoutapp.helpers

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.ion606.workoutapp.R


private const val TAG = "NotificationManager"


class MyBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "OPEN_APP") {
            Log.d(TAG, "Custom action received: OPEN_APP");
            // um.....open...open the app
        }
    }
}


class NotificationManager(private val context: Context) {
    private val CHANNEL_ID = "ion_workout_app_notif_channel";
    private val NOTIFICATION_ID = 1001;
    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 100;

    fun runInitialSetup() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ActivityCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermission(context as Activity);
        }

        createNotificationChannel();

        Log.d(TAG, "Notification channel created.");
    }

    /**
     * Create and register the notification channel on Android 8.0+.
     * Safe to call multiple times; only registered once.
     */
    private fun createNotificationChannel() {
        // Only needed on API 26+ (Oreo) and above.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "My Channel";
            val importance = NotificationManager.IMPORTANCE_HIGH;
            val channel = NotificationChannel(CHANNEL_ID, channelName, importance).apply {
                description = "ION Workout App Notifications";
                enableVibration(true);
            };

            val notificationManager =
                this.context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager;
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Request notification permission on Android 13+ (TIRAMISU).
     * For older versions, this is not required and does nothing.
     */
    private fun requestNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    activity, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.VIBRATE),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                );
            }
        }
    }

    /**
     * Build and send a notification with a big picture and a custom action.
     * Make sure you have already called createNotificationChannel() in your Application/Activity.
     */
    fun sendNotification(
        title: String,
        message: String,
        actionName: String = "OPEN_APP",
        iconResId: Int = R.drawable.ic_app_icon,
        actions: List<NotificationCompat.Action> = emptyList(),
        intents: List<Pair<String, String>> = emptyList()
    ) {
        // Check for POST_NOTIFICATIONS permission (Android 13+).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ActivityCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return // Permission not granted.
        }

        // Prepare a large icon bitmap from the provided resource ID.
        val largeIconBitmap = BitmapFactory.decodeResource(context.resources, iconResId)

        // Base intent to handle the notification's custom action.
        val actionIntent = Intent(context, MyBroadcastReceiver::class.java).apply {
            action = actionName
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intents.forEach { (key, value) -> putExtra(key, value) }
        }

        // PendingIntent for the custom action.
        val actionPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            actionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification.
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(iconResId) // Small notification icon.
            .setContentTitle(title) // Notification title.
            .setContentText(message) // Notification message.
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Notification priority.
            .setLargeIcon(largeIconBitmap) // Large icon.
            .setAutoCancel(true) // Dismiss notification on tap.
            .setStyle(
                NotificationCompat.BigPictureStyle().bigPicture(largeIconBitmap)
                    .bigLargeIcon(null as Bitmap?)
            )

        // Add each action dynamically.
        actions.forEach { action ->
            builder.addAction(action)
        }

        // Add the default action if no custom actions are provided.
        if (actions.isEmpty()) {
            builder.addAction(
                R.drawable.ic_log, // Replace with your default action icon.
                "Open App", // Action text.
                actionPendingIntent
            )
        }

        // Send the notification.
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    fun sendNotificationIfUnfocused(
        title: String,
        message: String,
        actionName: String = "OPEN_APP",
        iconResId: Int = R.drawable.ic_app_icon,
        actions: List<NotificationCompat.Action> = emptyList(),
        intents: List<Pair<String, String>> = emptyList()
    ) {
        // TODO: Implement this method to send a notification only if the app is not in the foreground
        sendNotification(title, message, actionName, iconResId, actions, intents)
    }
}
