package com.ion606.workoutapp.screens.activeExercise

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.ion606.workoutapp.R
import com.ion606.workoutapp.dataObjects.ActiveExercise
import com.ion606.workoutapp.dataObjects.WorkoutViewModel
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

const val CENTER_RAD = 500 // radius for centering the swipe
private const val TAG = "SuperSetBox"

enum class DragDirection { HORIZONTAL, VERTICAL }

//fun doRectanglesOverlap(rect1: Rect, rect2: Rect): Boolean {
//    return rect1.left < rect2.right && rect1.right > rect2.left && rect1.top < rect2.bottom && rect1.bottom > rect2.top
//}
// with margin
fun doRectanglesOverlap(rect1: Rect, rect2: Rect, margin: Float = 20f): Boolean {
    val expandedRect2 = Rect(
        left = rect2.left - margin,
        top = rect2.top - margin,
        right = rect2.right + margin,
        bottom = rect2.bottom + margin
    )
    return rect1.overlaps(expandedRect2)
}


@Composable
fun SuperSetBox(
    superSet: SuperSet,
    viewModel: WorkoutViewModel,
    superSetBounds: MutableMap<String, Rect?>,
    onExerciseClick: (ActiveExercise) -> Unit
) {
    val isExpanded = remember { mutableStateOf(false) }
    val isChildBeingDragged = remember { mutableStateOf(false) }

    Box(modifier = Modifier
        .padding(10.dp)
        .fillMaxWidth()
        .background(color = Color(0xFF1E1E1E), shape = RoundedCornerShape(12.dp))
        .onGloballyPositioned { coordinates ->
            // Store this Superset’s bounding box for collision detection
            superSetBounds[superSet.id] = coordinates.boundsInWindow()
        }) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Superset header
            Row(verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { if (!(isChildBeingDragged.value)) isExpanded.value = !isExpanded.value }) {
                Text(
                    text = "Superset (${superSet.exercises.size} Exercises)",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 18.sp,
                    color = if (superSet.isDone) Color.LightGray else Color.White,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (isExpanded.value) "Collapse" else "Expand",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 14.sp,
                    color = Color(0xFFAAAAAA)
                )
            }

            // If expanded, show exercises
            if (isExpanded.value) {
                Column {
                    for ((index, exercise) in superSet.exercises.withIndex()) {
                        // Determine if this exercise is the current swap target
                        val isSwapTarget = viewModel.currentSwapTargetId.value == exercise.id

                        ExerciseBox(
                            activeSuperset = superSet,
                            exercise = exercise,
                            viewModel = viewModel,
                            superSetBoundsList = superSetBounds,
                            onExerciseClick = {
                                superSet.setCurrentExercise(it)
                                onExerciseClick(it)
                            },
                            isSwapTarget = isSwapTarget, // Pass the swap target flag
                            isChildBeingDragged = isChildBeingDragged
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ExerciseBox(
    activeSuperset: SuperSet,
    exercise: ActiveExercise,
    viewModel: WorkoutViewModel,
    superSetBoundsList: MutableMap<String, Rect?>,
    onExerciseClick: (ActiveExercise) -> Unit,
    isSwapTarget: Boolean, // Indicates if this box is the current swap target
    isChildBeingDragged: MutableState<Boolean>
) {
    val xOffsetAnim = remember { Animatable(0f) }
    val yOffsetAnim = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    var exerciseBoxBounds by remember { mutableStateOf<Rect?>(null) }
    var isDragging by remember { mutableStateOf(false) }
    var dragDirection by remember { mutableStateOf<DragDirection?>(null) }

    if (isDragging) isChildBeingDragged.value = true
    else isChildBeingDragged.value = false

    // Transition for zIndex changes
    val transition = updateTransition(targetState = isDragging, label = "DragTransition")
    val animatedZIndex by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 300) }, label = "ZIndexAnimation"
    ) { dragging ->
        if (dragging) 1f else 0f
    }

    // Animate background color based on dragging state
    val backgroundColor by animateColorAsState(
        targetValue = if (isDragging) Color(0xFF2E2E2E) else Color(0xFF1E1E1E),
        label = "dragBackground"
    )

    // Define highlight color for swap target
    val highlightColor = Color(0xFFFFA500) // Orange color for highlight

    Box(modifier = Modifier
        .padding(5.dp)
        .background(
            color = if (isSwapTarget) highlightColor.copy(alpha = 0.3f)
            else if (dragDirection == DragDirection.HORIZONTAL && xOffsetAnim.value >= CENTER_RAD) Color.Red.copy(alpha = 0.5f)
            else backgroundColor, shape = RoundedCornerShape(12.dp)
        )
        .zIndex(animatedZIndex)
        .onGloballyPositioned { coordinates ->
            exerciseBoxBounds = coordinates.boundsInWindow()
            viewModel.exerciseBounds[exercise.id] = coordinates.boundsInWindow()
        }
        .pointerInput(Unit) {
            detectDragGestures(onDragStart = {
                isDragging = true
                viewModel.startDragging(exercise, activeSuperset)
                Log.d(TAG, "Drag started for exercise: ${exercise.id}")
            }, onDrag = { change, dragAmount ->
                // Fully consume the drag change to prevent gesture conflicts
                change.consumeAllChanges()

                coroutineScope.launch {
                    // Determine drag direction if not already set
                    if (dragDirection == null) {
                        dragDirection = when {
                            abs(dragAmount.x) > abs(dragAmount.y) -> DragDirection.HORIZONTAL

                            else -> DragDirection.VERTICAL
                        }
                        Log.d(TAG, "Drag direction set to: $dragDirection")
                    }

                    // Move only along the chosen axis
                    when (dragDirection) {
                        DragDirection.HORIZONTAL -> {
                            xOffsetAnim.snapTo(xOffsetAnim.value + dragAmount.x)
                        }

                        DragDirection.VERTICAL -> {
                            yOffsetAnim.snapTo(yOffsetAnim.value + dragAmount.y)
                        }

                        null -> {
                            // Should not reach here lmao
                            Log.d(TAG, "Invalid drag direction? What? How?")
                        }
                    }

                    // After moving, check if this box is overlapping with any other box to set as swap target
                    val currentBounds = Rect(
                        left = (exerciseBoxBounds?.left ?: 0f) + xOffsetAnim.value,
                        top = (exerciseBoxBounds?.top ?: 0f) + yOffsetAnim.value,
                        right = (exerciseBoxBounds?.right ?: 0f) + xOffsetAnim.value,
                        bottom = (exerciseBoxBounds?.bottom ?: 0f) + yOffsetAnim.value
                    )

                    // Iterate through all exercises to find overlap
                    var newTargetId: String? = null
                    for (superSet in viewModel.supersets) {
                        for (otherExercise in superSet.exercises) {
                            if (otherExercise.id == exercise.id) continue // Skip self
                            val otherBounds = viewModel.exerciseBounds[otherExercise.id]
                            if (otherBounds != null && doRectanglesOverlap(
                                    currentBounds, otherBounds, margin = 20f
                                )
                            ) {
                                newTargetId = otherExercise.id
                                break
                            }
                        }
                        if (newTargetId != null) break
                    }

                    // Update the ViewModel's current swap target
                    viewModel.setCurrentSwapTargetId(newTargetId)
                }
            }, onDragEnd = {
                Log.d(TAG, "Drag ended for exercise: ${exercise.id}")
                coroutineScope.launch {
                    // remove the exercise if dragged horizontally
                    if (dragDirection == DragDirection.HORIZONTAL && xOffsetAnim.value > CENTER_RAD) {
                        Log.d(TAG, "Removing exercise: ${exercise.id}")
                        viewModel.removeExerciseFromSuperset(activeSuperset, exercise)
                        return@launch
                    }

                    isDragging = false
                    dragDirection = null // Reset drag direction

                    val initialBounds = exerciseBoxBounds
                    if (initialBounds != null) {
                        val finalExerciseRect = Rect(
                            left = initialBounds.left + xOffsetAnim.value,
                            top = initialBounds.top + yOffsetAnim.value,
                            right = initialBounds.right + xOffsetAnim.value,
                            bottom = initialBounds.bottom + yOffsetAnim.value
                        )

                        var exerciseMoved = false

                        // 1. Check if dropped into a different superset
                        for ((sid, superSetBounds) in superSetBoundsList.entries) {
                            val targetSuperSet = viewModel.supersets.find { it.id == sid }
                            if (targetSuperSet == null || superSetBounds == null) continue

                            if (targetSuperSet.id != activeSuperset.id && doRectanglesOverlap(
                                    superSetBounds, finalExerciseRect, margin = 20f
                                )
                            ) {
                                Log.d(
                                    TAG,
                                    "Dragged ${exercise.exercise.title} " + "from ${activeSuperset.id} into ${targetSuperSet.id}"
                                )

                                viewModel.moveExercise(targetSuperSet)
                                exerciseMoved = true
                                break
                            }
                        }

                        // 2. Check if dropped within the original superset's bounds for reordering
                        if (!exerciseMoved) {
                            val originalSupersetBounds = superSetBoundsList[activeSuperset.id]
                            if (originalSupersetBounds != null && doRectanglesOverlap(
                                    originalSupersetBounds, finalExerciseRect, margin = 20f
                                )
                            ) {
                                val fromIndex = activeSuperset.exercises.indexOf(exercise)
                                if (fromIndex != -1) {
                                    val finalCenterX =
                                        finalExerciseRect.left + (finalExerciseRect.width / 2f)
                                    val finalCenterY =
                                        finalExerciseRect.top + (finalExerciseRect.height / 2f)

                                    val closestIndex = activeSuperset.exercises.withIndex()
                                        .filter { it.value.id != exercise.id }
                                        .minByOrNull { indexed ->
                                            val targetBounds =
                                                viewModel.exerciseBounds[indexed.value.id]
                                            if (targetBounds == null) {
                                                Float.MAX_VALUE
                                            } else {
                                                val centerX =
                                                    (targetBounds.left + targetBounds.right) / 2f
                                                val centerY =
                                                    (targetBounds.top + targetBounds.bottom) / 2f
                                                val dx = centerX - finalCenterX
                                                val dy = centerY - finalCenterY
                                                dx * dx + dy * dy // Distance squared
                                            }
                                        }?.index

                                    if (closestIndex != null && closestIndex != fromIndex && closestIndex in activeSuperset.exercises.indices) {
                                        Log.d(
                                            TAG,
                                            "Reordering ${exercise.exercise.title} " + "from index $fromIndex to $closestIndex in ${activeSuperset.id}"
                                        )

                                        viewModel.reorderExercise(
                                            superSet = activeSuperset,
                                            fromIndex = fromIndex,
                                            toIndex = closestIndex
                                        )
                                        exerciseMoved =
                                            true // Consider it as moved since reordering happened
                                    }
                                }
                            }
                        }

                        // 3. If not moved to another superset or reordered, create a new superset
                        if (!exerciseMoved) {
                            Log.d(
                                TAG,
                                "Exercise not moved to any superset or reordered, creating a new superset"
                            )
                            viewModel.createNewSuperset(exercise)
                            viewModel.removeExerciseFromSuperset(activeSuperset, exercise)
                            exerciseMoved = true
                        }

                        viewModel.stopDragging()

                        // Reset swap target
                        viewModel.setCurrentSwapTargetId(null)

                        // Animate the card back to its original position
                        xOffsetAnim.animateTo(0f, animationSpec = spring())
                        yOffsetAnim.animateTo(0f, animationSpec = spring())
                    }
                }
            }, onDragCancel = {
                Log.d(TAG, "Drag canceled for exercise: ${exercise.id}")
                coroutineScope.launch {
                    isDragging = false
                    dragDirection = null
                    viewModel.stopDragging()

                    // Reset swap target
                    viewModel.setCurrentSwapTargetId(null)

                    xOffsetAnim.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(dampingRatio = 0.8f, stiffness = 200f)
                    )
                    yOffsetAnim.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(dampingRatio = 0.8f, stiffness = 200f)
                    )
                }
            })
        }
        .offset {
            IntOffset(
                xOffsetAnim.value.roundToInt(), yOffsetAnim.value.roundToInt()
            )
        }
        .animateContentSize()) {
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !exercise.isDone) {
                    Log.d(TAG, "Exercise clicked: ${exercise.exercise.title}")
                    onExerciseClick(exercise)
                }) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_background),
                contentDescription = "Exercise Image",
                modifier = Modifier
                    .size(64.dp)
                    .padding(end = 16.dp)
            )

            Column(
                verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = exercise.exercise.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 18.sp,
                    color = if (exercise.isDone) Color.LightGray else Color.White
                )

                val secondLinePart = if (exercise.reps != null) {
                    "${exercise.reps!!.size} reps"
                } else {
                    "${exercise.times} each"
                }

                Text(
                    text = "${exercise.sets} sets, $secondLinePart",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 14.sp,
                    color = if (exercise.isDone) Color.Gray else Color(0xFFAAAAAA),
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}
