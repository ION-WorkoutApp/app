package com.ion606.workoutapp.screens

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.password
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.ion606.workoutapp.elements.Tooltip
import com.ion606.workoutapp.helpers.Alerts
import com.ion606.workoutapp.helpers.isWithinPastMonth
import com.ion606.workoutapp.helpers.openWebPage
import com.ion606.workoutapp.helpers.transformTimestampToDateString
import com.ion606.workoutapp.managers.DataManager
import com.ion606.workoutapp.managers.UserManager
import kotlinx.coroutines.launch
import java.util.Locale


class UserScreen {
    companion object {
        @Composable
        fun CreateScreen(
            userManager: UserManager,
            dataManager: DataManager,
            navController: NavHostController,
            context: Context
        ) {
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
            var triggerDataRequest by remember { mutableStateOf(false) }
            val alertmsg = remember { mutableStateOf(Pair<String, String?>("", "")) }
            var logout by remember { mutableStateOf(false) }
            var checkDataStatus by remember { mutableStateOf<String?>(null) }
            var canRequestData by remember {
                mutableStateOf(
                    user.lastRequestedData.isNullOrBlank() || !isWithinPastMonth(
                        user.lastRequestedData
                    )
                )
            }

            val isInMinMode by userManager.isMinimalistModeFlow.collectAsState(initial = false)

            if (triggerDelete.value) userManager.DeleteAccount(navController);
            else if (triggerDataRequest) {
                Alerts.CreateDropdownDialog(
                    title = "Data Format",
                    context = context,
                    options = listOf("JSON", "CSV", "ICS"),
                ) {
                    if (it == null) triggerDataRequest = false
                    else {
                        coroutineScope.launch {
                            canRequestData = false
                            val r = userManager.requestData(it.lowercase(Locale.getDefault()));
                            if (r.first) alertmsg.value = Pair("Data request sent successfully", "")
                            else {
                                alertmsg.value =
                                    Pair("Failed to send data request", r.second?.toString() ?: "")
                                canRequestData = true
                            }
                            triggerDataRequest = false
                        }
                    }
                }
            }

            LaunchedEffect(Unit) {
                coroutineScope.launch {
                    val r = userManager.checkDataStatus()
                    if (r.second.toString().contains("No Request Made")) return@launch
                    checkDataStatus = "Data request status: ${r.second}"
                }
            }

            if (logout) {
                LaunchedEffect(Unit) {
                    coroutineScope.launch {
                        dataManager.logout(navController)
                    }
                }
            }

            if (alertmsg.value.first.isNotEmpty()) Alerts.ShowAlert(
                {
                    alertmsg.value = Pair("", "")
                }, alertmsg.value.first, alertmsg.value.second ?: "", oneButton = true
            )

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
            var showTooltip by remember { mutableStateOf(false) }
            if (showTooltip) {
                Tooltip(text = "Please contact support to change your email",
                    onDismiss = { showTooltip = false })

            }

            Scaffold(bottomBar = {
                WorkoutBottomBar(navController, 2)
            }) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        IconButton(onClick = {
                            openWebPage(
                                context, "https://workout.ion606.com"
                            )
                        }) {
                            Icon(Icons.Default.Info, contentDescription = "Info")
                        }

                        Spacer(Modifier.weight(1f))

                        IconButton(onClick = { logout = true }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Logout",
                                tint = Color.Red,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    HorizontalDivider()

                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "App Settings", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Minimalist Mode", fontSize = 18.sp)
                            Spacer(modifier = Modifier.weight(1f))
                            Switch(checked = isInMinMode, onCheckedChange = {
                                coroutineScope.launch { userManager.toggleMinimalistMode() }
                            })
                        }
                    }

                    HorizontalDivider()

                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "User Settings", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(value = user.email,
                            onValueChange = {},
                            label = { Text("Email") },
                            enabled = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showTooltip = !showTooltip })
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

                        OutlinedTextField(value = oldPassword,
                            onValueChange = { oldPassword = it },
                            label = { Text("Old Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { this.password() })
                        OutlinedTextField(value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("New Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { this.password() })

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

                                        alertmsg.value = Pair(
                                            "Failed to update user data",
                                            r.second ?: "Unknown error"
                                        )
                                    }
                                }
                            }, modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text("Save")
                        }
                    }

                    HorizontalDivider(thickness = 3.dp, color = Color.Red)
                    Text("DANGER ZONE", modifier = Modifier.padding(top = 8.dp))

                    Button(
                        onClick = {
                            navController.navigate("permissions")
                        },
                        colors = ButtonDefaults.buttonColors(Color.Blue, Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Permissions")
                    }

                    val btnMod =
                        if (canRequestData) Modifier.fillMaxWidth() else Modifier
                            .fillMaxWidth()
                            .alpha(0.5f)
                            .focusable(false)
                    Button(
                        onClick = {
                            if (canRequestData) triggerDataRequest = true
                            else alertmsg.value = Pair(
                                "Data already requested this month",
                                "You last requested data on ${transformTimestampToDateString(user.lastRequestedData)}"
                            )
                        }, colors = ButtonDefaults.buttonColors(
                            if (canRequestData) Color.Blue else Color.DarkGray, Color.White
                        ), modifier = btnMod
                    ) {
                        Text(
                            if (canRequestData) "Request Data" else if (user.lastRequestedData.isNullOrBlank() || !isWithinPastMonth(
                                    user.lastRequestedData
                                )
                            ) "Data requested successfully" else "Data already requested this month"
                        )
                    }

                    if (!checkDataStatus.isNullOrEmpty()) {
                        Text(
                            text = "Data Request Status: $checkDataStatus",
                            fontSize = 14.sp,
                            color = Color.LightGray,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    }

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
