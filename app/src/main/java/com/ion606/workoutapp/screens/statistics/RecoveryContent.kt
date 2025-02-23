package com.ion606.workoutapp.screens.statistics

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ion606.workoutapp.dataObjects.User.MuscleRecoverIndividual
import com.ion606.workoutapp.dataObjects.User.MuscleRecovery
import com.ion606.workoutapp.dataObjects.User.MuscleRecoverySimple
import com.ion606.workoutapp.dataObjects.User.UserStats
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter


fun convertToSimpleRecovery(detailed: MuscleRecovery): MuscleRecoverySimple {
    fun averageRecovery(muscles: List<MuscleRecoverIndividual>): MuscleRecoverIndividual {
        val avgPercentage = muscles.map { it.recoveryPercentage }.average().toFloat()
        val avgHours = muscles.map { it.personalizedRecoveryHours }.average().toFloat()
        return MuscleRecoverIndividual(
            lastUsed = "Never",  // This would need proper handling
            recoveryPercentage = avgPercentage,
            personalizedRecoveryHours = avgHours
        )
    }

    return MuscleRecoverySimple(
        chest = detailed.chest,
        back = averageRecovery(
            listOf(
                detailed.lats,
                detailed.lowerBack,
                detailed.middleBack,
                detailed.traps
            )
        ),
        legs = averageRecovery(
            listOf(
                detailed.adductors,
                detailed.abductors,
                detailed.calves,
                detailed.glutes,
                detailed.hamstrings,
                detailed.quadriceps
            )
        ),
        arms = averageRecovery(
            listOf(
                detailed.biceps,
                detailed.triceps,
                detailed.forearms
            )
        ),
        core = detailed.abdominals,
        shoulders = averageRecovery(
            listOf(
                detailed.shoulders,
                detailed.neck
            )
        ),
        lastUpdated = detailed.lastUpdated
    )
}

fun muscleRecoveryToMap(
    mr: MuscleRecovery,
    isSimple: Boolean = false
): Map<String, MuscleRecoverIndividual> {
    return if (isSimple) convertToSimpleRecovery(mr).let {
        mapOf(
            "Chest" to it.chest,
            "Back" to it.back,
            "Legs" to it.legs,
            "Arms" to it.arms,
            "Core" to it.core,
            "Shoulders" to it.shoulders
        )
    }
    else mapOf(
        "Abdominals" to mr.abdominals,
        "Adductors" to mr.adductors,
        "Abductors" to mr.abductors,
        "Biceps" to mr.biceps,
        "Triceps" to mr.triceps,
        "Calves" to mr.calves,
        "Chest" to mr.chest,
        "Forearms" to mr.forearms,
        "Glutes" to mr.glutes,
        "Hamstrings" to mr.hamstrings,
        "Lats" to mr.lats,
        "Lower Back" to mr.lowerBack,
        "Middle Back" to mr.middleBack,
        "Traps" to mr.traps,
        "Neck" to mr.neck,
        "Quadriceps" to mr.quadriceps,
        "Shoulders" to mr.shoulders
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RecoveryContent(stats: UserStats) {
    var showSimple by remember { mutableStateOf(false) }
    val recoveryData = remember(stats, showSimple) {
        muscleRecoveryToMap(stats.muscleRecovery, showSimple)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val lastUpdated = stats.muscleRecovery.lastUpdated

        val lastUpdatedText = try {
            val instant = Instant.parse(lastUpdated)
            val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss")
                .withZone(ZoneId.systemDefault())
            formatter.format(instant)
        } catch (e: Exception) {
            Log.e("RecoveryContent", "no last updated time")
            "Unknown"
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "💊 Muscle Recovery",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(Modifier.weight(1f))
                        Switch(
                            checked = showSimple,
                            onCheckedChange = { showSimple = it },
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Text(
                            text = if (showSimple) "Simple" else "Detailed",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Last Updated: $lastUpdatedText",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                recoveryData.entries.sortedByDescending { it.value.recoveryPercentage }
                    .forEach { (muscle, individual) ->
                        RecoveryBar(muscle, individual.recoveryPercentage.toInt())
                    }
            }
        }
    }
}

@Composable
fun RecoveryBar(muscle: String, percentage: Int) {
    val barColor = when {
        percentage >= 80 -> Color(0xFF4CAF50) // Green
        percentage >= 50 -> Color(0xFFFFC107) // Yellow
        else -> Color(0xFFF44336) // Red
    }

    val minTextThreshold = 0.25f // Move text outside if bar is smaller than 25% width

    Column {
        Text(
            text = muscle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Colored recovery bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth(percentage / 100f)
                        .fillMaxHeight()
                        .background(barColor, MaterialTheme.shapes.small),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (percentage / 100f > minTextThreshold) {
                        // Text inside the bar if there's enough space
                        Text(
                            text = "$percentage%",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (percentage >= 50) Color.Black else Color.White,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                if (percentage / 100f <= minTextThreshold) {
                    // Text outside the bar if it's too small
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
