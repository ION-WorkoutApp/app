package com.ion606.workoutapp.screens.user

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ion606.workoutapp.managers.DataManager
import com.ion606.workoutapp.screens.WorkoutBottomBar
import androidx.compose.material3.CenterAlignedTopAppBar as TopAppBar


sealed class Screen(val route: String) {
    // Settings Screens
    data object MasterSettings : Screen("master_settings")
    data object PersonalInfo : Screen("personal_info")
    data object GeneralPreferences : Screen("general_preferences")
    data object ProgressTracking : Screen("progress_tracking")
    data object Notifications : Screen("notifications")
    data object SocialPreferences : Screen("social_preferences")
    data object DangerZone : Screen("danger_zone")

    // Main Application Screens
    data object Home : Screen("home")
    data object Details : Screen("details")
    data object LoginSignup : Screen("login_signup")
    data object Signup : Screen("signup")
    data object PermissionsRedirect : Screen("permissionsredirect")
    data object Login : Screen("login")
    data object Workout : Screen("workout")
    data object ActiveWorkout : Screen("active_workout")
    data object RestartApp : Screen("restart_app")
    data object ExitApp : Screen("exit_app")
    data object Log : Screen("log")
    data object Profile : Screen("profile")
    data object Permissions : Screen("permissions")
    data object AccountSettings : Screen("accountSettings")

    // Fallback Route
    data object NotFound : Screen("not_found")
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasterSettingsScreen(dataManager: DataManager, navController: NavController) {
    var logout by remember { mutableStateOf(false) }
    if (logout) LaunchedEffect(Unit) { dataManager.logout(navController) }

    Scaffold(topBar = {
        TopAppBar(title = { Text("Settings") }, actions = {
            IconButton(
                modifier = Modifier.padding(horizontal = 16.dp),
                onClick = { logout = true },
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Logout,
                    tint = Color.Red,
                    contentDescription = "Logout"
                )
            }
        })
    }, bottomBar = {
        WorkoutBottomBar(navController, 2)
    }, content = { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SettingsItem(title = "Personal Information",
                    onClick = { navController.navigate(Screen.PersonalInfo.route) })
            }
            item {
                SettingsItem(title = "General Preferences",
                    onClick = { navController.navigate(Screen.GeneralPreferences.route) })
            }
            item {
                SettingsItem(title = "Notifications",
                    onClick = { navController.navigate(Screen.Notifications.route) })
            }
            item {
                SettingsItem(title = "Social Preferences",
                    onClick = { navController.navigate(Screen.SocialPreferences.route) })
            }

            item {
                SettingsItem(
                    title = "Danger Zone",
                    onClick = { navController.navigate(Screen.DangerZone.route) },
                    isDangerZone = true
                )
            }
        }
    })
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
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isDangerZone) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
