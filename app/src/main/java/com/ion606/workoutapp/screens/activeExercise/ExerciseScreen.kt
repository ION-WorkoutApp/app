package com.ion606.workoutapp.screens.activeExercise

import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.DropdownMenu
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ion606.workoutapp.dataObjects.ActiveExercise
import com.ion606.workoutapp.dataObjects.ActiveExerciseDao
import com.ion606.workoutapp.helpers.Alerts
import com.ion606.workoutapp.managers.SyncManager
import com.ion606.workoutapp.managers.UserManager
import kotlinx.coroutines.launch


private const val TAG = "ExerciseScreen"


suspend fun List<ActiveExercise>.saveAll(dao: ActiveExerciseDao) {
    for (exercise in this) {
        dao.insert(exercise)
    }
}


@SuppressLint("NotConstructor")
class ExerciseScreen {
    companion object {
        @Composable
        fun CreateScreen(
            userManager: UserManager,
            syncManager: SyncManager,
            dao: ActiveExerciseDao,
            navController: NavHostController
        ) {
            val exercises = remember { mutableStateOf(listOf<ActiveExercise>()) }
            val dispSelPop = remember { mutableStateOf(false) }
            val openExercise = remember { mutableStateOf<ActiveExercise?>(null) }
            val showExitConfirmation = remember { mutableStateOf(false) }
            val currentCat = remember { mutableStateOf("") } // Tracks selected category
            val coroutneScope = rememberCoroutineScope()
            val expandDropdown = remember { mutableStateOf(false) }
            val endWorkout = remember { mutableStateOf(false) }

            // Handle back navigation
            BackHandler {
                if (exercises.value.isEmpty()) navController.navigate("home")
                else showExitConfirmation.value = true
            }

            // Exit confirmation dialog
            if (showExitConfirmation.value) {
                if (currentCat.value.isNotEmpty()) currentCat.value = ""
                else if (dispSelPop.value) dispSelPop.value = false
                else if (openExercise.value != null) openExercise.value = null
                else return Alerts.ShowAlert(onClick = { navController.navigate("home") })
                showExitConfirmation.value = false
            }

            LaunchedEffect(Unit) {
                exercises.value = dao.getAll() // Fetch all saved exercises from the database
            }

            if (dispSelPop.value) {
                ExercisePickerPopup.CreateSelectionPopup(
                    userManager,
                    exercises,
                    dispSelPop,
                    currentCat,
                    dao = dao
                )
            }

            if (endWorkout.value) {
                LaunchedEffect("endworkout") {
                    coroutneScope.launch {
                        if (dao.size() == 0) {
                            Log.d(TAG, "No exercises to save")
                            navController.navigate("home")
                            endWorkout.value = false
                            return@launch
                        }

                        val totalTime = 12123123123
                        val toSend = mapOf(
                            "exercises" to dao.getAll(),
                            "totalTime" to totalTime
                        )
                        Log.d("SAVING", toSend.toString())
                        val r = syncManager.sendData(toSend, path = "workout")
                        Log.d("SAVE RESULT", r.toString())

                        dao.getAll().forEach { exercise ->
                            Log.d(TAG, "Deleting exercise: ${exercise.exercise.title}")
                            dao.delete(exercise)
                            exercises.value =
                                exercises.value.filter { it.id != exercise.id }
                            Log.d(TAG, "Deleted exercise: ${exercise.exercise.title}")
                        }

                        if (exercises.value.isNotEmpty()) {
                            Log.d(TAG, "Failed to remove all exercises: $exercises")
                        } else {
                            Log.d(TAG, "Successfully removed all exercises")
                        }

                        navController.navigate("home")
                        endWorkout.value = false;
                    }
                }
            }

            if (exercises.value.isNotEmpty() && !dispSelPop.value) {
                Log.d(TAG, "Exercises size: ${exercises.value.size}")
                Log.d(TAG, "Exercises: ${exercises.value.map { it.exercise.title }}")

                if (openExercise.value == null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Spacer(modifier = Modifier.size(20.dp))

                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Button(
                                onClick = { dispSelPop.value = true },
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text("+")
                            }
                            Spacer(modifier = Modifier.size(20.dp))
                            Button(onClick = { endWorkout.value = true }) {
                                Text("End")
                            }

                            Spacer(modifier = Modifier.weight(1f)) // Pushes the three-dot button to the right

                            Box { // Wrap the three-dot Button and DropdownMenu in a Box
                                Button(onClick = { expandDropdown.value = true }) {
                                    Text("...")
                                }

                                DropdownMenu(
                                    expanded = expandDropdown.value,
                                    onDismissRequest = { expandDropdown.value = false },
                                    modifier = Modifier.wrapContentSize(Alignment.TopEnd) // Align dropdown to the button's top-end
                                ) {
                                    DropdownMenuItem(
                                        onClick = {
                                            val toSave = exercises.value.map {
                                                mapOf(
                                                    "exercise" to it.exercise,
                                                    "sets" to it.sets,
                                                    "perset" to if (it.exercise.timeBased) it.times else it.reps
                                                )
                                            }

                                            Log.d("SAVING", toSave.toString())
                                            TODO()

                                            expandDropdown.value = false
                                        },
                                        text = { Text("Save Workout") }
                                    )
                                }
                            }
                        }


                        exercises.value.forEach { exercise ->
                            if (openExercise.value == null) {
                                ExerciseBox(exercise, openExercise, exercises, dao)
                            }
                        }
                    }
                }
            } else if (exercises.value.isEmpty() && !dispSelPop.value) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        Button(onClick = { dispSelPop.value = true }) {
                            Text("Add Exercises")
                        }

                        Spacer(modifier = Modifier.size(20.dp))

                        Button(onClick = { TODO(); /* TODO: Load saved workout logic */ }) {
                            Text("Load Saved Workout")
                        }
                    }

                    Button(onClick = { endWorkout.value = true }, modifier = Modifier.align(Alignment.TopEnd).padding(top = 30.dp, end = 30.dp)) {
                        Text("End")
                    }
                }
            }

            if (openExercise.value != null) {
                DisplayActiveExercise.DisplayActiveExerciseScreen(
                    activeExercise = openExercise,
                    triggerExerciseSave = { exercise: ActiveExercise ->
                        Log.d(
                            TAG,
                            "Saving exercise (in ExerciseScreen): ${exercise.exercise.title}"
                        )

                        coroutneScope.launch {
                            dao.update(exercise)
                            Log.d(TAG, "Saved exercise: ${exercise.exercise.title}")
                        }
                    }
                )
            }
        }
    }
}
