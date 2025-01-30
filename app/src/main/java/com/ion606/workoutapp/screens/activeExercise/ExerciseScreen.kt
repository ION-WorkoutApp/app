package com.ion606.workoutapp.screens.activeExercise

//noinspection UsingMaterialAndMaterial3Libraries
import SelectWorkoutPopup
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.gson.Gson
import com.ion606.workoutapp.dataObjects.ActiveExercise
import com.ion606.workoutapp.dataObjects.ActiveExerciseDao
import com.ion606.workoutapp.dataObjects.SavedWorkoutResponse
import com.ion606.workoutapp.dataObjects.Workout
import com.ion606.workoutapp.dataObjects.WorkoutViewModel
import com.ion606.workoutapp.dataObjects.WorkoutViewModelFactory
import com.ion606.workoutapp.helpers.Alerts
import com.ion606.workoutapp.helpers.Alerts.Companion.CreateAlertDialog
import com.ion606.workoutapp.helpers.NotificationManager
import com.ion606.workoutapp.helpers.convertSecondsToTimeString
import com.ion606.workoutapp.managers.SyncManager
import com.ion606.workoutapp.managers.UserManager
import com.ion606.workoutapp.screens.WorkoutBottomBar
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


private const val TAG = "ExerciseScreen"


suspend fun List<ActiveExercise>.saveAll(dao: ActiveExerciseDao) {
    for (exercise in this) {
        dao.insert(exercise)
    }
}


class WorkoutTimerObject {
    var time by mutableIntStateOf(0)
    var totalTime by mutableIntStateOf(0)
    var paused by mutableStateOf(false)
}

@Composable
fun Timer(workoutTime: WorkoutTimerObject) {
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000L)
            if (!workoutTime.paused) {
                workoutTime.time++
            }
            workoutTime.totalTime++
        }
    }
}

@Composable
fun TimerDisplay(workoutTime: WorkoutTimerObject) {
    Text(text = convertSecondsToTimeString(workoutTime.time),
        style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
        color = if (workoutTime.paused) Color.LightGray else Color.White,
        textAlign = TextAlign.Center,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier
            .padding(top = 5.dp)
            .clickable {
                workoutTime.paused = !workoutTime.paused
            })
}


