package com.ion606.workoutapp.screens.logs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ion606.workoutapp.dataObjects.ExerciseMeasureType
import com.ion606.workoutapp.dataObjects.ParsedExercise
import com.ion606.workoutapp.dataObjects.Workout
import com.ion606.workoutapp.dataObjects.convertActiveExerciseToParsed
import com.ion606.workoutapp.dataObjects.SuperSet


@Composable
fun MinimalistWorkoutCard(
    workout: Workout
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Simplified workout header
            Text(
                text = "Workout on ${formatTimestamp(workout.createdAt)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFE0E0E0)
            )
            Spacer(modifier = Modifier.height(4.dp))

            workout.supersets.map {
                MinimalistSupersetDetails(it)
            }
        }
    }
}

@Composable
fun MinimalistExerciseDetails(exercise: ParsedExercise) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Simplified title
        Text(
            text = exercise.title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFE0E0E0)
        )

        // Main details in a compact format without descriptions
        Text(
            text = "Type: ${exercise.type} | Body Part: ${exercise.bodyPart}",
            fontSize = 12.sp,
            color = Color(0xFFB0B0B0)
        )

        Text(
            text = "${if (ExerciseMeasureType.useTime(exercise.measureType)) "Time-Based" else "Reps-Based"}: ${exercise.inset?.filter { it.isDone }?.size}/${exercise.inset?.size} sets",
            fontSize = 12.sp,
            color = Color(0xFFE0E0E0)
        )
    }
}

@Composable
fun MinimalistSupersetDetails(superset: SuperSet) {
    Column(
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        // Optional: You can choose to remove the superset label if desired
        Text(
            text = "Superset",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFB0B0B0)
        )
        Spacer(modifier = Modifier.height(2.dp))

        // Display exercises in the superset
        superset.exercises.forEach { activeExercise ->
            val parsedExercise = convertActiveExerciseToParsed(activeExercise)
            parsedExercise.exercises.forEach { exercise ->
                MinimalistExerciseDetails(exercise)
            }
        }
    }
}
