package com.ion606.workoutapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ion606.workoutapp.dataObjects.ActiveExercise
import com.ion606.workoutapp.screens.settings.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSummaryBottomSheet(
    activeExercises: List<ActiveExercise>,
    navController: NavHostController
) {
    val bottomBarHeight = 80.dp
    // Create and configure the sheet state so that it opens fully expanded.
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = {
            navController.navigate(Screen.Home.route)
        },
        sheetState = sheetState,
        properties = ModalBottomSheetProperties(
            shouldDismissOnBackPress = true
        )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Workout Summary", style = MaterialTheme.typography.titleLarge) }
                )
            },
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(bottomBarHeight)
                        .background(Color.Transparent)
                        .offset(y = (-30).dp)
                ) {
                    Button(
                        onClick = {
                            navController.navigate(Screen.Home.route)
                        },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                    ) {
                        Text("Done")
                    }
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(bottom = bottomBarHeight),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    WorkoutSummaryStats(activeExercises = activeExercises)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                items(activeExercises) { exercise ->
                    ExerciseSummaryItem(exercise = exercise)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun WorkoutSummaryStats(activeExercises: List<ActiveExercise>) {
    val totalCalories = activeExercises.sumOf { it.caloriesBurned }
    val totalRestTime = activeExercises.sumOf { it.restTime } / 60
    val totalSets = activeExercises.sumOf { it.sets }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Overall Workout Stats",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Total Calories Burned: ${totalCalories.toInt()} kcal",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Total Rest Time: $totalRestTime minutes",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Total Sets: $totalSets",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun ExerciseSummaryItem(exercise: ActiveExercise) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = exercise.exercise.title,
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Sets: ${exercise.sets} (Done: ${exercise.inset?.count { it.isDone } ?: 0})",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Calories Burned: ${exercise.caloriesBurned.toInt()} kcal",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Total Rest Time: ${exercise.inset?.sumOf { it.restTime } } sec",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