class ExerciseScreen {
    companion object {

        @Composable
        @SuppressLint("UnusedContentLambdaTargetStateParameter")
        fun CreateScreen(
            userManager: UserManager,
            syncManager: SyncManager,
            dao: SuperSetDao,
            navController: NavHostController,
            context: Context,
            nhelper: NotificationManager,
            workoutViewModel: WorkoutViewModel = viewModel(
                factory = WorkoutViewModelFactory(dao)
            )
        ) {
            val supersets = workoutViewModel.supersets
            val currentExercise = workoutViewModel.currentExercise
            val currentSuperset = workoutViewModel.currentSuperset

            val dispSelPop = remember { mutableStateOf(false) }
            val showExitConfirmation = remember { mutableStateOf(false) }
            val currentCat = remember { mutableStateOf("") } // Tracks selected category
            val coroutineScope = rememberCoroutineScope()
            val expandDropdown = remember { mutableStateOf(false) }
            val endWorkout = remember { mutableIntStateOf(0) }
            var saveWorkout by remember { mutableStateOf(false) }
            val workoutTime = remember { WorkoutTimerObject() }

            // Handle back navigation
            BackHandler {
                if (supersets.isEmpty()) navController.navigate("home")
                else showExitConfirmation.value = true
            }

            // Timer logic: count up every second
            Timer(workoutTime)

            // Exit confirmation dialog
            if (showExitConfirmation.value) {
                if (currentCat.value.isNotEmpty()) currentCat.value = ""
                else if (dispSelPop.value) dispSelPop.value = false
                else if (currentExercise.value != null) workoutViewModel.clearCurrentExercise()
                else Alerts.ShowAlert(onClick = {
                    if (it) endWorkout.intValue = 2

                    // not an else because the popup needs to close either way
                    showExitConfirmation.value = false
                })
            }

            // Initialize supersets from the database
            LaunchedEffect(Unit) {
                val fetchedSupersets = dao.getAll() // Fetch all saved supersets from the database
                workoutViewModel.initializeSupersets(fetchedSupersets)
            }

            if (dispSelPop.value) {
                ExercisePickerPopup.CreateSelectionPopup(
                    userManager, supersets, dispSelPop, currentCat, dao = dao
                )
            }

            if (saveWorkout) {
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
                    Log.d(TAG, "Saving workout with name: $workoutName")

                    LaunchedEffect("saveworkoutsend") {
                        coroutineScope.launch {
                            val r = syncManager.sendData(
                                mapOf(
                                    "workout" to mapOf(
                                        "supersets" to dao.getAll(),
                                        "totalTime" to 0
                                    ), "workoutname" to workoutName.toString()
                                ), path = "workouts/savedworkouts"
                            )

                            error = if (r.first) {
                                "Successfully saved workout"
                            } else r.second.toString()

                            saveWorkout = false
                            Log.d("SAVE RESULT", r.toString())
                        }
                    }
                }
            }

            if (endWorkout.intValue == 1) {
                LaunchedEffect("endworkout") {
                    coroutineScope.launch {
                        if (dao.size() == 0) {
                            Log.d(TAG, "No exercises to save")
                            navController.navigate("home")
                            endWorkout.intValue = 0
                            return@launch
                        }

                        val exercises = dao.getAll()
                        val totalTime = workoutTime.time

                        if (!exercises.any { it.isDone }) {
                            Log.d(TAG, "No exercises completed")
                            navController.navigate("home")
                            endWorkout.intValue = 0
                            return@launch
                        }

                        val toSend = mapOf(
                            "supersets" to exercises,
                            "totalTime" to totalTime,
                            "workoutTime" to workoutTime.time
                        )
                        Log.d("SAVING", toSend.toString())
                        val r = syncManager.sendData(toSend, path = "workouts/workout")
                        Log.d("SAVE RESULT", r.toString())

                        dao.getAll().forEach { exercise ->
                            Log.d(TAG, "Deleting exercise: ${exercise.id}")
                            dao.delete(exercise)
                            supersets.removeAll { it.id == exercise.id }
                            Log.d(TAG, "Deleted exercise: ${exercise.id}")
                        }

                        if (supersets.isNotEmpty()) {
                            Log.d(TAG, "Failed to remove all exercises: $supersets")
                        } else {
                            Log.d(TAG, "Successfully removed all exercises")
                        }

                        navController.navigate("home")
                        endWorkout.intValue = 0
                    }
                }
            } else if (endWorkout.intValue == 2) {
                // End the workout without saving
                LaunchedEffect("endworkout") {
                    coroutineScope.launch {
                        dao.getAll().forEach { exercise ->
                            Log.d(TAG, "Deleting exercise: ${exercise.id}")
                            dao.delete(exercise)
                            supersets.removeAll { it.id == exercise.id }
                            Log.d(TAG, "Deleted exercise: ${exercise.id}")
                        }
                    }
                }

                navController.navigate("home")
                endWorkout.intValue = 0
            }

            if (supersets.isNotEmpty() && !dispSelPop.value) {
                if (currentSuperset.value == null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp), // Removed verticalScroll()
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Spacer(modifier = Modifier.size(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically, // Align items vertically centered
                            horizontalArrangement = Arrangement.SpaceBetween // Space items evenly
                        ) {
                            // + Button on the left
                            Button(
                                onClick = { dispSelPop.value = true },
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text("+")
                            }

                            // Center the TimerDisplay
                            Box(
                                modifier = Modifier
                                    .weight(1f) // Take up remaining space
                                    .wrapContentSize(Alignment.Center) // Center the content inside the Box
                            ) {
                                TimerDisplay(workoutTime)
                            }

                            // Dropdown menu on the right
                            Box {
                                Button(onClick = { expandDropdown.value = true }) {
                                    Text("...")
                                }

                                DropdownMenu(
                                    expanded = expandDropdown.value,
                                    onDismissRequest = { expandDropdown.value = false },
                                    modifier = Modifier.wrapContentSize(Alignment.TopEnd)
                                ) {
                                    DropdownMenuItem(onClick = {
                                        saveWorkout = true
                                        expandDropdown.value = false
                                    }, text = { Text("Add to Saved Workouts", color = Color.White) })

                                    DropdownMenuItem(onClick = {
                                        endWorkout.intValue = 1
                                        expandDropdown.value = false
                                    }, text = { Text("Complete Workout", color = Color.White) })
                                }
                            }
                        }

                        val superSetBoundsList = remember { mutableMapOf<String, Rect?>() }
                        supersets.map { superSetBoundsList[it.id] = null }

                        // LazyColumn to display supersets
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(items = supersets, key = { it.id }) { superSet ->
                                val xOffset = remember { Animatable(0f) }

                                Modifier.fillMaxWidth()
                                Box(modifier = Modifier.offset {
                                    IntOffset(
                                        xOffset.value.roundToInt(), 0
                                    )
                                }) {
                                    SuperSetBox(superSet = superSet,
                                        viewModel = workoutViewModel,
                                        superSetBounds = superSetBoundsList,
                                        onExerciseClick = { clickedExercise ->
                                            workoutViewModel.setCurrentExercise(
                                                clickedExercise, superSet
                                            )
                                        })
                                }
                            }
                        }
                    }
                }
            } else if (supersets.isEmpty() && !dispSelPop.value) {
                val supersetsToAdd = remember { mutableStateOf<List<SuperSet>?>(null) }

                // reset the dao ("clear the cache" ish)
                LaunchedEffect(Unit) {
                    coroutineScope.launch {
                        dao.getAll().forEach { exercise ->
                            dao.delete(exercise)
                            supersets.removeAll { it.id == exercise.id }
                            Log.d(TAG, "Deleted exercise: ${exercise.id}")
                        }
                    }
                }

                LaunchedEffect(supersetsToAdd.value) {
                    val newExercises = supersetsToAdd.value ?: return@LaunchedEffect

                    dao.insertAll(newExercises)
                    workoutViewModel.initializeSupersets(newExercises)
                    supersetsToAdd.value = null
                }

                Box(
                    modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        var loadWorkout by remember { mutableStateOf(false) }
                        val savedWorkouts = remember { mutableStateOf<List<Workout>?>(null) }

                        if (savedWorkouts.value != null) {
                            SelectWorkoutPopup(savedWorkouts.value!!, {
                                supersetsToAdd.value = it.supersets.toList()
                            }, { savedWorkouts.value = null }, syncManager, context
                            )
                        }

                        if (!loadWorkout) {
                            Button(onClick = { dispSelPop.value = true }) {
                                Text("Add Exercises")
                            }
                            Spacer(modifier = Modifier.size(20.dp))
                        } else {
                            LaunchedEffect("savedworkoutsbtn") {
                                coroutineScope.launch {
                                    val r = syncManager.sendData(
                                        mapOf(), path = "workouts/savedworkouts", method = "GET"
                                    )

                                    if (r.first) {
                                        val res = Gson().fromJson(
                                            r.second.toString(), SavedWorkoutResponse::class.java
                                        )
                                        if (!res.success) {
                                            if (!res.message.isNullOrEmpty()) Log.d(
                                                "ERROR!", res.message
                                            )
                                        } else savedWorkouts.value = res.workouts.toList()
                                    } else Log.d("ERROR!", r.second.toString())
                                    loadWorkout = false
                                }
                            }
                        }
                        Button(onClick = {
                            loadWorkout = true
                        }) {
                            Text("Load Saved Workout")
                        }
                    }

                    Box(
                        contentAlignment = Alignment.BottomCenter,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    ) {
                        WorkoutBottomBar(navController)
                    }
                }
            }

            if (currentExercise.value != null && currentSuperset.value != null) {
                AnimatedContent(targetState = currentExercise.value, transitionSpec = {
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> fullWidth },
                        animationSpec = tween(durationMillis = 300)
                    ) togetherWith slideOutHorizontally(
                        targetOffsetX = { fullWidth -> -fullWidth },
                        animationSpec = tween(durationMillis = 300)
                    )
                }) { _ ->
                    DisplayActiveExercise.DisplayActiveExerciseScreen(activeExercise = currentExercise,
                        context = context,
                        nhelper = nhelper,
                        currentSuperset = currentSuperset,
                        triggerExerciseSave = { exercise: ActiveExercise, superset: SuperSet, exitScreen: Boolean ->
                            Log.d(
                                TAG,
                                "Saving exercise (in ExerciseScreen): ${exercise.exercise.title}. Is an exit? $exitScreen"
                            )

                            superset.updateExercise(exercise)

                            coroutineScope.launch {
                                dao.update(superset)
                                Log.d(
                                    TAG, "updated DAO for exercise: ${exercise.exercise.title}"
                                )
                                if (exitScreen) workoutViewModel.clearCurrentExercise()
                            }
                        },
                        advanceToNextExercise = { nextExercise: ActiveExercise?, superset: SuperSet ->
                            // if the current exercise is completed, mark it as done
                            if (currentExercise.value!!.isDone) {
                                currentExercise.value!!.isDone = true
                                coroutineScope.launch { dao.update(superset) }
                            }

                            // superset over
                            if (nextExercise == null) {
                                Log.d(TAG, "Completed superset ${superset.id}")
                                superset.isDone = true
                                workoutViewModel.clearCurrentExercise()
                                coroutineScope.launch { dao.update(superset) }
                                return@DisplayActiveExerciseScreen
                            }

                            if (superset.isOnFirstExercise()) {
                                // "reset" the superset order (go back to first)
                                workoutViewModel.clearCurrentExercise()
                                workoutViewModel.setCurrentExercise(
                                    superset.exercises.first(), superset
                                )
                            } else workoutViewModel.setCurrentExercise(nextExercise, superset)
                        })
                }
            }
        }
    }
}