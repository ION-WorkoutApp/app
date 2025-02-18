package com.ion606.workoutapp.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.password
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ion606.workoutapp.managers.DataManager
import com.ion606.workoutapp.managers.UserManager
import kotlinx.coroutines.launch

private const val TAG = "LoginScreen"

@Composable
fun LoginScreen(navController: NavController, dataManager: DataManager, userManager: UserManager) {
    val username = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val serverUrl = remember { mutableStateOf("https://workoutep.ion606.com") }
    val loginMessage = remember { mutableStateOf("") }
    val isDisabled = remember { mutableStateOf(dataManager.isLoggedIn()) }
    val titleText = remember { mutableStateOf("Log In") }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = titleText.value,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = username.value,
                onValueChange = { username.value = it },
                label = { Text("Email") },
                enabled = !isDisabled.value
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password.value,
                onValueChange = { password.value = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                enabled = !isDisabled.value,
                modifier = Modifier.semantics { this.password() }
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = serverUrl.value,
                onValueChange = { serverUrl.value = it },
                label = { Text("Server URL") },
                enabled = !isDisabled.value
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                if (isDisabled.value) return@Button
                isDisabled.value = true
                titleText.value = "Logging In..."

                val loginObj = mapOf(
                    "email" to username.value.trim(),
                    "password" to password.value.trim()
                )

                coroutineScope.launch {
                    val (success, msg) = dataManager.login(loginObj, serverUrl.value)
                    if (success) {
                        loginMessage.value = "Login Successful!"
                        navController.navigate("permissionsredirect")
                    } else {
                        loginMessage.value = "Login Failed: ${msg.orEmpty()}"
                        isDisabled.value = false
                        titleText.value = "Log In"
                    }
                }
            }) {
                Text("Log In")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = loginMessage.value, color = MaterialTheme.colorScheme.error)
        }
    }
}
