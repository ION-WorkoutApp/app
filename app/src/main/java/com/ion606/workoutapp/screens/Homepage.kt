package com.ion606.workoutapp.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ion606.workoutapp.managers.DataManager

@Composable
fun HomeScreen(navController: NavController, dataManager: DataManager) {
    val isLoggedIn = dataManager.isLoggedIn()
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) navController.navigate("details");
        else navController.navigate("login_signup");
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to Workout App!",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}
