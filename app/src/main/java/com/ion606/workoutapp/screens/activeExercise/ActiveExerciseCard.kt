package com.ion606.workoutapp.screens.activeExercise

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ion606.workoutapp.R
import com.ion606.workoutapp.dataObjects.ActiveExercise
import com.ion606.workoutapp.dataObjects.ActiveExerciseDao
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

const val CENTER_RAD = 150 // Radius for centering the swipe
private const val TAG = "ExerciseBox"


@Composable
fun ExerciseBox(
    exercise: ActiveExercise,
    openExercise: MutableState<ActiveExercise?>,
    exercises: MutableState<List<ActiveExercise>>,
    dao: ActiveExerciseDao
) {
    val xOffsetAnim = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    val dragThreshold = CENTER_RAD.toFloat()

    val isDone = exercise.isDone

    Box(
        modifier = Modifier
            .padding(5.dp)
            .fillMaxWidth()
    ) {
        // Background red box
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    color = if (isDone) Color.Gray else Color.Red, // Gray if done
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_background),
                contentDescription = "Trash Icon",
                modifier = Modifier
                    .size(64.dp)
                    .padding(16.dp)
            )
        }

        // Swipeable box
        Box(
            modifier = Modifier
                .offset { IntOffset(xOffsetAnim.value.roundToInt(), 0) }
                .background(
                    color = if (isDone) Color.DarkGray else Color(0xFF1E1E1E), // Dark gray if done
                    shape = RoundedCornerShape(12.dp)
                )
                .pointerInput(Unit) {
                    if (!isDone) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                coroutineScope.launch {
                                    xOffsetAnim.snapTo(xOffsetAnim.value + dragAmount)
                                }
                            },
                            onDragEnd = {
                                coroutineScope.launch {
                                    when {
                                        xOffsetAnim.value > dragThreshold -> {
                                            // Remove the item on right swipe
                                            openExercise.value = null
                                            exercises.value = exercises.value.filter { it != exercise }
                                            dao.delete(exercise)
                                        }
                                        xOffsetAnim.value < -dragThreshold -> {
                                            Log.d(TAG, "Left swipe action triggered")
                                        }
                                        else -> {
                                            // Animate back to center using spring animation
                                            xOffsetAnim.animateTo(
                                                targetValue = 0f,
                                                animationSpec = spring(
                                                    dampingRatio = 0.8f,
                                                    stiffness = 200f
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isDone) { // Disable click if done
                        openExercise.value = exercise
                    }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_background),
                    contentDescription = "Exercise Image",
                    modifier = Modifier
                        .size(64.dp)
                        .padding(end = 16.dp)
                )

                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = exercise.exercise.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 18.sp,
                        color = if (isDone) Color.LightGray else Color.White // Lighter text color if done
                    )

                    val secondLinePart =
                        if (exercise.reps != null) "${exercise.reps?.size} reps" else "${exercise.times} each"

                    Text(
                        text = "${exercise.sets} sets, $secondLinePart",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 14.sp,
                        color = if (isDone) Color.Gray else Color(0xFFAAAAAA), // Gray text if done
                        textAlign = TextAlign.Start
                    )
                }
            }
        }
    }
}
