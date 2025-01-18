package com.ion606.workoutapp.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ion606.workoutapp.dataObjects.ActiveExercise


class WorkoutScreen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun WorkoutPage(
        workoutTitle: String = "Today's Workout",
        exercises: List<ActiveExercise>,
        onAddExerciseClick: () -> Unit,
        onCompleteWorkoutClick: () -> Unit,
        onExerciseClick: (ActiveExercise) -> Unit
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = workoutTitle,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Exercise List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(exercises) { exercise ->
                        ExerciseCard(
                            // TODO: Implement set/rep/weight recommendation system
                            onClick = { onExerciseClick(exercise) },
                            exercise = exercise
                        )
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = onAddExerciseClick) {
                        Text("Add Exercise")
                    }
                    Button(
                        onClick = onCompleteWorkoutClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Green
                        )
                    ) {
                        Text("Complete Workout")
                    }
                }
            }
        }
    }

    @Composable
    fun ExerciseCard(
        exercise: ActiveExercise,
        onClick: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            onClick = onClick
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Exercise Name
                Text(
                    text = exercise.exercise.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
                // Exercise Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Sets: ${exercise.sets}")
                    Text("Reps: ${exercise.reps}")
                    Text("Weight: ${exercise.weight} kg")
                }
            }
        }
    }
}