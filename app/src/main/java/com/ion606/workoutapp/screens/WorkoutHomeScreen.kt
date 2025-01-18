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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ion606.workoutapp.R
import com.ion606.workoutapp.managers.DataManager
import com.ion606.workoutapp.managers.UserManager


private const val TAG = "WorkoutHomeScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutHomeScreen(
    navController: NavController,
    dataManager: DataManager,
    userManager: UserManager
) {
    val sheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = { WorkoutTopBar() },
        bottomBar = { WorkoutBottomBar(navController, 0) }
    ) { innerPadding ->
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
                Text("New Workout")
            }
            Button(
                onClick = { TODO() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                enabled = false
            ) {
                Text("Use Saved Workout")
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
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutTopBar() {
    TopAppBar(
        title = {
            Text(
                text = "Workout App",
                style = MaterialTheme.typography.headlineSmall
            )
        }
    )
}


@Composable
fun WorkoutBottomBar(navController: NavController, isActive: Int = 0) {
    BottomAppBar {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButtonWithLabel(
                iconResId = R.drawable.ic_workout,
                label = "Workout",
                onClick = { navController.navigate("workout") },
                isActive = (isActive == 0)
            )
            IconButtonWithLabel(
                iconResId = R.drawable.ic_log,
                label = "Log",
                onClick = { navController.navigate("log") },
                isActive = (isActive == 1)
            )
            IconButtonWithLabel(
                iconResId = R.drawable.ic_profile,
                label = "Profile",
                onClick = { navController.navigate("profile") },
                isActive = (isActive == 2)
            )
        }
    }
}

@Composable
fun IconButtonWithLabel(iconResId: Int, label: String, onClick: () -> Unit, isActive: Boolean = false) {
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
