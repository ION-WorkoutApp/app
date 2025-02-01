package com.ion606.workoutapp.logic

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale

object SimpleStorage {
    private const val PREFS_NAME = "simple_prefs"
    private const val STOP_THIS_WEEK_KEY = "stop_this_week"
    private const val WEEK_SAVED_KEY = "week_saved"

    // Saves both the flag and the current week number
    @RequiresApi(Build.VERSION_CODES.O)
    fun saveFlag(context: Context, value: Boolean) {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentWeek = getCurrentWeekNumber() // Get current week number

        sharedPrefs.edit()
            .putBoolean(STOP_THIS_WEEK_KEY, value)
            .putInt(WEEK_SAVED_KEY, currentWeek)
            .apply()
    }

    // Retrieves the stop flag (default is false if not set)
    fun getFlag(context: Context): Boolean {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPrefs.getBoolean(STOP_THIS_WEEK_KEY, false)
    }

    // Retrieves the week number when the flag was last set (default is 0)
    fun getWeekSaved(context: Context): Int {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPrefs.getInt(WEEK_SAVED_KEY, 0)
    }

    // Clears the stored flag (useful for resetting at the start of a new week)
    fun clearFlag(context: Context) {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .remove(STOP_THIS_WEEK_KEY)
            .remove(WEEK_SAVED_KEY)
            .apply()
    }

    // Utility function to get the current week number based on the locale
    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentWeekNumber(): Int {
        return LocalDate.now().get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear())
    }
}
