package com.ion606.workoutapp.screens.logs

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.ion606.workoutapp.dataObjects.SavedWorkoutResponse
import org.json.JSONArray
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale


fun parseWorkoutResponse(json: String): SavedWorkoutResponse? {
    return try {
        val gson = Gson()
        gson.fromJson(json, SavedWorkoutResponse::class.java)
    } catch (e: Exception) {
        Log.e("LogScreen", "Parsing error: ${e.message}")
        e.printStackTrace()
        null
    }
}

fun parseSavedWorkoutResponse(json: String): SavedWorkoutResponse? {
    return try {
        val gson = Gson()
        gson.fromJson(json, SavedWorkoutResponse::class.java)
    } catch (e: Exception) {
        Log.e("LogScreen", "Parsing error: ${e.message}")
        e.printStackTrace()
        null
    }
}


@SuppressLint("NewApi")
fun formatTimestamp(timestamp: String, returnTime: Boolean = false): String {
    return try {
        // Define the formatter matching the log's timestamp format
        val instant = Instant.parse(timestamp) // Parses "2025-01-28T16:16:46.003Z"
        val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())


        // Determine the default date format
        val defaultDateFormat = (DateFormat.getDateInstance(
            DateFormat.SHORT, Locale.getDefault()
        ) as SimpleDateFormat).toPattern()

        // Define the formatter based on the returnTime flag
        val outputFormatter = if (returnTime) {
            DateTimeFormatter.ofPattern("HH:mm:ss")
        } else {
            DateTimeFormatter.ofPattern(defaultDateFormat)
        }

        localDateTime.format(outputFormatter)
    } catch (e: Exception) {
        Log.e("LogScreen", "Timestamp parsing error: ${e.message}")
        timestamp // Return the original timestamp in case of an error
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun parseTimestamps(jsonString: String): List<ZonedDateTime> {
    val timestamps = mutableListOf<ZonedDateTime>()
    try {
        val jsonArray = JSONArray(jsonString)
        val localZone = ZoneId.systemDefault()

        for (i in 0 until jsonArray.length()) {
            val timestamp = jsonArray.getString(i)
            val zonedDateTime = ZonedDateTime.parse(timestamp, DateTimeFormatter.ISO_ZONED_DATE_TIME)
                .withZoneSameInstant(localZone)
            timestamps.add(zonedDateTime)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return timestamps
}

@RequiresApi(Build.VERSION_CODES.O)
fun getDaysForMonth(
    timestamps: List<ZonedDateTime>,
    displayedYear: Int,
    displayedMonth: Int
): List<Int> {
    return timestamps.filter { date ->
        date.year == displayedYear && date.monthValue == displayedMonth
    }.map { it.dayOfMonth }
}
