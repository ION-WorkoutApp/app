package com.ion606.workoutapp.screens

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.gson.Gson
import com.ion606.workoutapp.dataObjects.ParsedActiveExercise
import com.ion606.workoutapp.dataObjects.ParsedExercise
import com.ion606.workoutapp.dataObjects.ParsedWorkoutResponse
import com.ion606.workoutapp.elements.CreateWorkoutLogDropdown
import com.ion606.workoutapp.helpers.Alerts
import com.ion606.workoutapp.helpers.Alerts.Companion.CreateAlertDialog
import com.ion606.workoutapp.helpers.convertSecondsToTimeString
import com.ion606.workoutapp.managers.SyncManager
import com.ion606.workoutapp.managers.UserManager
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


private const val TAG = "LogScreen"


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


@Composable
fun DarkThemeWorkoutResponse(
    syncManager: SyncManager,
    userManager: UserManager,
    response: ParsedWorkoutResponse,
    navController: NavHostController,
    context: Context
) {
    Scaffold(bottomBar = {
        WorkoutBottomBar(navController, 1)
    }) { innerPadding ->
        if (response.success && response.workouts.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF121212))
                    .padding(innerPadding)
            ) {
                LazyColumn {
                    items(response.workouts) { workout ->
                        WorkoutCard(workout, userManager, syncManager, navController, context)
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color(0xFF121212)), contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (response.success) "No workouts available" else "Failed to load workouts",
                    color = Color.Gray,
                    fontSize = 18.sp
                )
            }
        }
    }
}


@Composable
fun WorkoutCard(
    workout: ParsedActiveExercise,
    userManager: UserManager,
    syncManager: SyncManager,
    navController: NavHostController,
    context: Context
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)) // Dark card background
    ) {
        var visible by remember { mutableStateOf(true) }
        var errored by remember { mutableStateOf<String?>(null) }
        val scope = rememberCoroutineScope()

        val savedWorkout = remember { mutableListOf<ParsedActiveExercise>() }
        if (savedWorkout.isNotEmpty()) {
            var workoutName by remember { mutableStateOf<String?>(null) }
            var error by remember { mutableStateOf("") }

            CreateAlertDialog(
                "Enter saved workout name", context
            ) {
                if (!it.isNullOrEmpty()) workoutName = it
                else error = "Failed to read workout name"
            }

            if (error.isNotEmpty()) {
                CreateAlertDialog(
                    error, context
                ) {
                    error = ""
                }
            } else if (!workoutName.isNullOrEmpty()) {
                LaunchedEffect("saveworkoutsend") {
                    scope.launch {
                        val r = syncManager.sendData(
                            mapOf(
                                "workout" to mapOf(
                                    "exercises" to savedWorkout, "totalTime" to 0
                                ), "workoutname" to workoutName.toString()
                            ), path = "savedworkouts"
                        )

                        if (r.first) {
                            error = "Successfully saved workout"
                            savedWorkout.clear()
                        } else error = r.second.toString()

                        Log.d("SAVE RESULT", r.toString())
                    }
                }
            }
        }

        if (visible) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Workout on ${formatTimestamp(workout.createdAt)} at ${
                            formatTimestamp(
                                workout.createdAt, true
                            )
                        }",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE0E0E0)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Display exercises in the workout
                    var weightSum = 0
                    var setSum = 0
                    workout.exercises.forEach { exercise ->
                        ExerciseDetails(exercise)
                        setSum += if (exercise.timeBased) exercise.times?.size
                            ?: 0 else exercise.reps?.size ?: 0
                        weightSum += exercise.weight?.sumOf { it.value } ?: 0
                    }

                    // Display workout-specific details
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${workout.exercises.size} total exercises, $setSum total sets, ${weightSum}kg total weight",
                        fontSize = 14.sp,
                        color = Color(0xFFB0B0B0)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                var editTime by remember { mutableStateOf(false) }

                if (editTime) {
                    CreateAlertDialog(
                        title = "New Workout Time",
                        context = context,
                        isTimeInput = true
                    ) {
                        Log.d("EditTime", "New Workout Time: $it")
                        scope.launch {
                            val r = syncManager.sendData(
                                mapOf("workoutId" to workout.id, "newTime" to it.toString()),
                                path = "workout",
                                method = "PUT"
                            )

                            if (r.first) {
                                visible = false
                                navController.navigate("log")
                            } else errored = r.second.toString()

                            editTime = false
                        }
                    }
                }

                CreateWorkoutLogDropdown("...",
                    Modifier
                        .padding(16.dp)
                        .align(Alignment.TopEnd),
                    context,
                    { editTime = true },
                    {
                        scope.launch {
                            val r = userManager.deleteWorkout(workout)
                            if (r.first) {
                                visible = false;
                                navController.navigate("log")
                            } else errored = r.second.toString();
                        }
                    },
                    {
                        val toSave = workout.exercises.map {
                            mapOf(
                                "exercise" to it,
                                "sets" to it.sets,
                                "perset" to if (it.timeBased) it.times else it.reps
                            )
                        }

                        Log.d("SAVING", toSave.toString())
                    })
            }
        } else if (!errored.isNullOrEmpty()) {
            Alerts.ShowAlert({ errored = null }, "Failed to delete workout", errored!!);
        }
    }
}


