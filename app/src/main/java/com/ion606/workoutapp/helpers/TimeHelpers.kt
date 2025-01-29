package com.ion606.workoutapp.helpers


import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ion606.workoutapp.managers.SyncManager
import kotlinx.coroutines.delay
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone


@Composable
private fun TimeInputField(
    value: String,
    onSubmit: (String) -> Unit,
    onChange: (String) -> Unit = {}, // to be run in addition to the input field text chang3e
    textStyle: TextStyle
) {
    var tempstr by remember { mutableStateOf(value) }
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .width(20.dp) // Adjust width to match Text size
            .height(20.dp) // Adjust height to match Text size
    ) {
        BasicTextField(
            value = tempstr,
            onValueChange = { newValue ->
                // allow empty input (backspace) and enforce length limit/digit-only constraint
                if (newValue.isEmpty() || (newValue.all { it.isDigit() } && newValue.length <= 2)) {
                    tempstr = newValue
                    onChange(newValue)
                }
            },
            textStyle = textStyle,
            modifier = Modifier
                .fillMaxSize()
                .onFocusChanged {
                    if (it.isFocused && tempstr == "00") tempstr = ""
                    else if (!it.isFocused && tempstr.isEmpty()) tempstr = "00"
                },
            singleLine = true,
            cursorBrush = Brush.verticalGradient(
                colors = listOf(Color.White, Color.Transparent)
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number, // show a numeric keyboard
                imeAction = ImeAction.Done // set the IME action to "Done"
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus() // clear focus when "Done" is pressed
                    onSubmit(tempstr) // trigger the submit action
                }
            ),
            maxLines = 1
        )
    }
}


@Composable
fun TimeInput(
    timeStr: String,
    onDismiss: () -> Unit,
    onTimeSelected: (Pair<Int, Int>) -> Unit
) {
    // Split the initial time string into hours and minutes
    val timeSplit = timeStr.split(":").map { it.trim() }
    var selectedMinute by remember { mutableStateOf(timeSplit.getOrNull(0) ?: "") }
    var selectedSecond by remember { mutableStateOf(timeSplit.getOrNull(1) ?: "") }

    // Text style to match both Text and InputField
    val textStyle = TextStyle(fontSize = 16.sp, color = Color.White)

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(0.dp)
    ) {
        Spacer(modifier = Modifier.width(10.dp))

        // Input field for hours
        TimeInputField(
            value = selectedMinute,
            onSubmit = { newMinute ->
                if (newMinute.all { it.isDigit() } && newMinute.toIntOrNull() in 0..23) {
                    if (newMinute == selectedMinute) onDismiss()
                    else onTimeSelected(Pair(newMinute.toInt(), selectedSecond.toInt()))
                    selectedMinute = newMinute
                }
            },
            onChange = { newMinute -> selectedMinute = newMinute },
            textStyle = textStyle
        )

        Text(" : ", style = textStyle) // Separator

        // Input field for minutes
        TimeInputField(
            value = selectedSecond,
            onSubmit = { newSecond ->
                if (newSecond.all { it.isDigit() } && newSecond.toIntOrNull() in 0..59) {
                    if (newSecond == selectedSecond) onDismiss()
                    else onTimeSelected(Pair(selectedMinute.toInt(), newSecond.toInt()))

                    selectedSecond = newSecond
                }
            },
            textStyle = textStyle,
            onChange = { newSecond -> selectedSecond = newSecond }
        )
    }

    // Action to return selected time in "HH:mm" format when dismissed
    DisposableEffect(Unit) {
        onDispose {
            onTimeSelected(Pair(selectedMinute.toInt(), selectedSecond.toInt()))
            onDismiss()
        }
    }
}

@Composable
fun CheckIfInDebugMode(sm: SyncManager, isInDebugMode: (Boolean) -> Unit) {
    // launchedEffect is triggered once and keeps the coroutine running as long as this composable is in the composition
    LaunchedEffect(Unit) {
        while (true) {
            val r = sm.sendData(emptyMap(), path = "isindebugmode", method = "HEAD");
            isInDebugMode(r.first)

            // wait for 5 minutes
            delay(5 * 60 * 1000L)
        }
    }
}

fun transformTimestampToDateString(timestamp: String?): String? {
    try {
        if (timestamp == null) return null

        // define the input format (ISO 8601 with milliseconds and 'Z' for UTC)
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        inputFormat.timeZone = TimeZone.getTimeZone("UTC") // use UTC

        val date = inputFormat.parse(timestamp) ?: return null

        // get the user's default date and time formats
        val dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault())
        val timeFormat = SimpleDateFormat.getTimeInstance(SimpleDateFormat.MEDIUM, Locale.getDefault())

        // format date and time according to user's locale
        return "${dateFormat.format(date)} at ${timeFormat.format(date)}"
    } catch (e: Exception) {
        Log.e("TimeHelpers", "Failed to transform timestamp to date string", e)
        return null
    }
}

fun isWithinPastMonth(dateString: String): Boolean {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    dateFormat.timeZone = TimeZone.getTimeZone("UTC") // use UTC

    return try {
        val date = dateFormat.parse(dateString) ?: return false // Convert string to Date

        val now = Date()
        val calendar = Calendar.getInstance().apply {
            time = now
            add(Calendar.MONTH, -1) // Move back one month in the jankiest way possible
        }

        val oneMonthAgo = calendar.time
        date in oneMonthAgo..now // Check if the date falls within the range
    } catch (e: Exception) {
        false // Return false if the date format is invalid
    }
}