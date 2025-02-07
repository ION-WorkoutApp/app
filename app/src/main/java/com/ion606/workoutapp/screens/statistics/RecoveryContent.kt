package com.ion606.workoutapp.screens.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ion606.workoutapp.dataObjects.User.MuscleRecoverIndividual
import com.ion606.workoutapp.dataObjects.User.MuscleRecovery
import com.ion606.workoutapp.dataObjects.User.UserStats
import com.ion606.workoutapp.screens.statistics.UserStatsScreen.Companion.DonutChart

fun muscleRecoveryToMap(mr: MuscleRecovery): Map<String, MuscleRecoverIndividual> {
    return mapOf(
        "chest" to mr.chest,
        "back" to mr.back,
        "legs" to mr.legs,
        "arms" to mr.arms,
        "core" to mr.core,
        "shoulders" to mr.shoulders
    )
}

@Composable
fun RecoveryContent(stats: UserStats) {
    val recoveryData = remember(stats) { muscleRecoveryToMap(stats.muscleRecovery) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "💊 Muscle Recovery",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Last Updated ${stats.muscleRecovery.lastUpdated}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    // Donut Chart with recovery percentages
                    DonutChart(
                        data = recoveryData.mapValues { it.value.recoveryPercentage.toInt() },
                        title = "Muscle Readiness"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Detailed Recovery List
                    recoveryData.entries.sortedByDescending { it.value.recoveryPercentage }
                        .forEach { (muscle, individual) ->
                            RecoveryListItem(
                                muscle = muscle,
                                individual = individual
                            )
                        }
                }
            }
        }
    }
}

@Composable
fun RecoveryListItem(muscle: String, individual: MuscleRecoverIndividual) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Muscle indicator
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = muscle.take(3).uppercase(),
                style = MaterialTheme.typography.labelMedium
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = muscle.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Last trained: ${individual.lastUsed}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Recovery percentage indicator
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    color = when {
                        individual.recoveryPercentage >= 80 -> Color(0xFF4CAF50)
                        individual.recoveryPercentage >= 50 -> Color(0xFFFFC107)
                        else -> Color(0xFFF44336)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${individual.recoveryPercentage.toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
        }
    }
}