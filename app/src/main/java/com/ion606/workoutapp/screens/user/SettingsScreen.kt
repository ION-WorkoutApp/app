package com.ion606.workoutapp.screens.user

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ion606.workoutapp.screens.WorkoutBottomBar
import androidx.compose.material3.CenterAlignedTopAppBar as TopAppBar


sealed class Screen(val route: String) {
    // Settings Screens
    object MasterSettings : Screen("master_settings")
    object PersonalInfo : Screen("personal_info")
    object GeneralPreferences : Screen("general_preferences")
    object ProgressTracking : Screen("progress_tracking")
    object Notifications : Screen("notifications")
    object SocialPreferences : Screen("social_preferences")
    object DangerZone : Screen("danger_zone")

    // Main Application Screens
    object Home : Screen("home")
    object Details : Screen("details")
    object LoginSignup : Screen("login_signup")
    object Signup : Screen("signup")
    object PermissionsRedirect : Screen("permissionsredirect")
    object Login : Screen("login")
    object Workout : Screen("workout")
    object ActiveWorkout : Screen("active_workout")
    object RestartApp : Screen("restart_app")
    object ExitApp : Screen("exit_app")
    object Log : Screen("log")
    object Profile : Screen("profile")
    object Permissions : Screen("permissions")
    object AccountSettings : Screen("accountSettings")

    // Fallback Route
    object NotFound : Screen("not_found")
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasterSettingsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") }
            )
        },
        bottomBar = {
            WorkoutBottomBar(navController, 2)
        },
        content = { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    SettingsItem(
                        title = "Personal Information",
                        onClick = { navController.navigate(Screen.PersonalInfo.route) }
                    )
                }
                item {
                    SettingsItem(
                        title = "General Preferences",
                        onClick = { navController.navigate(Screen.GeneralPreferences.route) }
                    )
                }
                item {
                    SettingsItem(
                        title = "Notifications",
                        onClick = { navController.navigate(Screen.Notifications.route) }
                    )
                }
                item {
                    SettingsItem(
                        title = "Social Preferences",
                        onClick = { navController.navigate(Screen.SocialPreferences.route) }
                    )
                }

                item {
                    SettingsItem(
                        title = "Danger Zone",
                        onClick = { navController.navigate(Screen.DangerZone.route) },
                        isDangerZone = true
                    )
                }
            }
        }
    )
}


@Composable
fun SettingsItem(title: String, onClick: () -> Unit, isDangerZone: Boolean = false) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = if (isDangerZone) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isDangerZone) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
