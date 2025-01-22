package com.ion606.workoutapp

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ion606.workoutapp.dataObjects.WorkoutDatabase
import com.ion606.workoutapp.helpers.Alerts
import com.ion606.workoutapp.helpers.NotificationManager
import com.ion606.workoutapp.managers.DataManager
import com.ion606.workoutapp.managers.SyncManager
import com.ion606.workoutapp.managers.UserManager
import com.ion606.workoutapp.screens.DetailsScreen
import com.ion606.workoutapp.screens.LoginScreen
import com.ion606.workoutapp.screens.LoginSignupScreen
import com.ion606.workoutapp.screens.PermissionsManager
import com.ion606.workoutapp.screens.Signup
import com.ion606.workoutapp.screens.WorkoutHomeScreen
import com.ion606.workoutapp.screens.activeExercise.ExerciseScreen
import com.ion606.workoutapp.ui.theme.WorkoutAppTheme


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("UnsafeIntentLaunch")
    override fun onCreate(savedInstanceState: Bundle?) {
        val sm = SyncManager("", this@MainActivity);
        val dataManager = DataManager(this@MainActivity, sm); // Pass the context
        val userManager = UserManager(this@MainActivity, dataManager, sm);
        val dao = WorkoutDatabase.getInstance(this@MainActivity)?.activeExerciseDao()
            ?: return setContent {
                Alerts.ShowAlert(
                    { finish() },
                    "Error",
                    "Failed to initialize the database.\nMaybe try clearing all app data through settings and trying again."
                )
            }

        val nhelper = NotificationManager(this@MainActivity);
        val permissions = PermissionsManager();

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WorkoutAppTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "home"
                ) {
                    navController.addOnDestinationChangedListener() { controller, destination, arguments ->
                        if (!controller.graph.contains(destination)) controller.navigate("not_found")
                    }
                    composable("home") {
                        val isLoggedIn = dataManager.isLoggedIn()
                        LaunchedEffect(isLoggedIn) {
                            if (isLoggedIn) navController.navigate("details");
                            else navController.navigate("login_signup");
                        }
                    }
                    composable("details") {
                        DetailsScreen(
                            navController,
                            dataManager,
                            userManager,
                            this@MainActivity,
                            nhelper
                        )
                    }
                    composable("login_signup") { LoginSignupScreen(navController) }
                    composable("signup") { Signup(navController = navController, dataManager) }
                    composable("permissionsredirect") { permissions.PermissionsScreen(this@MainActivity, navController, true) }
                    composable("login") {
                        // put this in one instance for singleton reasons
                        LoginScreen(
                            navController = navController,
                            dataManager = dataManager,
                            userManager
                        );
                    }
                    composable("workout") {
                        WorkoutHomeScreen(
                            navController,
                            dataManager,
                            userManager,
                            nhelper
                        )
                    }
                    composable("active_workout") {
                        ExerciseScreen.CreateScreen(
                            userManager,
                            sm,
                            dao,
                            navController,
                            this@MainActivity,
                            nhelper
                        )
                    }
                    composable("restart_app") {
                        finish();
                        startActivity(intent);
                    }

                    composable("exit_app") { finish(); }

                    composable("log") {
                        com.ion606.workoutapp.screens.LogScreen.CreateScreen(
                            userManager,
                            sm,
                            navController,
                            this@MainActivity
                        )
                    }

                    composable("profile") {
                        com.ion606.workoutapp.screens.UserScreen.CreateScreen(
                            userManager,
                            dataManager,
                            navController,
                            this@MainActivity
                        )
                    }

                    composable("permissions") {
                        permissions.PermissionsScreen(this@MainActivity, navController)
                    }

                    // catch-all fallback route
                    composable("not_found") { RouteNotFoundScreen(navController) }
                }
            }
        }
    }
}
