package com.ion606.workoutapp.screens

//import androidx.compose.material3.ModalBottomSheetState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ion606.workoutapp.R
import com.ion606.workoutapp.dataObjects.SuperSetDao
import com.ion606.workoutapp.screens.settings.Screen


private const val TAG = "WorkoutHomeScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutHomeScreen(navController: NavController, dao: SuperSetDao) {
    Scaffold(topBar = { WorkoutTopBar() },
        bottomBar = { WorkoutBottomBar(navController, 0) }) { innerPadding ->

        val isLoading = remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            if (dao.size() > 0) navController.navigate("active_workout")
            else isLoading.value = false
        }

        if (isLoading.value) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Loading...")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { navController.navigate("active_workout") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("New/Saved Workout")
            }

            Button(
                onClick = { TODO() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                enabled = false
            ) {
                Text("Generate Workout for Me")
            }

            Button(
                onClick = { navController.navigate(Screen.UserStatsScreen.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("View Stats")
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutTopBar() {
    TopAppBar(title = {
        Text(
            text = "Workout App", style = MaterialTheme.typography.headlineSmall
        )
    })
}


@Composable
fun WorkoutBottomBar(navController: NavController, isActive: Int = 0) {
    BottomAppBar {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 5.dp, end = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButtonWithLabel(
                iconResId = R.drawable.ic_workout,
                label = "Workout",
                onClick = { navController.navigate(Screen.Workout.route) },
                isActive = (isActive == 0)
            )
            IconButtonWithLabel(
                iconResId = R.drawable.ic_log,
                label = "Log",
                onClick = { navController.navigate(Screen.Log.route) },
                isActive = (isActive == 1)
            )
            IconButtonWithLabel(
                iconResId = R.drawable.ic_profile,
                label = "Profile",
                onClick = { navController.navigate(Screen.Profile.route) },
                isActive = (isActive == 2)
            )
        }
    }
}

@Composable
fun IconButtonWithLabel(
    iconResId: Int, label: String, onClick: () -> Unit, isActive: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        androidx.compose.material3.IconButton(onClick = onClick) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = label,
                tint = if (isActive) Color.Cyan else Color.LightGray
            )
        }
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}
