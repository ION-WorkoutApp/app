package com.ion606.workoutapp.screens;

import android.util.Log
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
import com.ion606.workoutapp.helpers.Alerts
import com.ion606.workoutapp.managers.DataManager
import com.ion606.workoutapp.screens.settings.Screen

@Composable
fun Signup(dataManager: DataManager, navController: NavController) {
    // state for current step
    val currentStep = remember { mutableStateOf(1) };

    // step 1: basic account info
    val name = remember { mutableStateOf("") };
    val email = remember { mutableStateOf("") };
    val password = remember { mutableStateOf("") };
    val passwordVisible = remember { mutableStateOf(false) };

    // step 2: personal info (plus new essential fields)
    val age = remember { mutableStateOf("") };
    val gender = remember { mutableStateOf("Male") };
    val height = remember { mutableStateOf("") };
    val weight = remember { mutableStateOf("") };
    val weightUnit = remember { mutableStateOf("kg") };
    val distanceUnit = remember { mutableStateOf("km") };

    // step 3: essential preferences + general preferences (required sub-document)
    val fitnessGoal = remember { mutableStateOf("") };
    val preferredWorkoutType = remember { mutableStateOf("") };
    val comfortLevel = remember { mutableStateOf("") };

    // general preferences (required)
    val activityLevel = remember { mutableStateOf("moderate") };
    val preferredWorkoutTime = remember { mutableStateOf("no preference") };
    val workoutFrequency = remember { mutableStateOf("3") };
    val injuriesOrLimitations = remember { mutableStateOf("") }; // comma separated
    val equipmentAccess = remember { mutableStateOf("") }; // comma separated
    val preferredWorkoutEnvironment = remember { mutableStateOf("no preference") };

    // optional preferences (if you want to allow customization during signup)
    val preferredWorkoutDuration = remember { mutableStateOf("30") };
    val exerciseDifficulty = remember { mutableStateOf("beginner") };
    val warmupAndCooldownPreference = remember { mutableStateOf(true) };
    val preferredWorkoutMusic = remember { mutableStateOf("No preference") };

    val stepGoal = remember { mutableStateOf("10000") };
    val waterIntakeGoal = remember { mutableStateOf("2000") };
    val sleepTracking = remember { mutableStateOf(false) };

    val remindersEnabled = remember { mutableStateOf(true) };
    val notificationFrequency = remember { mutableStateOf("daily") };
    val preferredReminderTime = remember { mutableStateOf("08:00 AM") };

    val socialSharing = remember { mutableStateOf(false) };
    val leaderboardParticipation = remember { mutableStateOf(false) };
    val badgesAndAchievements = remember { mutableStateOf("") }; // comma separated

    // finish: server setup and status messages
    val serverUrl = remember { mutableStateOf("https://test.ion606.com") };
    val statusMessage = remember { mutableStateOf("creating account...") };
    val statusSubMessage = remember { mutableStateOf("") };
    val showDebugAlert = remember { mutableStateOf(false) };
    val confed = remember { mutableStateOf(false) };

    // dropdown options
    val genderOptions = listOf("Male", "Female", "Non-Binary", "Other");
    val weightUnitOptions = listOf("kg", "lbs");
    val distanceUnitOptions = listOf("km", "miles");
    val activityLevelOptions = listOf("sedentary", "light", "moderate", "active", "very active");
    val preferredWorkoutTimeOptions = listOf("morning", "afternoon", "evening", "no preference");
    val preferredWorkoutEnvironmentOptions = listOf("gym", "home", "outdoor", "no preference");
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

    val exerciseDifficultyOptions = listOf("beginner", "intermediate", "advanced");
    val notificationFrequencyOptions = listOf("daily", "weekly", "none");

    val counter = remember { mutableStateOf(0) };
    val confirmationCode = remember { mutableStateOf("") };

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (currentStep.value) {
                1 -> {
                    // screen 1: basic account info
                    Text("create your account", style = MaterialTheme.typography.headlineMedium);

                    Spacer(modifier = Modifier.height(24.dp));

                    OutlinedTextField(value = name.value,
                        onValueChange = { name.value = it },
                        label = { Text("name") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1
                    );

                    Spacer(modifier = Modifier.height(16.dp));

                    OutlinedTextField(value = email.value,
                        onValueChange = { email.value = it },
                        label = { Text("email") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1
                    );

                    Spacer(modifier = Modifier.height(16.dp));

                    OutlinedTextField(value = password.value,
                        onValueChange = { password.value = it },
                        label = { Text("password") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { this.password() },
                        maxLines = 1,
                        visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image =
                                if (passwordVisible.value) Icons.Filled.Visibility else Icons.Filled.VisibilityOff;
                            val description =
                                if (passwordVisible.value) "hide password" else "show password";
                            IconButton(onClick = {
                                passwordVisible.value = !passwordVisible.value
                            }) {
                                Icon(imageVector = image, contentDescription = description);
                            }
                        });

                    Spacer(modifier = Modifier.height(24.dp));

                    OutlinedTextField(value = serverUrl.value,
                        onValueChange = { serverUrl.value = it },
                        label = { Text("Server URL") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1
                    );

                    Button(
                        onClick = { currentStep.value = 2 }, modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("next");
                    }
                }

                2 -> {
                    // screen 2: personal info
                    Text("personal information", style = MaterialTheme.typography.headlineMedium);
                    Spacer(modifier = Modifier.height(24.dp));
                    OutlinedTextField(value = age.value,
                        onValueChange = { age.value = it },
                        label = { Text("age") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1
                    );
                    Spacer(modifier = Modifier.height(16.dp));
                    Dropdown(
                        label = "gender", options = genderOptions, selectedOption = gender
                    );
                    Spacer(modifier = Modifier.height(16.dp));
                    OutlinedTextField(value = height.value,
                        onValueChange = { height.value = it },
                        label = { Text("height (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1
                    );
                    Spacer(modifier = Modifier.height(16.dp));
                    OutlinedTextField(value = weight.value,
                        onValueChange = { weight.value = it },
                        label = { Text("weight ${weightUnit.value}") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1
                    );
                    Spacer(modifier = Modifier.height(16.dp));
                    Dropdown(
                        label = "weight unit",
                        options = weightUnitOptions,
                        selectedOption = weightUnit
                    );
                    Spacer(modifier = Modifier.height(16.dp));
                    Dropdown(
                        label = "distance unit",
                        options = distanceUnitOptions,
                        selectedOption = distanceUnit
                    );
                    Spacer(modifier = Modifier.height(24.dp));
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(onClick = { currentStep.value = 1 }) {
                            Text("back");
                        }
                        Button(onClick = { currentStep.value = 3 }) {
                            Text("next");
                        }
                    }
                }

                3 -> {
                    // screen 3: essential & general preferences
                    Text("preferences", style = MaterialTheme.typography.headlineMedium);
                    Spacer(modifier = Modifier.height(24.dp));
                    Dropdown(
                        label = "fitness goal", options = goals, selectedOption = fitnessGoal
                    );
                    Spacer(modifier = Modifier.height(16.dp));
                    Dropdown(
                        label = "preferred workout type",
                        options = workoutTypes,
                        selectedOption = preferredWorkoutType
                    );

                    Spacer(modifier = Modifier.height(16.dp));

                    Dropdown(
                        label = "comfort level",
                        options = comfortLevels,
                        selectedOption = comfortLevel
                    );

                    Spacer(modifier = Modifier.height(24.dp));
                    Text("general preferences", style = MaterialTheme.typography.headlineSmall);
                    Spacer(modifier = Modifier.height(16.dp));
                    Dropdown(
                        label = "activity level",
                        options = activityLevelOptions,
                        selectedOption = activityLevel
                    );
                    Spacer(modifier = Modifier.height(16.dp));
                    Dropdown(
                        label = "preferred workout time",
                        options = preferredWorkoutTimeOptions,
                        selectedOption = preferredWorkoutTime
                    );
                    Spacer(modifier = Modifier.height(16.dp));
                    OutlinedTextField(
                        value = workoutFrequency.value,
                        onValueChange = { workoutFrequency.value = it },
                        label = { Text("workout frequency (days per week)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1
                    );
                    Spacer(modifier = Modifier.height(16.dp));
                    Dropdown(
                        label = "preferred workout environment",
                        options = preferredWorkoutEnvironmentOptions,
                        selectedOption = preferredWorkoutEnvironment
                    );
                    Spacer(modifier = Modifier.height(24.dp));
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(onClick = { currentStep.value = 2 }) {
                            Text("back");
                        }
                        Button(onClick = { currentStep.value = 4 }) {
                            Text("finish");
                        }
                    }
                }

                4 -> {
                    LaunchedEffect(Unit) {
                        dataManager.genCode(email.value, serverUrl.value);
                    }

                    // screen 4: email confirmation code
                    // state for confirmation code input
                    val sendConfCode = remember { mutableStateOf(false) };
                    val showErrorAlert = remember { mutableStateOf(false) };

                    if (sendConfCode.value) {
                        LaunchedEffect(Unit) {
                            val r = dataManager.testConfCode(
                                email.value, confirmationCode.value, serverUrl.value
                            );
                            if (!r.first) statusSubMessage.value =
                                "Incorrect Confirmation Code, you have ${3 - counter.value} attempts left";
                            else currentStep.value = 5;
                            sendConfCode.value = false;
                        }
                    } else if (showErrorAlert.value) {
                        Alerts.ShowAlert(title = "error",
                            text = "you have entered the wrong confirmation code too many times. please try again later.",
                            oneButton = true,
                            onClick = {
                                showErrorAlert.value = false;
                                navController.navigate(Screen.RestartApp.route);
                            });
                    }

                    Text("email confirmation", style = MaterialTheme.typography.headlineMedium);
                    Spacer(modifier = Modifier.height(24.dp));

                    OutlinedTextField(
                        value = confirmationCode.value,
                        onValueChange = { confirmationCode.value = it },
                        label = { Text("confirmation code") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1
                    );

                    Spacer(modifier = Modifier.height(16.dp));

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(onClick = { currentStep.value = 3 }) {
                            Text("back");
                        }
                        Button(onClick = {
                            if (counter.value >= 3) showErrorAlert.value = true;
                            else {
                                sendConfCode.value = true;
                                counter.value++;
                            }
                        }) {
                            Text("confirm");
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp));
                    Text(statusSubMessage.value, style = MaterialTheme.typography.headlineMedium);
                }

                5 -> {
                    // screen 4: account creation
                    Text(statusMessage.value, style = MaterialTheme.typography.headlineLarge);
                    Spacer(modifier = Modifier.height(16.dp));
                    Text(statusSubMessage.value, style = MaterialTheme.typography.headlineMedium);

                    if (showDebugAlert.value) {
                        Alerts.ShowAlert(title = "debug mode",
                            text = "debug mode is enabled on the server, this means that your data is unencrypted and can be read by admins. are you sure you want to continue?",
                            onClick = {
                                if (it) {
                                    confed.value = true;
                                    statusSubMessage.value = "";
                                    showDebugAlert.value = false;
                                } else navController.navigate(Screen.Home.route);
                            });
                    }

                    LaunchedEffect("FinalSignupCheck") {
                        // debug check
                        val r = dataManager.checkDebugMode(serverUrl.value);

                        if (r.first) showDebugAlert.value = true;
                        else confed.value = true

                    }
                    LaunchedEffect(confed.value) {
                        // sloppy fix
                        if (confed.value) {
                            statusMessage.value = "creating account...";

                            // assemble general preferences
                            val generalPreferences = mapOf("activityLevel" to activityLevel.value,
                                "preferredWorkoutTime" to preferredWorkoutTime.value,
                                "workoutFrequency" to (workoutFrequency.value.trim().toIntOrNull()
                                    ?: 3),
                                "injuriesOrLimitations" to injuriesOrLimitations.value.split(",")
                                    .map { it.trim() }.filter { it.isNotEmpty() },
                                "equipmentAccess" to equipmentAccess.value.split(",")
                                    .map { it.trim() }.filter { it.isNotEmpty() },
                                "preferredWorkoutEnvironment" to preferredWorkoutEnvironment.value
                            );
                            // assemble optional preferences (using defaults if not set)
                            val workoutPreferences = mapOf(
                                "preferredWorkoutDuration" to (preferredWorkoutDuration.value.trim()
                                    .toIntOrNull() ?: 30),
                                "exerciseDifficulty" to exerciseDifficulty.value,
                                "warmupAndCooldownPreference" to warmupAndCooldownPreference.value,
                                "preferredWorkoutMusic" to preferredWorkoutMusic.value
                            );
                            val progressTracking = mapOf(
                                "stepGoal" to (stepGoal.value.trim().toIntOrNull() ?: 10000),
                                "waterIntakeGoal" to (waterIntakeGoal.value.trim().toIntOrNull()
                                    ?: 2000),
                                "sleepTracking" to sleepTracking.value
                            );
                            val notifications = mapOf(
                                "remindersEnabled" to remindersEnabled.value,
                                "notificationFrequency" to notificationFrequency.value,
                                "preferredReminderTime" to preferredReminderTime.value
                            );
                            val socialPreferences = mapOf("socialSharing" to socialSharing.value,
                                "leaderboardParticipation" to leaderboardParticipation.value,
                                "badgesAndAchievements" to badgesAndAchievements.value.split(",")
                                    .map { it.trim() }.filter { it.isNotEmpty() });

                            val weightTransformed = if (weightUnit.value == "lbs") {
                                (weight.value.trim().toIntOrNull() ?: 0) * 0.453592
                            } else {
                                weight.value.trim().toIntOrNull() ?: 0
                            }

                            val userData = mapOf(
                                "code" to confirmationCode.value,
                                "name" to name.value.trim(),
                                "email" to email.value.trim(),
                                "password" to password.value.trim(),
                                "age" to (age.value.trim().toIntOrNull() ?: 0),
                                "gender" to gender.value,
                                "height" to (height.value.trim().toIntOrNull() ?: 0),
                                "weight" to weightTransformed,
                                "weightUnit" to weightUnit.value,
                                "distanceUnit" to distanceUnit.value,
                                "fitnessGoal" to fitnessGoal.value.trim(),
                                "preferredWorkoutType" to preferredWorkoutType.value.trim(),
                                "comfortLevel" to comfortLevel.value.trim(),
                                "generalPreferences" to generalPreferences,
                                "workoutPreferences" to workoutPreferences,
                                "progressTracking" to progressTracking,
                                "notifications" to notifications,
                                "socialPreferences" to socialPreferences
                            );

                            // create account via dataManager
                            val (success, response) = dataManager.createAccount(
                                userData, serverUrl.value
                            );

                            if (success) {
                                navController.navigate("details");
                            } else {
                                statusMessage.value = "signup failed!";
                                statusSubMessage.value = response ?: "unknown error";
                            }
                        } else Log.d("SignupScreen", "not confed")
                    }
                }
            }
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
    val expanded = remember { mutableStateOf(false) };

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(value = selectedOption.value,
            onValueChange = { },
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { expanded.value = !expanded.value }) {
                    Icon(
                        imageVector = if (expanded.value) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = "dropdown"
                    );
                }
            });
        DropdownMenu(expanded = expanded.value, onDismissRequest = { expanded.value = false }) {
            options.forEach { option ->
                DropdownMenuItem(onClick = {
                    selectedOption.value = option;
                    expanded.value = false;
                    onSelected?.invoke(option);
                }, text = { Text(option) });
            }
        }
    }
}
