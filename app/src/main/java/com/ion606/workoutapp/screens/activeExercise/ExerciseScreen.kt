package com.ion606.workoutapp.screens.activeExercise

//noinspection UsingMaterialAndMaterial3Libraries
import SelectWorkoutPopup
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.gson.Gson
import com.ion606.workoutapp.dataObjects.ActiveExercise
import com.ion606.workoutapp.dataObjects.ActiveExerciseDao
import com.ion606.workoutapp.dataObjects.SavedWorkoutResponse
import com.ion606.workoutapp.dataObjects.Workout
import com.ion606.workoutapp.helpers.Alerts
import com.ion606.workoutapp.helpers.Alerts.Companion.CreateAlertDialog
import com.ion606.workoutapp.helpers.NotificationManager
import com.ion606.workoutapp.helpers.convertSecondsToTimeString
import com.ion606.workoutapp.managers.SyncManager
import com.ion606.workoutapp.managers.UserManager
import com.ion606.workoutapp.screens.WorkoutBottomBar
import kotlinx.coroutines.launch


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


@SuppressLint("NotConstructor")
class ExerciseScreen {
    companion object {
        @Composable
        fun CreateScreen(
            userManager: UserManager,
            syncManager: SyncManager,
            dao: ActiveExerciseDao,
            navController: NavHostController,
            context: Context,
            nhelper: NotificationManager
        ) {
            val exercises = remember { mutableStateOf(listOf<ActiveExercise>()) }
            val dispSelPop = remember { mutableStateOf(false) }
            val openExercise = remember { mutableStateOf<ActiveExercise?>(null) }
            val showExitConfirmation = remember { mutableStateOf(false) }
            val currentCat = remember { mutableStateOf("") } // Tracks selected category
            val coroutneScope = rememberCoroutineScope()
            val expandDropdown = remember { mutableStateOf(false) }
            val endWorkout = remember { mutableIntStateOf(0) }
            val savedWorkout = remember { mutableListOf<Map<String, Any?>?>() }
            val workoutTime = remember { mutableStateOf(WorkoutTimerObject()) }

            // Handle back navigation
            BackHandler {
                if (exercises.value.isEmpty()) navController.navigate("home")
                else showExitConfirmation.value = true
            }

            // timer logic: count up every second
            LaunchedEffect(Unit) {
                while (endWorkout.intValue == 0) {
                    kotlinx.coroutines.delay(1000L) // wait for 1 second
                    if (!workoutTime.value.paused) {
                        workoutTime.value.time++
                    }
                    workoutTime.value.totalTime++
                }
            }

            // Exit confirmation dialog
            if (showExitConfirmation.value) {
                if (currentCat.value.isNotEmpty()) currentCat.value = ""
                else if (dispSelPop.value) dispSelPop.value = false
                else if (openExercise.value != null) openExercise.value = null
                else return Alerts.ShowAlert(onClick = {
                    if (it) endWorkout.intValue = 2

                    // not an else because the popup needs to close either way
                    showExitConfirmation.value = false
                })
                showExitConfirmation.value = false
            }

            LaunchedEffect(Unit) {
                exercises.value = dao.getAll() // Fetch all saved exercises from the database
            }

            if (dispSelPop.value) {
                ExercisePickerPopup.CreateSelectionPopup(
                    userManager, exercises, dispSelPop, currentCat, dao = dao
                )
            }

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
                    Log.d(TAG, "Saving workout with name: $workoutName")

                    LaunchedEffect("saveworkoutsend") {
                        coroutneScope.launch {
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

            if (endWorkout.intValue == 1) {
                LaunchedEffect("endworkout") {
                    coroutneScope.launch {
                        if (dao.size() == 0) {
                            Log.d(TAG, "No exercises to save")
                            navController.navigate("home")
                            endWorkout.intValue = 0
                            return@launch
                        }

                        val totalTime = workoutTime.value.time
                        val toSend = mapOf(
                            "exercises" to dao.getAll(),
                            "totalTime" to totalTime,
                            "workoutTime" to workoutTime.value.time
                        )
                        Log.d("SAVING", toSend.toString())
                        val r = syncManager.sendData(toSend, path = "workouts/workout")
                        Log.d("SAVE RESULT", r.toString())

                        dao.getAll().forEach { exercise ->
                            Log.d(TAG, "Deleting exercise: ${exercise.exercise.title}")
                            dao.delete(exercise)
                            exercises.value = exercises.value.filter { it.id != exercise.id }
                            Log.d(TAG, "Deleted exercise: ${exercise.exercise.title}")
                        }

                        if (exercises.value.isNotEmpty()) {
                            Log.d(TAG, "Failed to remove all exercises: $exercises")
                        } else {
                            Log.d(TAG, "Successfully removed all exercises")
                        }

                        navController.navigate("home")
                        endWorkout.intValue = 0;
                    }
                }
            } else if (endWorkout.intValue == 2) {
                // end the workout without saving
                LaunchedEffect("endworkout") {
                    coroutneScope.launch {
                        dao.getAll().forEach { exercise ->
                            Log.d(TAG, "Deleting exercise: ${exercise.exercise.title}")
                            dao.delete(exercise)
                            exercises.value = exercises.value.filter { it.id != exercise.id }
                            Log.d(TAG, "Deleted exercise: ${exercise.exercise.title}")
                        }
                    }
                }

                navController.navigate("home")
                endWorkout.intValue = 0
            }

            if (exercises.value.isNotEmpty() && !dispSelPop.value) {
                Log.d(TAG, "Exercises size: ${exercises.value.size}")
                Log.d(TAG, "Exercises: ${exercises.value.map { it.exercise.title }}")

                if (openExercise.value == null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp), // Removed verticalScroll()
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Spacer(modifier = Modifier.size(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Button(
                                onClick = { dispSelPop.value = true },
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text("+")
                            }

                            Text(text = convertSecondsToTimeString(workoutTime.value.time),
                                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                                color = if (workoutTime.value.paused) Color.LightGray else Color.White,
                                textAlign = TextAlign.Center,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier
                                    .padding(top = 5.dp)
                                    .weight(1f) // Take up remaining space
                                    .clickable {
                                        workoutTime.value.paused = !workoutTime.value.paused
                                    })

//                            Spacer(modifier = Modifier.weight(1f)) // Pushes the three-dot button to the right

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
                                        val toSave = exercises.value.map {
                                            mapOf(
                                                "exercise" to it.exercise,
                                                "sets" to it.sets,
                                                "perset" to if (it.exercise.timeBased) it.times else it.reps
                                            )
                                        }

                                        Log.d("SAVING", toSave.toString())
                                        savedWorkout.apply {
                                            clear()
                                            addAll(toSave)
                                        }

                                        expandDropdown.value = false
                                    }, text = { Text("Save Workout", color = Color.White) })

                                    DropdownMenuItem(onClick = {
                                        endWorkout.intValue = 1
                                        expandDropdown.value = false
                                    }, text = { Text("Complete Workout", color = Color.White) })
                                }
                            }
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(items = exercises.value, key = { it.id }) { exercise ->
                                ExerciseBox(exercise, openExercise, exercises, dao)
                            }
                        }
                    }
                }
            } else if (exercises.value.isEmpty() && !dispSelPop.value) {
                val exercisesToAdd = remember { mutableStateOf<List<ActiveExercise>?>(null) }

                // this effect will only run when exercisesToAdd.value changes
                LaunchedEffect(exercisesToAdd.value) {
                    val newExercises = exercisesToAdd.value ?: return@LaunchedEffect
                    val toAdd = exercisesToActiveExercises(newExercises.map { it.exercise });

                    dao.insertAll(toAdd)
                    exercises.value = toAdd

                    Log.d(TAG, "Added exercises: ${toAdd.map { it.exercise.title }}")
                    exercisesToAdd.value = null
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
                                exercisesToAdd.value =
                                    exercisesToActiveExercises(it.exercises.toList());
                            }, { savedWorkouts.value = null }, syncManager, context
                            )
                        }

                        if (!loadWorkout) {
                            Button(onClick = { dispSelPop.value = true }) {
                                Text("Add Exercises")
                            }
                            Spacer(modifier = Modifier.size(20.dp))
                        } else {
                            LaunchedEffect("endworkout") {
                                coroutneScope.launch {
                                    val r = syncManager.sendData(
                                        mapOf(), path = "workouts/savedworkouts", method = "GET"
                                    );

                                    if (r.first) {
                                        savedWorkouts.value = Gson().fromJson(
                                            r.second.toString(), SavedWorkoutResponse::class.java
                                        ).workouts.toList()
                                    } else Log.d("ERROR!", r.second.toString())
                                    loadWorkout = false;
                                }
                            }
                        }
                        Button(onClick = {
                            loadWorkout = true
//                            dao.insertAll()
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

            if (openExercise.value != null) {
                DisplayActiveExercise.DisplayActiveExerciseScreen(activeExercise = openExercise,
                    context = context,
                    nhelper = nhelper,
                    triggerExerciseSave = { exercise: ActiveExercise ->
                        Log.d(
                            TAG, "Saving exercise (in ExerciseScreen): ${exercise.exercise.title}"
                        )

                        coroutneScope.launch {
                            dao.update(exercise)
                            Log.d(TAG, "Saved exercise: ${exercise.exercise.title}")
                        }
                    })
            }
        }
    }
}