@SuppressLint("NewApi")
fun formatTimestamp(timestamp: String, returnTime: Boolean = false): String {
    val zonedDateTime = ZonedDateTime.parse(timestamp)
    val formatter = DateTimeFormatter.ofPattern(if (returnTime) "hh:mm:ss" else "dd/MM/yy")
    return zonedDateTime.format(formatter)
}


@Composable
fun ExerciseDetails(exercise: ParsedExercise) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Title with emoji
        Text(
            text = "🏋️ ${exercise.title}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE0E0E0)
        )

        // Create an icon button to toggle description visibility
        var descriptionVisible by remember { mutableStateOf(false) }
        Row {
            IconButton(onClick = { descriptionVisible = !descriptionVisible }) {
                Icon(
                    imageVector = if (descriptionVisible) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (descriptionVisible) "Hide description" else "Show description",
                    tint = Color(0xFFE0E0E0),
                    modifier = Modifier.padding(top = 3.dp)
                )
            }

            Text(text = "💡", modifier = Modifier.padding(end = 4.dp, top = 10.dp))

            Text(
                text = if (descriptionVisible) "Hide description" else "Show description",
                fontSize = 15.sp,
                color = Color(0xFFE0E0E0),
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        // Slide in description when visible
        AnimatedVisibility(
            visible = descriptionVisible,
            enter = slideInVertically(initialOffsetY = { 0 }),
            exit = slideOutVertically(targetOffsetY = { 0 })
        ) {
            // Display description text
            Text(
                text = "${exercise.description}",
                fontSize = 12.sp,
                color = Color(0xFFB0B0B0),
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Main details in a compact format
        Text(
            text = "📖 Type: ${exercise.type} | 🎯 Body Part: ${exercise.bodyPart}\n🛠️ Equipment: ${exercise.equipment} | 🔥 Level: ${exercise.level}",
            fontSize = 12.sp,
            color = Color(0xFFB0B0B0),
            modifier = Modifier
                .padding(top = 4.dp)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Reps or time-based sets with emojis
        if (exercise.timeBased) {
            Text(
                text = "⏱️ Time-Based: ${exercise.times?.filter { it.isDone }?.size}/${exercise.times?.size} sets completed",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE0E0E0)
            )
            exercise.times?.forEachIndexed { index, time ->
                Text(
                    text = "  ${if (time.isDone) "✅" else "❌"}: ${time.value / 60}:${time.value % 60}(time.value)} | ${
                        exercise.weight?.getOrNull(
                            index
                        )?.value ?: 0
                    }kg | ${convertSecondsToTimeString(time.restTime)} rest",
                    fontSize = 12.sp,
                    color = Color(0xFFB0B0B0),
                    modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                )
            }
        } else {
            Text(
                text = "🔢 Reps-Based: ${exercise.reps?.filter { it.isDone }?.size}/${exercise.reps?.size} sets completed",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE0E0E0)
            )
            exercise.reps?.forEachIndexed { index, rep ->
                Text(
                    text = "  ${if (rep.isDone) "✅" else "❌"}: ${rep.value} reps | ${
                        exercise.weight?.getOrNull(
                            index
                        )?.value ?: 0
                    }kg | ${convertSecondsToTimeString(rep.restTime)} rest",
                    fontSize = 12.sp,
                    color = Color(0xFFB0B0B0),
                    modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}


class LogScreen {
    companion object {
        @Composable
        fun CreateScreen(
            userManager: UserManager,
            syncManager: SyncManager,
            navController: NavHostController,
            context: Context
        ) {
            val showLog = remember { mutableStateOf<ParsedWorkoutResponse?>(null) }

            if (showLog.value != null) {
                DarkThemeWorkoutResponse(
                    syncManager, userManager, showLog.value!!, navController, context
                )
            }

            val err = remember { mutableStateOf(false) }
            if (err.value) {
                Alerts.ShowAlert({
                    userManager.clearPreferences();
                    navController.navigate("login");
                    err.value = false;
                }, "Failed to load workouts", "Please log out and try again!")
            }

            LaunchedEffect("userlog") {
                val ulog = syncManager.sendData(emptyMap(), path = "workouts", method = "GET")
                try {
                    Log.d("LogScreen", "Raw response: ${ulog.second}")

                    if (ulog.second is String) {
                        val json = ulog.second.toString()
                        val parsedResponse = parseWorkoutResponse(json)

                        if (parsedResponse != null && parsedResponse.success) {
                            showLog.value = parsedResponse
                            Log.d("parsedResponse", "Parsed response: $parsedResponse")
                        } else {
                            err.value = true
                            Log.e("LogScreen", "Failed to parse workout response")
                        }
                    } else {
                        Log.e("LogScreen", "Unexpected response type: ${ulog.second!!::class.java}")
                    }
                } catch (e: Exception) {
                    Log.e("LogScreen", "Exception during processing: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }
}