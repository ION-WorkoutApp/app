package com.ion606.workoutapp.screens.user

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ion606.workoutapp.dataObjects.User.SanitizedUserDataObj
import com.ion606.workoutapp.helpers.Alerts
import com.ion606.workoutapp.managers.UserManager
import kotlinx.coroutines.launch
import androidx.compose.material3.CenterAlignedTopAppBar as TopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoScreen(
    navController: NavController, userManager: UserManager
) {
    val coroutineScope = rememberCoroutineScope()
    val user = userManager.getAllUserData() ?: return

    // State variables
    var email by remember { mutableStateOf(user.email) }
    var password by remember { mutableStateOf("") } // Handle securely
    var name by remember { mutableStateOf(user.name) }
    var age by remember { mutableStateOf(user.age.toString()) }
    var gender by remember { mutableStateOf(user.gender) }
    var height by remember { mutableStateOf(user.height.toString()) }
    var weight by remember { mutableStateOf(user.weight.toString()) }

    val alertmsg = remember { mutableStateOf(Pair<String, String?>("", "")) }

    if (alertmsg.value.first.isNotEmpty()) {
        Alerts.ShowAlert(
            onClick = { alertmsg.value = Pair("", "") },
            title = alertmsg.value.first,
            text = alertmsg.value.second ?: "",
            oneButton = true
        )
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text("Personal Information") }, navigationIcon = {
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
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
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
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = gender,
                onValueChange = { gender = it },
                label = { Text("Gender") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = height,
                onValueChange = { height = it },
                label = { Text("Height (cm)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Weight (in ${user.weightUnit})") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Button(
                onClick = {
                    // Validate numeric fields
                    val ageInt = age.toIntOrNull()
                    val heightInt = height.toIntOrNull()
                    val weightInt = weight.toIntOrNull()

                    if (ageInt == null || heightInt == null || weightInt == null) {
                        alertmsg.value = Pair(
                            "Invalid Input",
                            "Please ensure Age, Height, and Weight are valid numbers."
                        )
                        return@Button
                    }

                    // Update user data
                    coroutineScope.launch {
                        val result = userManager.updateUserData(
                            SanitizedUserDataObj(
                                user.copy(
                                    email = email,
                                    name = name,
                                    age = ageInt,
                                    gender = gender,
                                    height = heightInt,
                                    weight = weightInt
                                )
                            )
                        )
                        if (result.first) {
                            alertmsg.value = Pair("Success", "Personal information updated.")
                        } else {
                            alertmsg.value = Pair(
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
