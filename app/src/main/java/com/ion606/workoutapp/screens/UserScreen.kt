package com.ion606.workoutapp.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ion606.workoutapp.helpers.Alerts
import com.ion606.workoutapp.managers.UserManager
import kotlinx.coroutines.launch


class UserScreen {
    companion object {
        @Composable
        fun CreateScreen(userManager: UserManager, navController: NavHostController) {
            val user = userManager.getUserData() ?: return
            val coroutineScope = rememberCoroutineScope()

            var name by remember { mutableStateOf(user.name) }
            var age by remember { mutableStateOf(user.age.toString()) }
            val gender = remember { mutableStateOf(user.gender) }
            var height by remember { mutableStateOf(user.height.toString()) }
            var weight by remember { mutableStateOf(user.weight.toString()) }
            val fitnessGoal = remember { mutableStateOf(user.fitnessGoal) }
            val preferredWorkoutType = remember { mutableStateOf(user.preferredWorkoutType) }
            val comfortLevel = remember { mutableStateOf(user.comfortLevel) }
            var oldPassword by remember { mutableStateOf("") }
            var newPassword by remember { mutableStateOf("") }
            val triggerDelete = remember { mutableStateOf(false) }
            val alertmsg = remember { mutableStateOf(Pair<String, String?>("", "")) }

            if (triggerDelete.value) userManager.DeleteAccount(navController);

            if (alertmsg.value.first.isNotEmpty()) Alerts.ShowAlert({
                alertmsg.value = Pair("", "")
            }, alertmsg.value.first, alertmsg.value.second ?: "")

            val genderOptions = listOf("Male", "Female", "Non-Binary", "Other")
            val fitnessGoals = listOf(
                "Build Muscle",
                "Lose Weight",
                "Improve Endurance",
                "Increase Strength",
                "Enhance Flexibility & Mobility",
                "General Fitness & Well-being",
                "Sports Performance",
                "Gain Weight & Bulk Up",
                "Unknown"
            )
            val workoutTypes = listOf(
                "Gym Workouts",
                "Home Workouts (Minimal Equipment)",
                "Bodyweight Only",
                "Cardio Focused",
                "Strength Training",
                "Yoga & Pilates",
                "HIIT (High-Intensity Interval Training)",
                "CrossFit Style Workouts",
                "Unknown"
            )
            val comfortLevels = listOf("Beginner", "Intermediate", "Advanced", "Unknown")

            Scaffold(
                bottomBar = {
                    WorkoutBottomBar(navController, 2)
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = user.email,
                        onValueChange = {},
                        label = { Text("Email") },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it },
                        label = { Text("Age") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Dropdown("Gender", genderOptions, gender)
                    OutlinedTextField(
                        value = height,
                        onValueChange = { height = it },
                        label = { Text("Height (cm)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("Weight (kg)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Dropdown("Fitness Goal", fitnessGoals, fitnessGoal)
                    Dropdown("Preferred Workout Type", workoutTypes, preferredWorkoutType)
                    Dropdown("Comfort Level", comfortLevels, comfortLevel)

                    HorizontalDivider()

                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        label = { Text("Old Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            val updatedUser = user.copy(
                                name = name,
                                age = age.toIntOrNull() ?: user.age,
                                gender = gender.value,
                                height = height.toIntOrNull() ?: user.height,
                                weight = weight.toIntOrNull() ?: user.weight,
                                fitnessGoal = fitnessGoal.value,
                                preferredWorkoutType = preferredWorkoutType.value,
                                comfortLevel = comfortLevel.value
                            )

                            coroutineScope.launch {
                                val r = userManager.updateUserData(
                                    updatedUser,
                                    // yes newPassword is there twice on purpose
                                    if (newPassword.isNotEmpty()) oldPassword else null,
                                    if (newPassword.isNotEmpty()) newPassword else null
                                )

                                if (r.first) alertmsg.value =
                                    Pair("User data updated successfully", "")
                                else {
                                    // re-populate the fields with the old data
                                    name = user.name
                                    age = user.age.toString()
                                    gender.value = user.gender
                                    height = user.height.toString()
                                    weight = user.weight.toString()
                                    fitnessGoal.value = user.fitnessGoal
                                    preferredWorkoutType.value = user.preferredWorkoutType
                                    comfortLevel.value = user.comfortLevel

                                    alertmsg.value =
                                        Pair(
                                            "Failed to update user data",
                                            r.second ?: "Unknown error"
                                        )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("Save")
                    }

                    HorizontalDivider(thickness = 3.dp, color = Color.Red)
                    Text("DANGER ZONE", modifier = Modifier.padding(top = 8.dp))
                    Button(
                        onClick = {
                            triggerDelete.value = true
                        },
                        colors = ButtonDefaults.buttonColors(Color.Red, Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Delete Account")
                    }
                }
            }
        }
    }
}
