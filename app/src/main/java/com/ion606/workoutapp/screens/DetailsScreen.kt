package com.ion606.workoutapp.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ion606.workoutapp.BuildConfig
import com.ion606.workoutapp.managers.DataManager
import com.ion606.workoutapp.managers.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "DetailsScreen"

@Composable
fun DetailsScreen(navController: NavController, dataManager: DataManager, userManager: UserManager) {
    val isLoggedIn = dataManager.isLoggedIn()
    val mainText = remember { mutableStateOf("") }
    val subText = remember { mutableStateOf("") }
    val fetchFailed = remember { mutableStateOf(false) }

    LaunchedEffect(isLoggedIn) {
        try {
            if (BuildConfig.SENSITIVE_LOGGING_ENABLED) Log.d("MAIN", "LOGGING SENSITIVE DATA (to change this switch BuildConfig.SENSITIVE_LOGGING_ENABLED to false)")
            
            withContext(Dispatchers.IO) {
                dataManager.login()
                Log.d(TAG, "Logged in successfully on ${Thread.currentThread().name}")

                val serverPingSuccess = dataManager.pingServer()
                if (!serverPingSuccess) {
                    withContext(Dispatchers.Main) {
                        mainText.value = "Failed to connect to server"
                        subText.value = "Please check your internet connection.\nIf you're the server admin, please check the server logs.\n\naddr: ${dataManager.loadURL()}"
                        fetchFailed.value = true
                    }
                    Log.d(TAG, "Failed to ping server")
                    return@withContext
                } else {
                    Log.d(TAG, "DEBUG: server pinged successfully on ${Thread.currentThread().name}")
                }

                when (val result = userManager.fetchUserData()) {
                    is UserManager.FetchUserDataResult.Success -> {
                        val userData = result.data
                        if (userData != null) {
                            withContext(Dispatchers.Main) {
                                userManager.loadData(userData)
                                mainText.value = "Welcome ${userData.name}!"
                                subText.value = "User data loaded successfully."
                                Log.d(TAG, "DONE! $userData on ${Thread.currentThread().name}")
                                navController.navigate("workout");
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                mainText.value = "Failed to parse user data"
                                subText.value = "User data is null."
                                fetchFailed.value = true
                            }
                            Log.d(TAG, "User data is null")
                        }
                    }
                    is UserManager.FetchUserDataResult.Error -> {
                        withContext(Dispatchers.Main) {
                            mainText.value = "Failed to fetch user data"
                            subText.value = result.message
                            fetchFailed.value = true
                        }
                        Log.e(TAG, "Error fetching user data: ${result.message}")
                    }
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                mainText.value = "An error occurred"
                subText.value = e.localizedMessage ?: "Unknown error"
                fetchFailed.value = true
            }
            Log.e(TAG, "Exception during network operations", e)
        }
    }

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
                text = mainText.value,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = subText.value,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (fetchFailed.value) {
                Button(onClick = {
                    navController.navigate("restart_app")
                }) {
                    Text("Reload App")
                }

                Button(onClick = {
                    dataManager.clearCache()
                    navController.navigate("login_signup")
                }) {
                    Text("Log Out and Try Again")
                }
            }
        }
    }
}
