package com.ion606.workoutapp.screens.logs

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.derivedStateOf
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
import com.ion606.workoutapp.dataObjects.ParsedActiveExercise
import com.ion606.workoutapp.dataObjects.ParsedExercise
import com.ion606.workoutapp.dataObjects.ParsedWorkoutResponse
import com.ion606.workoutapp.elements.CreateWorkoutLogDropdown
import com.ion606.workoutapp.helpers.Alerts
import com.ion606.workoutapp.helpers.Alerts.Companion.CreateAlertDialog
import com.ion606.workoutapp.helpers.convertSecondsToTimeString
import com.ion606.workoutapp.managers.SyncManager
import com.ion606.workoutapp.managers.UserManager
import com.ion606.workoutapp.screens.WorkoutBottomBar
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DarkThemeWorkoutResponse(
    sm: SyncManager, userManager: UserManager, navController: NavHostController, context: Context
) {
    val listState = rememberLazyListState()

    // initial response state
    var response by remember { mutableStateOf<ParsedWorkoutResponse?>(null) }
    var error by remember { mutableStateOf("") }

    val tt = ZonedDateTime.now();
    var workoutDate by remember { mutableStateOf(getLocalDayRangeInUTC(tt.year, tt.monthValue, tt.dayOfMonth)) }

    val pageManager by remember {
        mutableStateOf(object {
            var currentPage = 0
            var previousPage = 0
            var nextPage = 1
            var hasNextPage = true
        })
    }

    // flag to prevent multiple requests at the same time
    var isLoading by remember { mutableStateOf(false) }
    var isLoadingDay by remember { mutableStateOf(false) }
    var dates by remember { mutableStateOf<List<ZonedDateTime>>(emptyList()) }

    // fetch initial workouts
    LaunchedEffect(workoutDate) {
        if (!isLoading) {
            isLoading = true
            isLoadingDay = true // needs to be different to avoid an infinite loop

            val result = sm.sendData(
                emptyMap(),
                path = "workouts/workouts?pagenum=${pageManager.currentPage}&date=$workoutDate",
                method = "GET"
            )
            if (result.first) {
                val parsedResponse = parseWorkoutResponse(result.second.toString())
                if (parsedResponse != null) {
                    response = parsedResponse
                } else {
                    error = "Failed to parse initial workout response"
                }
            } else error = result.second.toString()

            if (response != null && dates.isEmpty()) {
                val r2 = sm.sendData(
                    emptyMap(), path = "workouts/workoutdates", method = "GET"
                )

                if (r2.first) dates = parseTimestamps(r2.second.toString())
                else error = r2.second.toString()
            }

            isLoading = false
            isLoadingDay = false
        }
    }

    Scaffold(bottomBar = { WorkoutBottomBar(navController, 1) }, topBar = {
        LogTopBar(dates) { workoutDate = it }
    }) { innerPadding ->
        when {
            (response == null || isLoadingDay) -> {
                // loading state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(Color(0xFF121212)), contentAlignment = Alignment.Center
                ) {
                    Text(text = "Loading workouts...", color = Color.Gray, fontSize = 18.sp)
                }
            }

            response?.workouts.isNullOrEmpty() -> {
                // no workouts available
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(Color(0xFF121212)), contentAlignment = Alignment.Center
                ) {
                    Text(text = "No workouts available", color = Color.Gray, fontSize = 18.sp)
                }
            }

            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF121212))
                        .padding(innerPadding)
                ) {
                    LazyColumn(
                        state = listState, modifier = Modifier.fillMaxSize()
                    ) {
                        if (!response?.workouts.isNullOrEmpty()) {
                            items(response!!.workouts) { workout ->
                                WorkoutCard(workout, userManager, sm, navController, context)
                            }
                        }
                        else Log.d("WORKOUTS", "No workouts available for $workoutDate")
                    }

                    val reachedBottom by remember { derivedStateOf { listState.reachedBottom() } }
                    val reachedTop by remember { derivedStateOf { listState.reachedTop() } }

                    // load more workouts when reaching the bottom
                    LaunchedEffect(reachedBottom) {
                        if (reachedBottom && pageManager.hasNextPage && !isLoading) {
                            isLoading = true
                            val result = sm.sendData(
                                emptyMap(),
                                path = "workouts/workouts?pagenum=${pageManager.nextPage}",
                                method = "GET"
                            )
                            if (result.first) {
                                val parsedResponse = parseWorkoutResponse(result.second.toString())
                                pageManager.hasNextPage =
                                    (parsedResponse?.workouts?.isNotEmpty() == true)

                                if (parsedResponse != null && parsedResponse.workouts.isNotEmpty()) {
                                    pageManager.previousPage = pageManager.currentPage
                                    pageManager.currentPage = pageManager.nextPage
                                    pageManager.nextPage++
                                    response = response!!.copy(
                                        workouts = response!!.workouts + parsedResponse.workouts
                                    )
                                } else {
                                    error = "Failed to parse response"
                                }
                            } else {
                                error = result.second.toString()
                            }
                            isLoading = false
                        }
                    }

                    // handle top refresh (if needed)
                    LaunchedEffect(reachedTop) {
                        if (reachedTop && pageManager.currentPage > 0 && !isLoading) {
                            isLoading = true
                            val result = sm.sendData(
                                emptyMap(),
                                path = "workouts/workouts?pagenum=${pageManager.previousPage}",
                                method = "GET"
                            )
                            if (result.first) {
                                val parsedResponse = parseWorkoutResponse(result.second.toString())
                                if (parsedResponse != null) {
                                    pageManager.currentPage = pageManager.previousPage
                                    response = response!!.copy(
                                        workouts = parsedResponse.workouts + response!!.workouts
                                    )
                                } else {
                                    error = "Failed to parse response"
                                }
                            } else {
                                error = result.second.toString()
                            }
                            isLoading = false
                        }
                    }
                }
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
                            ), path = "workouts/savedworkouts"
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
                    modifier = Modifier.padding(16.dp)
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
                        title = "New Workout Time", context = context, isTimeInput = true
                    ) {
                        Log.d("EditTime", "New Workout Time: $it")
                        scope.launch {
                            val r = syncManager.sendData(
                                mapOf("workoutId" to workout.id, "newTime" to it.toString()),
                                path = "workouts/workout",
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
        AnimatedVisibility(visible = descriptionVisible,
            enter = slideInVertically(initialOffsetY = { 0 }),
            exit = slideOutVertically(targetOffsetY = { 0 })
        ) {
            // Display description text
            Text(
                text = exercise.description,
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
        @RequiresApi(Build.VERSION_CODES.O)
        @Composable
        fun CreateScreen(
            userManager: UserManager,
            syncManager: SyncManager,
            navController: NavHostController,
            context: Context
        ) {
            DarkThemeWorkoutResponse(
                syncManager, userManager, navController, context
            )
        }
    }
}