package com.ion606.workoutapp.screens.logs

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.lazy.LazyListState
import com.google.gson.Gson
import com.ion606.workoutapp.dataObjects.ParsedWorkoutResponse
import org.json.JSONArray
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale


// helper functions for lazy list states
internal fun LazyListState.reachedBottom(buffer: Int = 1): Boolean {
    val lastVisibleItem = this.layoutInfo.visibleItemsInfo.lastOrNull()
    return lastVisibleItem?.index != 0 && lastVisibleItem?.index == this.layoutInfo.totalItemsCount - buffer
}

internal fun LazyListState.reachedTop(buffer: Int = 0): Boolean {
    val firstVisibleItem = this.layoutInfo.visibleItemsInfo.firstOrNull()
    return firstVisibleItem?.index == buffer
}


fun parseWorkoutResponse(json: String): ParsedWorkoutResponse? {
    return try {
        val gson = Gson()
        gson.fromJson(json, ParsedWorkoutResponse::class.java)
    } catch (e: Exception) {
        Log.e("LogScreen", "Parsing error: ${e.message}")
        e.printStackTrace()
        null
    }
}

@SuppressLint("NewApi")
fun formatTimestamp(timestamp: String, returnTime: Boolean = false): String {
    // parse the UTC timestamp
    val zonedDateTime = ZonedDateTime.parse(timestamp)

    // convert to local time zone (last time I forgot the `ZoneId.systemDefault())`)
    val localDateTime = zonedDateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()

    // determine the default date format
    val defaultDateFormat = (DateFormat.getDateInstance(
        DateFormat.SHORT, Locale.getDefault()
    ) as SimpleDateFormat).toPattern()

    // define the formatter based on the returnTime flag
    val formatter = if (returnTime) {
        DateTimeFormatter.ofPattern("HH:mm:ss")
    } else {
        DateTimeFormatter.ofPattern(defaultDateFormat)
    }

    return localDateTime.format(formatter)
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

