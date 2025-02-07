package com.ion606.workoutapp.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ion606.workoutapp.MainActivity
import com.ion606.workoutapp.RouteNotFoundScreen
import com.ion606.workoutapp.dataObjects.SuperSetDao
import com.ion606.workoutapp.helpers.NotificationManager
import com.ion606.workoutapp.managers.DataManager
import com.ion606.workoutapp.managers.SyncManager
import com.ion606.workoutapp.managers.UserManager
import com.ion606.workoutapp.screens.activeExercise.ExerciseScreen
import com.ion606.workoutapp.screens.logs.LogScreen
import com.ion606.workoutapp.screens.user.DangerZoneScreen
import com.ion606.workoutapp.screens.user.GeneralPreferencesScreen
import com.ion606.workoutapp.screens.user.MasterSettingsScreen
import com.ion606.workoutapp.screens.user.NotificationsScreen
import com.ion606.workoutapp.screens.user.PersonalInfoScreen
import com.ion606.workoutapp.screens.user.Screen
import com.ion606.workoutapp.screens.user.SocialPreferencesScreen


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun SettingsNavGraph(
    navController: NavHostController,
    userManager: UserManager,
    innerPadding: PaddingValues,
    dataManager: DataManager,
    sm: SyncManager,
    permissions: PermissionsManager,
    dao: SuperSetDao,
    nhelper: NotificationManager,
    runDebugChecks: MutableState<Boolean>,
    context: MainActivity
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = Modifier.padding(innerPadding)
    ) {
        // Settings Screens
        composable(Screen.Profile.route) {
            MasterSettingsScreen(dataManager, navController)
        }

        composable(Screen.PersonalInfo.route) {
            PersonalInfoScreen(navController, userManager)
        }

        composable(Screen.GeneralPreferences.route) {
            GeneralPreferencesScreen(navController, userManager)
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(navController, userManager)
        }

        composable(Screen.SocialPreferences.route) {
            SocialPreferencesScreen(navController, userManager)
        }

        composable(Screen.DangerZone.route) {
            val user = userManager.getAllUserData() ?: return@composable
            DangerZoneScreen(userManager, navController, context, user)
        }

        // Main Application Screens
        composable(Screen.Home.route) {
            val isLoggedIn = dataManager.isLoggedIn()
            LaunchedEffect(isLoggedIn) {
                if (isLoggedIn) navController.navigate(Screen.Details.route)
                else navController.navigate(Screen.LoginSignup.route)
            }
        }

        composable(Screen.Details.route) {
            if (!runDebugChecks.value) runDebugChecks.value = true
            DetailsScreen(
                navController,
                dataManager,
                userManager,
                context,
                dao,
                nhelper
            )
        }

        composable(Screen.LoginSignup.route) { LoginSignupScreen(navController) }

        composable(Screen.Signup.route) { Signup(dataManager, navController) }

        composable(Screen.PermissionsRedirect.route) {
            permissions.PermissionsScreen(
                context, navController, true
            )
        }

        composable(Screen.Login.route) {
            // Singleton instance
            LoginScreen(
                navController = navController,
                dataManager = dataManager,
                userManager
            )
        }

        composable(Screen.Workout.route) {
            WorkoutHomeScreen(navController)
        }

        composable(Screen.ActiveWorkout.route) {
            ExerciseScreen.CreateScreen(
                userManager, sm, dao, navController, context, nhelper
            )
        }

        composable(Screen.UserStatsScreen.route) {
            UserStatsScreen.WorkoutStatsScreen(context, dataManager, navController)
        }

        composable(Screen.RestartApp.route) {
            context.finish()
            context.startActivity(context.intent)
        }

        composable(Screen.ExitApp.route) { context.finish() }

        composable(Screen.Log.route) {
            LogScreen.CreateScreen(
                userManager, sm, navController, context
            )
        }

        composable(Screen.Permissions.route) {
            permissions.PermissionsScreen(context, navController)
        }

        // User Settings Screens
        composable(Screen.AccountSettings.route) {
            PersonalInfoScreen(navController, userManager)
        }

        // Catch-all fallback route
        composable(Screen.NotFound.route) { RouteNotFoundScreen(navController) }
    }

    // Handle unknown routes
    navController.addOnDestinationChangedListener { controller, destination, _ ->
        if (!controller.graph.contains(destination)) controller.navigate(Screen.NotFound.route)
    }
}
