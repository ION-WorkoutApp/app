package com.ion606.workoutapp.screens.activeExercise

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
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
import com.ion606.workoutapp.dataObjects.ExerciseMeasureType
import com.ion606.workoutapp.dataObjects.SuperSet
import com.ion606.workoutapp.dataObjects.WorkoutViewModel
import kotlinx.coroutines.launch
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.math.abs
import kotlin.math.roundToInt

const val CENTER_RAD = 500 // radius for centering the swipe
private const val TAG = "SuperSetBox"

enum class DragDirection { HORIZONTAL, VERTICAL }

// Function to check if two rectangles overlap with a margin
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
fun GradientConnector() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp) // Adjust height as needed
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        // Enhanced Gradient line with rounded edges
        Box(
            modifier = Modifier
                .width(4.dp) // Increased width for better visibility
                .fillMaxHeight()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFB0BEC5), Color(0xFF78909C)),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    ),
                    shape = RoundedCornerShape(2.dp) // Rounded corners
                )
        )
    }
}

@Composable
fun SuperSetBox(
    superSet: SuperSet,
    viewModel: WorkoutViewModel,
    superSetBounds: MutableMap<String, Rect?>,
    onExerciseClick: (ActiveExercise) -> Unit
) {
    // State to track if any child is being dragged
    val isChildBeingDragged = remember { mutableStateOf(false) }
    val isExpanded = remember { mutableStateOf(true) }

    // Determine if the superset has only one exercise
    val isSingleExercise = superSet.exercises.size == 1

    // Define background color and shape based on whether it's a single exercise
    val backgroundColor = if (isSingleExercise) Color(0xFF2E2E2E) else Color(0xFF1E1E1E)
    val cornerRadius = if (isSingleExercise) 16.dp else 12.dp

    Box(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(cornerRadius)
            )
            .onGloballyPositioned { coordinates ->
                // Store this Superset’s bounding box for collision detection
                superSetBounds[superSet.id] = coordinates.boundsInWindow()
            }
    ) {
        if (isSingleExercise) {
            // Render the single exercise directly without the superset header
            ExerciseBox(
                activeSuperset = superSet,
                exercise = superSet.exercises.first(),
                viewModel = viewModel,
                superSetBoundsList = superSetBounds,
                onExerciseClick = {
                    superSet.setCurrentExercise(it)
                    onExerciseClick(it)
                },
                isSwapTarget = viewModel.currentSwapTargetId.value == superSet.exercises.first().id,
                isChildBeingDragged = isChildBeingDragged
            )
        } else {
            // Render the superset header and list of exercises with connectors
            Column(modifier = Modifier.padding(10.dp)) {
                // Superset header with conditional text and animated indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (!isChildBeingDragged.value) isExpanded.value = !isExpanded.value
                        }
                        .animateContentSize(
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = FastOutSlowInEasing
                            )
                        )
                ) {
                    Text(
                        text = "Superset (${superSet.exercises.size} Exercises)${if (superSet.isDone) " (Done)" else ""}",
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 18.sp,
                        color = if (superSet.isDone) Color.LightGray else Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    // Animated arrow indicator
                    Icon(
                        imageVector = if (isExpanded.value) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded.value) "Collapse" else "Expand",
                        tint = Color(0xFFAAAAAA),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Animated visibility for expanding/collapsing exercises
                AnimatedVisibility(
                    visible = isExpanded.value,
                    enter = expandVertically(
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = FastOutSlowInEasing
                        )
                    ) + fadeIn(
                        initialAlpha = 0.3f,
                        animationSpec = tween(
                            durationMillis = 300,
                            delayMillis = 100
                        )
                    ),
                    exit = shrinkVertically(
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = FastOutSlowInEasing
                        )
                    ) + fadeOut(
                        animationSpec = tween(
                            durationMillis = 300
                        )
                    )
                ) {
                    Column {
                        superSet.exercises.forEachIndexed { index, exercise ->
                            ExerciseBox(
                                activeSuperset = superSet,
                                exercise = exercise,
                                viewModel = viewModel,
                                superSetBoundsList = superSetBounds,
                                onExerciseClick = {
                                    superSet.setCurrentExercise(it)
                                    onExerciseClick(it)
                                },
                                isSwapTarget = viewModel.currentSwapTargetId.value == exercise.id,
                                isChildBeingDragged = isChildBeingDragged
                            )

                            // Add a GradientConnector between exercises, except after the last one
                            if (index < superSet.exercises.size - 1) {
                                GradientConnector()
                            }
                        }
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

    // Set parent's flag
    isChildBeingDragged.value = isDragging

    // Determine zIndex based on dragging state
    val zInd = if (isDragging) 9999f else 0f

    // Define highlight and background colors
    val highlightColor = Color(0xFFFFA500) // Orange color for highlight
    val backgroundColor = when {
        isSwapTarget -> highlightColor
        isDragging && dragDirection == DragDirection.HORIZONTAL -> Color.Red
        else -> Color(0xFF1E1E1E)
    }

    Box(
        modifier = Modifier
            .padding(5.dp)
            .fillMaxWidth()
            // Apply zIndex to the outermost Box
            .zIndex(zInd)
            .onGloballyPositioned { coords ->
                exerciseBoxBounds = coords.boundsInWindow()
                viewModel.exerciseBounds[exercise.id] = exerciseBoxBounds!!
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        // no dragging if done
                        isDragging = !exercise.isDone
                        viewModel.startDragging(exercise, activeSuperset)
                    },
                    onDrag = { change, dragAmount ->
                        change.consumeAllChanges()
                        coroutineScope.launch {
                            // Determine drag direction if not set
                            if (dragDirection == null) {
                                dragDirection = when {
                                    abs(dragAmount.x) > abs(dragAmount.y) -> DragDirection.HORIZONTAL
                                    else -> DragDirection.VERTICAL
                                }
                            }

                            // Move only along the chosen axis
                            when (dragDirection) {
                                DragDirection.HORIZONTAL -> xOffsetAnim.snapTo(xOffsetAnim.value + dragAmount.x)
                                DragDirection.VERTICAL -> yOffsetAnim.snapTo(yOffsetAnim.value + dragAmount.y)
                                null -> {}
                            }

                            // If dragging horizontally, skip overlap detection
                            if (dragDirection == DragDirection.HORIZONTAL) return@launch

                            // Detect overlap for setting swap target
                            val currentBounds = Rect(
                                left = (exerciseBoxBounds?.left ?: 0f) + xOffsetAnim.value,
                                top = (exerciseBoxBounds?.top ?: 0f) + yOffsetAnim.value,
                                right = (exerciseBoxBounds?.right ?: 0f) + xOffsetAnim.value,
                                bottom = (exerciseBoxBounds?.bottom ?: 0f) + yOffsetAnim.value
                            )
                            var newTargetId: String? = null
                            for (superset in viewModel.supersets) {
                                for (otherExercise in superset.exercises) {
                                    if (otherExercise.id == exercise.id) continue
                                    val otherBounds = viewModel.exerciseBounds[otherExercise.id]
                                    if (
                                        otherBounds != null &&
                                        doRectanglesOverlap(currentBounds, otherBounds, margin = 20f)
                                    ) {
                                        newTargetId = otherExercise.id
                                        break
                                    }
                                }
                                if (newTargetId != null) break
                            }
                            viewModel.setCurrentSwapTargetId(newTargetId)
                        }
                    },
                    onDragEnd = {
                        coroutineScope.launch {
                            if (dragDirection == DragDirection.HORIZONTAL) {
                                // Remove if swiped far horizontally
                                if (xOffsetAnim.value > CENTER_RAD) {
                                    viewModel.removeExerciseFromSuperset(activeSuperset, exercise)
                                }
                                // Snap back
                                xOffsetAnim.animateTo(0f, animationSpec = spring())
                                yOffsetAnim.animateTo(0f, animationSpec = spring())
                                isDragging = false
                                dragDirection = null
                                return@launch
                            }

                            // Vertical drag ended
                            isDragging = false
                            dragDirection = null

                            val initialBounds = exerciseBoxBounds
                            if (initialBounds != null) {
                                val finalExerciseRect = Rect(
                                    left = initialBounds.left + xOffsetAnim.value,
                                    top = initialBounds.top + yOffsetAnim.value,
                                    right = initialBounds.right + xOffsetAnim.value,
                                    bottom = initialBounds.bottom + yOffsetAnim.value
                                )
                                var exerciseMoved = false

                                // 1. Check if dropped into another superset
                                for ((sid, supBounds) in superSetBoundsList) {
                                    val targetSuperset = viewModel.supersets.find { it.id == sid }
                                    if (targetSuperset == null || supBounds == null) continue

                                    if (
                                        targetSuperset.id != activeSuperset.id &&
                                        doRectanglesOverlap(supBounds, finalExerciseRect, margin = 20f)
                                    ) {
                                        viewModel.moveExercise(targetSuperset)
                                        exerciseMoved = true
                                        break
                                    }
                                }

                                // 2. Reorder if dropped within the same superset
                                if (!exerciseMoved) {
                                    val originalSupersetBounds = superSetBoundsList[activeSuperset.id]
                                    if (
                                        originalSupersetBounds != null &&
                                        doRectanglesOverlap(originalSupersetBounds, finalExerciseRect, margin = 20f)
                                    ) {
                                        val fromIndex = activeSuperset.exercises.indexOf(exercise)
                                        if (fromIndex != -1) {
                                            val finalCenterX = finalExerciseRect.left + (finalExerciseRect.width / 2f)
                                            val finalCenterY = finalExerciseRect.top + (finalExerciseRect.height / 2f)

                                            val closestIndex = activeSuperset.exercises.withIndex()
                                                .filter { it.value.id != exercise.id }
                                                .minByOrNull { indexed ->
                                                    val targetBounds = viewModel.exerciseBounds[indexed.value.id]
                                                    if (targetBounds == null) {
                                                        Float.MAX_VALUE
                                                    } else {
                                                        val centerX = (targetBounds.left + targetBounds.right) / 2f
                                                        val centerY = (targetBounds.top + targetBounds.bottom) / 2f
                                                        val dx = centerX - finalCenterX
                                                        val dy = centerY - finalCenterY
                                                        dx * dx + dy * dy // Distance squared
                                                    }
                                                }?.index

                                            if (
                                                closestIndex != null &&
                                                closestIndex != fromIndex &&
                                                closestIndex in activeSuperset.exercises.indices
                                            ) {
                                                viewModel.reorderExercise(
                                                    superSet = activeSuperset,
                                                    fromIndex = fromIndex,
                                                    toIndex = closestIndex
                                                )
                                                exerciseMoved = true
                                            }
                                        }
                                    }
                                }

                                // 3. If not moved or reordered, create a new superset
                                if (!exerciseMoved) {
                                    viewModel.createNewSuperset(exercise)
                                    viewModel.removeExerciseFromSuperset(activeSuperset, exercise)
                                    exerciseMoved = true
                                }

                                // Stop dragging in ViewModel and reset swap target
                                viewModel.stopDragging()
                                viewModel.setCurrentSwapTargetId(null)

                                // Animate back to original position
                                xOffsetAnim.animateTo(0f, animationSpec = spring())
                                yOffsetAnim.animateTo(0f, animationSpec = spring())
                            }
                        }
                    },
                    onDragCancel = {
                        coroutineScope.launch {
                            isDragging = false
                            dragDirection = null
                            viewModel.stopDragging()
                            viewModel.setCurrentSwapTargetId(null)
                            xOffsetAnim.animateTo(0f, animationSpec = spring())
                            yOffsetAnim.animateTo(0f, animationSpec = spring())
                        }
                    }
                )
            }
    ) {
        // Fixed Red Background Layer
        if (isDragging && dragDirection == DragDirection.HORIZONTAL) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Red)
            )
        }

        // Draggable Card Layer
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        xOffsetAnim.value.roundToInt(),
                        yOffsetAnim.value.roundToInt()
                    )
                }
                .background(
                    color = if (isSwapTarget) Color(0xFFFFA500) else backgroundColor,
                    shape = RoundedCornerShape(12.dp)
                )
                .clip(RoundedCornerShape(12.dp))
                .animateContentSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E1E1E)) // Inner background color
                    .clickable {
                        onExerciseClick(exercise)
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
                        color = if (exercise.isDone) Color.LightGray else Color.White
                    )

                    val measureStr = if (ExerciseMeasureType.useTime(exercise.exercise.measureType)) "time" else "reps"
                    var secondLinePart = "${(exercise.inset?.map { it.value })?.average() ?: "???"} average $measureStr"

                    if (exercise.inset != null && exercise.exercise.measureType == ExerciseMeasureType.DISTANCE_BASED) {
                        secondLinePart += " with ${exercise.inset!!.mapNotNull { it.distance }.average()} average distance"
                    }

                    Text(
                        text = "${exercise.inset!!.size} sets, $secondLinePart",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 14.sp,
                        color = if (exercise.isDone) Color.Gray else Color(0xFFAAAAAA),
                        textAlign = TextAlign.Start
                    )
                }
            }
        }
    }
}
