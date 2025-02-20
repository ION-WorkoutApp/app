package com.ion606.workoutapp.screens.settings

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ion606.workoutapp.R
import com.ion606.workoutapp.dataObjects.User.SanitizedUserDataObj
import com.ion606.workoutapp.elements.DropdownMenuField
import com.ion606.workoutapp.elements.Tooltip
import com.ion606.workoutapp.helpers.Alerts
import com.ion606.workoutapp.logic.SimpleStorage.getCurrentWeekNumber
import com.ion606.workoutapp.managers.UserManager
import kotlinx.coroutines.launch
import androidx.compose.material3.CenterAlignedTopAppBar as TopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralPreferencesScreen(
    navController: NavController, userManager: UserManager, context: Context
) {
    val coroutineScope = rememberCoroutineScope()
    val user = userManager.getUserData()?.generalPreferences ?: return
    val allData = userManager.getUserData() ?: return

    // State variables
    var activityLevel by remember { mutableStateOf(user.activityLevel) }
    var preferredWorkoutTime by remember { mutableStateOf(user.preferredWorkoutTime) }
    var workoutFrequency by remember { mutableStateOf(user.workoutFrequency.toString()) }
    var injuriesOrLimitations by remember {
        mutableStateOf(user.injuriesOrLimitations.joinToString(", "))
    }
    var equipmentAccess by remember {
        mutableStateOf(user.equipmentAccess.joinToString(", "))
    }
    var preferredWorkoutEnvironment by remember {
        mutableStateOf(user.preferredWorkoutEnvironment)
    }

    val alertMsg = remember { mutableStateOf(Pair("", "")) }

    // Enumerations
    val activityLevels = listOf("sedentary", "light", "moderate", "active", "very active")
    val workoutTimes = listOf("morning", "afternoon", "evening", "no preference")
    val workoutEnvironments = listOf("gym", "home", "outdoor", "no preference")

    // State for dropdowns
    val goals = listOf(
        "Build Muscle",
        "Lose Weight",
        "Improve Endurance",
        "Increase Strength",
        "Enhance Flexibility & Mobility",
        "General Fitness & Well-being",
        "Sports Performance",
        "Gain Weight & Bulk Up"
    )
    val workoutTypes = listOf(
        "Gym Workouts",
        "Home Workouts (Minimal Equipment)",
        "Bodyweight Only",
        "Cardio Focused",
        "Strength Training",
        "Yoga & Pilates",
        "HIIT (High-Intensity Interval Training)",
        "CrossFit Style Workouts"
    )
    val comfortLevels = listOf("Beginner", "Intermediate", "Advanced")

    // preferences
    val sharedPrefs = context.getSharedPreferences("localprefs", Context.MODE_PRIVATE);
    var notificationSound by remember {
        mutableIntStateOf(
            sharedPrefs.getInt(
                "notifsound", R.raw.chime
            )
        )
    }
    var distanceUnit by remember { mutableStateOf(allData.distanceUnit) }
    var fitnessGoal by remember { mutableStateOf(allData.fitnessGoal) }
    var preferredWorkoutType by remember { mutableStateOf(allData.preferredWorkoutType) }
    var comfortLevel by remember { mutableStateOf(allData.comfortLevel) }
    var weightUnit by remember { mutableStateOf(allData.weightUnit) }
    var toolTipText by remember { mutableStateOf("") }

    val weightUnits = listOf("kg", "lbs")
    val distanceUnits = listOf("km", "miles")


    if (alertMsg.value.first.isNotEmpty()) {
        Alerts.ShowAlert(
            onClick = { alertMsg.value = Pair("", "") },
            title = alertMsg.value.first,
            text = alertMsg.value.second,
            oneButton = true
        )
    } else if (toolTipText.isNotEmpty()) {
        Tooltip(text = toolTipText, onDismiss = { toolTipText = "" })
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text("General Preferences") }, navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack, contentDescription = "Back"
                )
            }
        })
    }, content = { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DropdownMenuField(
                label = "Notification Sound",
                options = listOf("Chime", "Chime 2", "Chat Notification", "Xylophone", "None"),
                value = when (notificationSound) {
                    R.raw.chime_2 -> "Chime 2"
                    R.raw.livechat -> "Chat Notification"
                    R.raw.xylophone -> "Xylophone"
                    0 -> "None"
                    else -> "Chime"
                },
            ) {
                notificationSound = when (it) {
                    "Chime 2" -> R.raw.chime_2
                    "Chat Notification" -> R.raw.livechat
                    "Xylophone" -> R.raw.xylophone
                    "None" -> 0
                    else -> R.raw.chime
                }

                sharedPrefs.edit().putInt("notifsound", notificationSound).apply()
                toolTipText = "Notification sound updated"
            }

            DropdownMenuField(label = "Activity Level",
                options = activityLevels,
                value = activityLevel,
                onValueChange = { activityLevel = it })

            DropdownMenuField(label = "Preferred Workout Time",
                options = workoutTimes,
                value = preferredWorkoutTime,
                onValueChange = { preferredWorkoutTime = it })

            OutlinedTextField(value = workoutFrequency,
                onValueChange = { workoutFrequency = it },
                label = { Text("Workouts Per Week (1-7)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // TODO: add equipment selection
//                OutlinedTextField(
//                    value = equipmentAccess,
//                    onValueChange = { equipmentAccess = it },
//                    label = { Text("Equipment Access (comma separated)") },
//                    modifier = Modifier.fillMaxWidth()
//                )
            DropdownMenuField(label = "Preferred Workout Environment",
                options = workoutEnvironments,
                value = preferredWorkoutEnvironment,
                onValueChange = { preferredWorkoutEnvironment = it })

            DropdownMenuField(label = "Weight Unit",
                options = weightUnits,
                value = weightUnit,
                onValueChange = { weightUnit = it })

            DropdownMenuField(label = "Distance Unit",
                options = distanceUnits,
                value = distanceUnit,
                onValueChange = { distanceUnit = it })

            DropdownMenuField(label = "Fitness Goal",
                options = goals,
                value = fitnessGoal,
                onValueChange = { fitnessGoal = it })

            DropdownMenuField(label = "Preferred Workout Type",
                options = workoutTypes,
                value = preferredWorkoutType,
                onValueChange = { preferredWorkoutType = it })

            DropdownMenuField(label = "Comfort Level",
                options = comfortLevels,
                value = comfortLevel,
                onValueChange = { comfortLevel = it })

            Button(
                onClick = {
                    // Validate numeric fields
                    val workoutFreqInt = workoutFrequency.toIntOrNull()

                    if (workoutFreqInt == null || workoutFreqInt < 1 || workoutFreqInt > 7) {
                        alertMsg.value = Pair(
                            "Invalid Frequency",
                            "Workout frequency must be a number between 1 and 7."
                        )
                        return@Button
                    }

                    // Update general preferences
                    val newUser = userManager.getAllUserData()?.copy(generalPreferences = user.copy(
                        activityLevel = activityLevel,
                        preferredWorkoutTime = preferredWorkoutTime,
                        workoutFrequency = workoutFreqInt,
                        injuriesOrLimitations = injuriesOrLimitations.split(",").map { it.trim() }
                            .filter { it.isNotEmpty() },
                        equipmentAccess = equipmentAccess.split(",").map { it.trim() }
                            .filter { it.isNotEmpty() },
                        preferredWorkoutEnvironment = preferredWorkoutEnvironment
                    )
                    ) ?: return@Button

                    coroutineScope.launch {
                        // add data I hastily moved from other screen
                        val result = userManager.updateUserData(
                            SanitizedUserDataObj(
                                newUser.copy(
                                    weightUnit = weightUnit,
                                    distanceUnit = distanceUnit,
                                    fitnessGoal = fitnessGoal,
                                    preferredWorkoutType = preferredWorkoutType,
                                    comfortLevel = comfortLevel
                                )
                            )
                        );

                        if (result.first) {
                            alertMsg.value = Pair("Success", "General preferences updated.")
                        } else {
                            alertMsg.value = Pair(
                                "Error", result.second ?: "Unknown error."
                            )
                        }
                    }
                }, modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    })
}
