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
import androidx.compose.runtime.LaunchedEffect
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

@Composable
fun Signup(navController: NavController, dataManager: DataManager) {
    // State for user inputs
    val name = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val age = remember { mutableStateOf("") }
    val gender = remember { mutableStateOf("Male") }
    val height = remember { mutableStateOf("") }
    val weight = remember { mutableStateOf("") }
    val fitnessGoal = remember { mutableStateOf("General Fitness & Well-being") }
    val preferredWorkoutType = remember { mutableStateOf("Home Workouts (Minimal Equipment)") }
    val comfortLevel = remember { mutableStateOf("Beginner") }
    val serverUrl = remember { mutableStateOf("https://test.ion606.com") }
    val tempstr = remember { mutableStateOf("Loading...") }
    val tempsubheading = remember { mutableStateOf("") }

    // State for password visibility
    val passwordVisible = remember { mutableStateOf(false) }

    val genders = listOf("Male", "Female", "Non-Binary", "Other")
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

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Create Your Account",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Name
            OutlinedTextField(
                value = name.value,
                onValueChange = { name.value = it },
                label = { Text("Name") },
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Email
            OutlinedTextField(
                value = email.value,
                onValueChange = { email.value = it },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Password
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
            Spacer(modifier = Modifier.height(24.dp))

            // Next Button
            Button(
                onClick = {
                    // Validate inputs here (optional)
                    navController.navigate("signupStep2")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Next")
            }
        }
    }

    // Navigation to Step 2
    // Implemented as a separate composable for step-by-step input
    // For simplicity, assuming a single composable handles steps
    // Alternatively, use separate screens for each step
}

@Composable
fun SignupStep2(navController: NavController, dataManager: DataManager) {
    // State for user inputs
    val age = remember { mutableStateOf("") }
    val gender = remember { mutableStateOf("Male") }
    val height = remember { mutableStateOf("") }
    val weight = remember { mutableStateOf("") }
    val fitnessGoal = remember { mutableStateOf("General Fitness & Well-being") }
    val preferredWorkoutType = remember { mutableStateOf("Home Workouts (Minimal Equipment)") }
    val comfortLevel = remember { mutableStateOf("Beginner") }
    val serverUrl = remember { mutableStateOf("https://test.ion606.com") }
    val tempstr = remember { mutableStateOf("Loading...") }
    val tempsubheading = remember { mutableStateOf("") }

    // State for dropdowns
    val genders = listOf("Male", "Female", "Non-Binary", "Other")
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

    // State for password visibility
    val passwordVisible = remember { mutableStateOf(false) }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Additional Information",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Age
            OutlinedTextField(
                value = age.value,
                onValueChange = { age.value = it },
                label = { Text("Age") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Gender
            Dropdown("Gender", genders, gender)
            Spacer(modifier = Modifier.height(16.dp))

            // Height
            OutlinedTextField(
                value = height.value,
                onValueChange = { height.value = it },
                label = { Text("Height (cm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Weight
            OutlinedTextField(
                value = weight.value,
                onValueChange = { weight.value = it },
                label = { Text("Weight (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Next Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = { navController.navigateUp() }) {
                    Text("Back")
                }
                Button(onClick = { navController.navigate("signupStep3") }) {
                    Text("Next")
                }
            }
        }
    }
}

@Composable
fun SignupStep3(navController: NavController, dataManager: DataManager) {
    // State for user inputs
    val fitnessGoal = remember { mutableStateOf("General Fitness & Well-being") }
    val preferredWorkoutType = remember { mutableStateOf("Home Workouts (Minimal Equipment)") }
    val comfortLevel = remember { mutableStateOf("Beginner") }
    val serverUrl = remember { mutableStateOf("https://test.ion606.com") }
    val tempstr = remember { mutableStateOf("Loading...") }
    val tempsubheading = remember { mutableStateOf("") }

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

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Set Your Preferences",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Fitness Goal
            Dropdown("Fitness Goal", goals, fitnessGoal)
            Spacer(modifier = Modifier.height(16.dp))

            // Preferred Workout Type
            Dropdown("Preferred Workout Type", workoutTypes, preferredWorkoutType)
            Spacer(modifier = Modifier.height(16.dp))

            // Comfort Level
            Dropdown("Comfort Level", comfortLevels, comfortLevel)
            Spacer(modifier = Modifier.height(24.dp))

            // Finish Button
            Button(
                onClick = {
                    // Proceed to server setup and account creation
                    navController.navigate("signupFinish")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Next")
            }
        }
    }
}

@Composable
fun SignupFinish(navController: NavController, dataManager: DataManager) {
    // Collect all necessary user data
    // Assuming previous steps have stored their states in a shared ViewModel or passed via Nav arguments
    // For simplicity, using local states here

    val name = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val age = remember { mutableStateOf("") }
    val gender = remember { mutableStateOf("Male") }
    val height = remember { mutableStateOf("") }
    val weight = remember { mutableStateOf("") }
    val fitnessGoal = remember { mutableStateOf("General Fitness & Well-being") }
    val preferredWorkoutType = remember { mutableStateOf("Home Workouts (Minimal Equipment)") }
    val comfortLevel = remember { mutableStateOf("Beginner") }
    val serverUrl = remember { mutableStateOf("https://test.ion606.com") }
    val tempstr = remember { mutableStateOf("Creating account...") }
    val tempsubheading = remember { mutableStateOf("") }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(tempstr.value, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text(tempsubheading.value, style = MaterialTheme.typography.bodyMedium)
        }
    }

    // Account creation logic
    LaunchedEffect(Unit) {
        tempstr.value = "Creating account..."

        val userData = mapOf(
            "name" to name.value.trim(),
            "email" to email.value.trim(),
            "password" to password.value.trim(),
            "age" to age.value.trim(),
            "gender" to gender.value.trim(),
            "height" to height.value.trim(),
            "weight" to weight.value.trim(),
            "fitnessGoal" to fitnessGoal.value.trim(),
            "preferredWorkoutType" to preferredWorkoutType.value.trim(),
            "comfortLevel" to comfortLevel.value.trim(),
        )

        val (success, response) = dataManager.createAccount(userData, serverUrl.value)

        if (success) {
            navController.navigate("home") {
                popUpTo("signup") { inclusive = true }
            }
            // Optionally, show a welcome message or tutorial
        } else {
            tempstr.value = "Signup failed!"
            tempsubheading.value = "Error: ${response ?: "Unknown error"}"
            // Optionally, navigate back or allow retry
        }
    }
}

@Composable
fun Dropdown(
    label: String,
    options: List<String>,
    selectedOption: MutableState<String>,
    onSelected: ((String) -> Unit)? = null
) {
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
                        if (onSelected != null) onSelected(option)
                    },
                    text = { Text(option) }
                )
            }
        }
    }
}

