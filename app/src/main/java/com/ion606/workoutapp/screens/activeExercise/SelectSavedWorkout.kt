import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ion606.workoutapp.dataObjects.Workout
import com.ion606.workoutapp.helpers.Alerts
import com.ion606.workoutapp.managers.SyncManager


@Composable
fun SelectWorkoutPopup(
    workoutsInp: List<Workout>,
    onWorkoutSelected: (Workout) -> Unit,
    onDismissRequest: () -> Unit,
    syncManager: SyncManager,
    context: Context
) {
    var selectedWorkout by remember { mutableStateOf<Workout?>(null) }
    var expandedDropdownId by remember { mutableStateOf<String?>(null) }
    var workouts by remember { mutableStateOf(workoutsInp) }
    val scope = rememberCoroutineScope()

    if (workouts.isEmpty()) {
        return Alerts.ShowAlert(
            { onDismissRequest() },
            "No Saved Workouts",
            "You have not saved any workouts yet. Please save a workout to use it here.",
            true
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { onDismissRequest() }, contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
                .shadow(8.dp, RoundedCornerShape(20.dp)), shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(20.dp)
            ) {
                Text(
                    text = "Select Workout",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                workouts.forEach { workout ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { onWorkoutSelected(workout) },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val workoutName = remember { mutableStateOf(workout.workoutName) }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = workoutName.value,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${workout.exercises.size} exercises",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }

                        Box {
                            IconButton(onClick = {
                                expandedDropdownId =
                                    if (expandedDropdownId == workout.workoutName) null else workout.workoutName
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Options",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            DropdownMenu(expanded = expandedDropdownId == workout.workoutName,
                                onDismissRequest = { expandedDropdownId = null }) {
                                DropdownMenuItem(onClick = {
                                    Log.d("workoutmenu", "Info action triggered")

                                    selectedWorkout = workout
                                    expandedDropdownId = null
                                }, text = { Text("Info") })


                                val triggerRename = remember { mutableStateOf(false) }
                                val renameResult =
                                    remember { mutableStateOf<Pair<Boolean, Any?>?>(null) }

                                if (renameResult.value != null) {
                                    if (renameResult.value!!.first) {
                                        Alerts.ShowAlert(
                                            {
                                                workoutName.value = renameResult.value?.second.toString()
                                                expandedDropdownId = null
                                                renameResult.value = null
                                            },
                                            "Workout Renamed to ${renameResult.value?.second.toString()}",
                                            "Workout has been renamed successfully"
                                        )
                                    } else {
                                        Alerts.ShowAlert(
                                            {
                                                expandedDropdownId = null
                                                renameResult.value = null
                                            },
                                            "Failed to rename workout",
                                            renameResult.value!!.second.toString()
                                        )
                                    }
                                }


                                if (triggerRename.value) {
                                    val newName = remember { mutableStateOf("") }

                                    Alerts.CreateAlertDialog("Rename Workout", context) {
                                        if (!it.isNullOrEmpty()) {
                                            newName.value = it
                                        } else {
                                            renameResult.value = Pair(false, "Name cannot be empty")
                                            triggerRename.value = false
                                        }
                                    }

                                    if (newName.value.isNotEmpty()) {
                                        syncManager.sendDataCB(
                                            scope, mapOf(
                                                "workoutId" to workout.id,
                                                "newName" to newName.value
                                            ), path = "savedworkouts", method = "PUT"
                                        ) { result -> run {
                                            if (result.first) renameResult.value = Pair(true, newName.value)
                                            else renameResult.value = Pair(false, result.second)

                                            newName.value = ""
                                        } }
                                    }
                                }

                                DropdownMenuItem(onClick = {
                                    Log.d("workoutmenu", "Rename action triggered")
                                    triggerRename.value = true
                                }, text = { Text("Rename") })


                                val deleteResult =
                                    remember { mutableStateOf<Pair<Boolean, Any?>?>(null) }
                                if (deleteResult.value != null) {
                                    if (deleteResult.value!!.first) {
                                        Alerts.ShowAlert(
                                            {
                                                expandedDropdownId = null
                                                deleteResult.value = null
                                                workouts = workouts.filter { it.id != workout.id }
                                                if (workouts.isEmpty()) onDismissRequest()
                                            },
                                            "Workout Deleted",
                                            "Workout has been deleted successfully"
                                        )
                                    } else {
                                        Alerts.ShowAlert(
                                            {
                                                expandedDropdownId = null
                                                deleteResult.value = null
                                            },
                                            "Failed to delete workout",
                                            deleteResult.value!!.second.toString()
                                        )
                                    }
                                }

                                DropdownMenuItem(onClick = {
                                    Log.d("workoutmenu", "Delete action triggered")
                                    syncManager.sendDataCB(
                                        scope,
                                        mapOf("workoutId" to workout.id),
                                        path = "savedworkouts",
                                        method = "DELETE"
                                    ) { result ->
                                        deleteResult.value = result
                                    }
                                }, text = { Text("Delete") })
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }

    // Show details dialog when "Info" is selected
    selectedWorkout?.let { workout ->
        WorkoutDetailsDialog(workout = workout, onDismiss = { selectedWorkout = null })
    }
}


@Composable
fun WorkoutDetailsDialog(
    workout: Workout, onDismiss: () -> Unit
) {
    AlertDialog(onDismissRequest = onDismiss, confirmButton = {
        TextButton(onClick = onDismiss) {
            Text("Close")
        }
    }, title = {
        Text(
            text = workout.workoutName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }, text = {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = "Created At: ${workout.createdAt}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.padding(8.dp))
            workout.exercises.forEachIndexed { index, exercise ->
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    Text(
                        text = "${index + 1}. ${exercise.title}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = exercise.description,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    })
}
