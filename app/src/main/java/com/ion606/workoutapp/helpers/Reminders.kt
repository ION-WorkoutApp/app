package com.ion606.workoutapp.helpers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ion606.workoutapp.R
import com.ion606.workoutapp.logic.SimpleStorage
import com.ion606.workoutapp.managers.DataManager
import com.ion606.workoutapp.managers.SyncManager
import com.ion606.workoutapp.managers.UserManager
import com.ion606.workoutapp.screens.logs.parseTimestamps
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.TimeUnit

class WorkoutReminderWorker(
    context: Context, params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val CHANNEL_ID = "workout_reminder_channel"
        const val NOTIFICATION_ID = 1
    }

    /**
     * Filters workout dates to count workouts completed in the current week
     * and calculates days left in the week.
     *
     * @param dates List of ZonedDateTime representing workout dates.
     * @return Pair containing the count of workouts completed and days left in the week.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun analyzeCurrentWeek(dates: List<ZonedDateTime>): Pair<Int, Int> {
        val now = ZonedDateTime.now(ZoneId.systemDefault())

        // get the previous (or current) Sunday
        val startOfWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
            .truncatedTo(ChronoUnit.DAYS)
        // the end of the week is Saturday
        val endOfWeek = startOfWeek.plusDays(6)

        // count workouts completed from startOfWeek up to now (inclusive)
        val workoutsCompleted = dates.count { !it.isBefore(startOfWeek) && !it.isAfter(now) }

        // calculate days left in the week (including today)
        val daysLeft = ChronoUnit.DAYS.between(now.truncatedTo(ChronoUnit.DAYS), endOfWeek) + 1

        return Pair(workoutsCompleted, daysLeft.toInt())
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        return try {
            Log.d("WorkoutReminderWorker", "Running workout reminder worker")

            // general vals
            val syncManager = SyncManager("", applicationContext)
            val dataManager = DataManager(applicationContext, syncManager)
            val userManager = UserManager(applicationContext, dataManager, syncManager)

            if (!dataManager.login().success) return Result.failure()

            // Fetch workout dates from the server or database
            val response = withContext(Dispatchers.IO) {
                syncManager.sendData(
                    path = "workouts/workoutdates", method = "GET", payload = emptyMap()
                )
            }

            Log.d("WorkoutReminderWorker", "Response: $response")

            val workoutDates = parseTimestamps(response.second.toString())
            val (workoutsCompleted, daysLeft) = analyzeCurrentWeek(workoutDates)

            val userResponse = userManager.fetchUserData()
            if (userResponse is UserManager.FetchUserDataResult.Error) return Result.failure()

            val user = (userResponse as UserManager.FetchUserDataResult.Success).data
                ?: return Result.failure()

            Log.d(
                "WorkoutReminderWorker",
                "Workouts completed: $workoutsCompleted || Days left: $daysLeft || Required: ${user.generalPreferences.workoutFrequency}"
            )

            val requiredWorkouts = user.generalPreferences.workoutFrequency

            // enough workouts completed
            if (workoutsCompleted >= requiredWorkouts || !user.notifications.remindersEnabled) return Result.success()

            // if the user does have it enabled, check the flags
            val stopThisWeek = SimpleStorage.getFlag(applicationContext)
            val currentWeek = SimpleStorage.getCurrentWeekNumber()

            if (SimpleStorage.getWeekSaved(applicationContext) == currentWeek) {
                println("Flag is for the current week")
            } else {
                SimpleStorage.clearFlag(applicationContext)
                SimpleStorage.saveFlag(applicationContext, false)
            }

            if (stopThisWeek) return Result.failure()

            // workouts remaining
            val workoutsRemaining = requiredWorkouts - workoutsCompleted

            // days left equal workouts remaining
            if (daysLeft == workoutsRemaining) {
                sendNotification(
                    "Workout Reminder",
                    "You need to work out every remaining day (${workoutsRemaining} days) to meet your weekly goal!"
                )
            } else if (daysLeft < workoutsRemaining) {
                sendNotification(
                    "Workout Reminder",
                    "Looks like you didn't complete all your workouts this week, better luck next time"
                )
                SimpleStorage.saveFlag(applicationContext, true)
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    /**
     * Sends a notification to the user.
     *
     * @param title The title of the notification.
     * @param message The message content of the notification.
     */
    private suspend fun sendNotification(title: String, message: String) =
        withContext(Dispatchers.IO) {
            val notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Create notification channel if necessary
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID, "Workout Reminders", NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notifications to remind users to complete workouts"
                }
                notificationManager.createNotificationChannel(channel)
            }

            val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_app_icon) // Ensure this icon exists in your drawable resources
                .setContentTitle(title).setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true) // Dismiss notification when tapped
                .build()

            notificationManager.notify(NOTIFICATION_ID, notification)
        }
}

class WorkoutTimerReminder(
    context: Context, params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        withContext(Dispatchers.IO) {
            val notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Create notification channel if necessary
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    WorkoutReminderWorker.CHANNEL_ID,
                    "Workout Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notifications to remind users to complete workouts"
                }
                notificationManager.createNotificationChannel(channel)
            }

            val notification = NotificationCompat.Builder(
                applicationContext, WorkoutReminderWorker.CHANNEL_ID
            )
                .setSmallIcon(R.drawable.ic_app_icon) // Ensure this icon exists in your drawable resources
                .setContentTitle("Workout Timeout")
                .setContentText("Your workout has timed out. Please complete it!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true) // Dismiss notification when tapped
                .build()

            notificationManager.notify(WorkoutReminderWorker.NOTIFICATION_ID, notification)
        }
        return Result.success()
    }
}


fun scheduleWorkoutReminder(context: Context, isWorkoutTimeoutReminder: Boolean = false) {
    val constraints = Constraints.Builder().build()

    if (isWorkoutTimeoutReminder) {
        // singleton version for testing
        val workRequest = if (isBatteryLow(context) || isPowerSaveMode(context)) {
            OneTimeWorkRequestBuilder<WorkoutTimerReminder>().setConstraints(constraints)
                .setInitialDelay(1, TimeUnit.HOURS).build()
        } else {
            OneTimeWorkRequestBuilder<WorkoutTimerReminder>().setConstraints(constraints)
                .setInitialDelay(30, TimeUnit.MINUTES).build()
        }
        WorkManager.getInstance(context)
            .enqueueUniqueWork("WorkoutTimeoutReminder", ExistingWorkPolicy.KEEP, workRequest)
        return
    }

    // Create a periodic work request
    val workRequest =
        PeriodicWorkRequestBuilder<WorkoutReminderWorker>(1, TimeUnit.DAYS).setConstraints(
            constraints
        ).build()

    // Schedule the work uniquely to avoid duplicate reminders
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "WorkoutReminder", ExistingPeriodicWorkPolicy.KEEP, workRequest
    )
}

fun cancelWorkoutReminder(context: Context) {
    // cancel the uniquely named work so that no duplicate reminders remain
    WorkManager.getInstance(context).cancelUniqueWork("WorkoutReminder")
}
