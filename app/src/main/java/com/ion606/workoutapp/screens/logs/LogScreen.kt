package com.ion606.workoutapp.screens.logs

import android.content.Context
import android.os.Build
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
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.ion606.workoutapp.dataObjects.ExerciseMeasureType
import com.ion606.workoutapp.dataObjects.ParsedExercise
import com.ion606.workoutapp.dataObjects.SavedWorkoutResponse
import com.ion606.workoutapp.dataObjects.Workout
import com.ion606.workoutapp.dataObjects.convertActiveExerciseToParsed
import com.ion606.workoutapp.helpers.convertSecondsToTimeString
import com.ion606.workoutapp.managers.SyncManager
import com.ion606.workoutapp.managers.UserManager
import com.ion606.workoutapp.screens.WorkoutBottomBar
import com.ion606.workoutapp.screens.activeExercise.SuperSet
import java.time.ZonedDateTime

// Utility Extensions
fun LazyListState.reachedBottom(buffer: Int = 1): Boolean {
    val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
    return lastVisibleItem != null && lastVisibleItem.index >= layoutInfo.totalItemsCount - buffer
}

fun LazyListState.reachedTop(buffer: Int = 0): Boolean {
    val firstVisibleItem = layoutInfo.visibleItemsInfo.firstOrNull()
    return firstVisibleItem?.index == buffer
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DarkThemeWorkoutResponse(
    sm: SyncManager,
    userManager: UserManager,
    navController: NavHostController,
    context: Context
) {
    val listState = rememberLazyListState()

    // Initial response state
    var response by remember { mutableStateOf<SavedWorkoutResponse?>(null) }
    var error by remember { mutableStateOf("") }

    val tt = ZonedDateTime.now()
    var workoutDate by remember {
        mutableStateOf(
            getLocalDayRangeInUTC(
                tt.year,
                tt.monthValue,
                tt.dayOfMonth
            )
        )
    }

    val pageManager = remember {
        object {
            var currentPage = 0
            var previousPage = 0
            var nextPage = 1
            var hasNextPage = true
        }
    }

    // Flags to prevent multiple requests at the same time
    var isLoading by remember { mutableStateOf(false) }
    var isLoadingDay by remember { mutableStateOf(false) }
    var dates by remember { mutableStateOf<List<ZonedDateTime>>(emptyList()) }
    val isMinimalist by userManager.isMinimalistModeFlow.collectAsState(initial = false)

    // Fetch initial workouts
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
                val parsedResponse = parseSavedWorkoutResponse(result.second.toString())
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

    Scaffold(
        bottomBar = { WorkoutBottomBar(navController, 1) },
        topBar = {
            LogTopBar(dates) { workoutDate = it }
        }
    ) { innerPadding ->
        when {
            (response == null || isLoadingDay) -> {
                // Loading state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(Color(0xFF121212)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Loading workouts...", color = Color.Gray, fontSize = 18.sp)
                }
            }

            response?.workouts.isNullOrEmpty() -> {
                // No workouts available
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(Color(0xFF121212)),
                    contentAlignment = Alignment.Center
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
                        items(response!!.workouts) { workout ->
                            if (isMinimalist) {
                                MinimalistWorkoutCard(workout)
                            } else {
                                WorkoutCard(workout, userManager)
                            }

                        }
                    }

                    val reachedBottom by remember { derivedStateOf { listState.reachedBottom() } }
                    val reachedTop by remember { derivedStateOf { listState.reachedTop() } }

                    // Load more workouts when reaching the bottom
                    LaunchedEffect(reachedBottom) {
                        if (reachedBottom && pageManager.hasNextPage && !isLoading) {
                            isLoading = true
                            val result = sm.sendData(
                                emptyMap(),
                                path = "workouts/workouts?pagenum=${pageManager.nextPage}",
                                method = "GET"
                            )
                            if (result.first) {
                                val parsedResponse = parseSavedWorkoutResponse(result.second.toString())
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

                    // Handle top refresh (if needed)
                    LaunchedEffect(reachedTop) {
                        if (reachedTop && pageManager.currentPage > 0 && !isLoading) {
                            isLoading = true
                            val result = sm.sendData(
                                emptyMap(),
                                path = "workouts/workouts?pagenum=${pageManager.previousPage}",
                                method = "GET"
                            )
                            if (result.first) {
                                val parsedResponse = parseSavedWorkoutResponse(result.second.toString())
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
    workout: Workout,
    userManager: UserManager
) {
    val isMinimalist by userManager.isMinimalistModeFlow.collectAsState(initial = false)

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // workout header
            Text(
                text = "Workout on ${formatTimestamp(workout.createdAt)} at ${formatTimestamp(workout.createdAt, true)}",
                fontSize = if (isMinimalist) 16.sp else 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE0E0E0)
            )
            Spacer(modifier = Modifier.height(if (isMinimalist) 4.dp else 8.dp))

            // display supersets
            if (workout.supersets.isNotEmpty()) {
                workout.supersets.forEach { superset ->
                    if (isMinimalist) {
                        MinimalistSupersetDetails(superset)
                    } else {
                        SupersetDetails(superset)
                    }
                }
            }

            Spacer(modifier = Modifier.height(if (isMinimalist) 4.dp else 8.dp))

            // display general exercise stats
            if (!isMinimalist) {
                Text(
                    text = "${workout.supersets.sumOf { it.exercises.size }} total exercises",
                    fontSize = 14.sp,
                    color = Color(0xFFB0B0B0)
                )
            }
        }
    }
}


@Composable
fun SupersetDetails(superset: SuperSet) {
    Column(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .background(Color(0xFF2C2C2C), shape = RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Text(
            text = "🔀 Superset",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFB0B0B0)
        )
        Spacer(modifier = Modifier.height(4.dp))

        // display exercises in the superset
        superset.exercises.forEach { activeExercise ->
            val parsedExercise = convertActiveExerciseToParsed(activeExercise)
            parsedExercise.exercises.forEach { exercise ->
                ExerciseDetails(exercise)
            }
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
        if (ExerciseMeasureType.useTime(exercise.measureType)) {
            Text(
                text = "⏱️ Time-Based: ${exercise.inset?.filter { it.isDone }?.size}/${exercise.inset?.size} sets completed",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE0E0E0)
            )
            exercise.inset?.forEachIndexed { index, time ->
                Text(
                    text = "  ${if (time.isDone) "✅" else "❌"}: ${time.value / 60}:${time.value % 60} | ${
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
                text = "🔢 Reps-Based: ${exercise.inset?.filter { it.isDone }?.size}/${exercise.inset?.size} sets completed",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE0E0E0)
            )
            exercise.inset?.forEachIndexed { index, rep ->
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