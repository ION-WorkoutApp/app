package com.ion606.workoutapp

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.ion606.workoutapp.helpers.Alerts
import com.ion606.workoutapp.helpers.CheckIfInDebugMode
import com.ion606.workoutapp.helpers.NotificationManager
import com.ion606.workoutapp.helpers.TopScreenMessageText
import com.ion606.workoutapp.managers.DataManager
import com.ion606.workoutapp.managers.SyncManager
import com.ion606.workoutapp.managers.UserManager
import com.ion606.workoutapp.screens.PermissionsManager
import com.ion606.workoutapp.screens.SettingsNavGraph
import com.ion606.workoutapp.screens.activeExercise.WorkoutDatabase
import com.ion606.workoutapp.ui.theme.WorkoutAppTheme


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("UnsafeIntentLaunch")
    override fun onCreate(savedInstanceState: Bundle?) {
        val sm = SyncManager("", this@MainActivity);
        val dataManager = DataManager(this@MainActivity, sm); // Pass the context
        val userManager = UserManager(this@MainActivity, dataManager, sm);
        val dao =
            WorkoutDatabase.getInstance(this@MainActivity)?.superSetDao() ?: return setContent {
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

                val runDebugChecks = remember { mutableStateOf(false) };
                val serverInDebugMode = remember { mutableStateOf(false) };

                if (runDebugChecks.value) CheckIfInDebugMode(sm) {
                    if (it != serverInDebugMode.value) serverInDebugMode.value = it
                }

                if (serverInDebugMode.value) {
                    Log.d("DEBUG", "Server is in debug mode - your data is unencrypted!")
                    Box(modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                        content = {
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Server is in debug mode - your data is unencrypted!",
                                    fontSize = 20.sp
                                )
                            }
                        })
                }

                Scaffold(topBar = {
                    if (serverInDebugMode.value) TopScreenMessageText("Server is in debug mode - your data is unencrypted!")
                }) { innerPadding ->
                    SettingsNavGraph(navController, userManager, innerPadding, dataManager, sm, permissions, dao, nhelper, runDebugChecks, this@MainActivity)

                }
            }
        }
    }
}
