package com.ion606.workoutapp.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.password
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ion606.workoutapp.managers.DataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun Dropdown(label: String, options: List<String>, selectedOption: MutableState<String>) {
    val expanded = remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedOption.value,
            onValueChange = { },
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { expanded.value = !expanded.value }) {
                    Icon(
                        imageVector = if (expanded.value) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown"
                    )
                }
            }
        )
        DropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    onClick = {
                        selectedOption.value = option
                        expanded.value = false
                    },
                    text = { Text(option) }
                )
            }
        }
    }
}

@Composable
fun Signup(navController: NavController, dataManager: DataManager) {
    // State for navigation between steps
    val step = remember { mutableStateOf(1) }

    // State for user inputs
    val name = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val age = remember { mutableStateOf("") }
    val gender = remember { mutableStateOf("") }
    val height = remember { mutableStateOf("") }
    val weight = remember { mutableStateOf("") }
    val fitnessGoal = remember { mutableStateOf("") }
    val preferredWorkoutType = remember { mutableStateOf("") }
    val comfortLevel = remember { mutableStateOf("") }
    val serverUrl = remember { mutableStateOf("https://test.ion606.com") }
    val tempstr = remember { mutableStateOf("Loading...") }
    val tempsubheading = remember { mutableStateOf("") }

    // State for password visibility
    val passwordVisible = remember { mutableStateOf(false) }

    val goals = listOf(
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

    val workoutLevel = listOf(
        "Beginner",
        "Intermediate",
        "Advanced",
        "Unknown"
    )

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (step.value) {
                1 -> {
                    Text(
                        "Step 1: Basic Information",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = name.value,
                        onValueChange = { name.value = it },
                        label = { Text("Name") },
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = email.value,
                        onValueChange = { email.value = it },
                        label = { Text("Email") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = password.value,
                        onValueChange = { password.value = it },
                        label = { Text("Password") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        maxLines = 1,
                        visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (passwordVisible.value)
                                Icons.Filled.Visibility
                            else Icons.Filled.VisibilityOff

                            val description =
                                if (passwordVisible.value) "Hide password" else "Show password"

                            IconButton(onClick = {
                                passwordVisible.value = !passwordVisible.value
                            }) {
                                Icon(imageVector = image, contentDescription = description)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { this.password() }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { step.value = 2 }, modifier = Modifier.fillMaxWidth()) {
                        Text("Next")
                    }
                }

                2 -> {
                    Text(
                        "Step 2: Personal Information",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = age.value,
                        onValueChange = { age.value = it },
                        label = { Text("Age") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Dropdown("Gender", listOf("Male", "Female", "Non-Binary", "Other"), gender)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = height.value,
                        onValueChange = { height.value = it },
                        label = { Text("Height (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = weight.value,
                        onValueChange = { weight.value = it },
                        label = { Text("Weight (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { step.value = 1 }) {
                            Text("Back")
                        }
                        Button(onClick = { step.value = 3 }) {
                            Text("Next")
                        }
                    }
                }

                3 -> {
                    Text("Step 3: Fitness Goals", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Dropdown("Fitness Goal", goals, fitnessGoal)
                    Spacer(modifier = Modifier.height(16.dp))
                    Dropdown("Preferred Workout Type", workoutTypes, preferredWorkoutType)
                    Spacer(modifier = Modifier.height(16.dp))
                    Dropdown("Comfort Level", workoutLevel, comfortLevel)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { step.value = 4 }, modifier = Modifier.fillMaxWidth()) {
                        Text("Next")
                    }
                }

                4 -> {
                    Text("Step 4: Server Setup", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = serverUrl.value,
                        onValueChange = { serverUrl.value = it },
                        label = { Text("Server URL") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            step.value = 5
                            tempstr.value = "Pinging Server..."

                            val scope = CoroutineScope(Dispatchers.Default + Job())
                            scope.launch {
                                val rPing = dataManager.pingServer(serverUrl.value)
                                if (!rPing) {
                                    scope.launch(Dispatchers.Main) {
                                        tempstr.value = "Server is not reachable!"
                                        tempsubheading.value =
                                            "Please check the server URL and try again."
                                    }
                                    return@launch
                                }

                                tempstr.value = "Creating account..."

                                val userData = mapOf(
                                    "name" to name.value,
                                    "email" to email.value,
                                    "password" to password.value,
                                    "age" to age.value,
                                    "gender" to gender.value,
                                    "height" to height.value,
                                    "weight" to weight.value,
                                    "fitnessGoal" to fitnessGoal.value,
                                    "preferredWorkoutType" to preferredWorkoutType.value,
                                    "comfortLevel" to comfortLevel.value,
                                ).map { (k, v) -> k to v.trim() }.toMap()

                                val (success, v) = dataManager.createAccount(
                                    userData,
                                    serverUrl.value
                                )

                                scope.launch(Dispatchers.Main) {
                                    if (!success) {
                                        println("SIGNUP FAILED WITH ERROR $v!");
                                        if (v!!.contains("Client must be connected before running operations")) {
                                            tempstr.value =
                                                "SERVER ERROR! PLEASE CONTACT YOUR ADMIN OR CHECK SERVER LOGS!"
                                            tempsubheading.value =
                                                "if you ARE an admin and have recently set up your server, try restarting the server and try again."
                                        } else if (v.contains("Message: Conflict")) {
                                            tempstr.value = "Signup Error!"
                                            tempsubheading.value = "a user with the email ${email.value} already exists!"
                                        } else {
                                            tempstr.value = "Signup failed!"
                                            tempsubheading.value = "Error: $v"
                                        }
                                    } else navController.navigate("home")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Finish")
                    }
                }

                5 -> {
                    Text(tempstr.value, style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(tempsubheading.value, style = MaterialTheme.typography.headlineSmall)
                }
            }
        }
    }
}
