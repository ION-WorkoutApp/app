package com.ion606.workoutapp.screens.activeExercise

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.ion606.workoutapp.R
import com.ion606.workoutapp.dataObjects.ActiveExercise
import com.ion606.workoutapp.dataObjects.ExerciseSetDataObj
import com.ion606.workoutapp.elements.InputField
import com.ion606.workoutapp.elements.InputFieldCompact
import com.ion606.workoutapp.helpers.Alerts
import com.ion606.workoutapp.helpers.NotificationManager
import com.ion606.workoutapp.helpers.convertSecondsToTimeString
import com.ion606.workoutapp.logic.StartTimer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


private const val TAG = "DisplayActiveExercise"

fun stringToInt(s: String): Int {
    return try {
        s.trim().toInt()
    } catch (e: NumberFormatException) {
        0
    }
}

fun convertToSeconds(time: String): Int {
    val parts = time.split(":")
    if (parts.size != 2) {
        throw IllegalArgumentException("Invalid time format. Expected MM:SS.")
    }

    val minutes = parts[0].toIntOrNull() ?: throw IllegalArgumentException("Invalid minute value.")
    val seconds = parts[1].toIntOrNull() ?: throw IllegalArgumentException("Invalid second value.")
    if (seconds !in 0..59) {
        throw IllegalArgumentException("Seconds must be between 0 and 59.")
    }
    return minutes * 60 + seconds
}


class DisplayActiveExercise {
    companion object {
        @SuppressLint("MutableCollectionMutableState", "UnusedContentLambdaTargetStateParameter")
        @Composable
        fun DisplayActiveExerciseScreen(
            activeExercise: State<ActiveExercise?>,
            triggerExerciseSave: (ActiveExercise, SuperSet, Boolean) -> Unit,
            currentSuperset: State<SuperSet?>,
            context: Context,
            nhelper: NotificationManager,
            advanceToNextExercise: (ActiveExercise?, SuperSet) -> Unit
        ) {
            val exercise = activeExercise.value ?: return
            val superset = currentSuperset.value ?: return

            Log.d(TAG, "Displaying active exercise: $exercise")

            // State to control the visibility of exercise information
            val isInfoExpanded = remember { mutableStateOf(false) }
            val animatedHeight: Dp by animateDpAsState(
                targetValue = if (isInfoExpanded.value) 200.dp else 0.dp,
                label = "animatedHeight${exercise.exercise.exerciseId}"
            )
            val timerSetr = remember { mutableStateOf<ExerciseSetDataObj?>(null) }
            val showAlert = remember { mutableStateOf(false) }
            val restTimer = remember { mutableIntStateOf(0) }

            // List for items in the LazyColumn
            val ll = if (exercise.exercise.timeBased) exercise.times else exercise.reps
            val itemList = remember { mutableStateOf(ll?.toMutableList() ?: mutableListOf()) }

            val listState = rememberLazyListState()
            val coroutineScope = rememberCoroutineScope()
            val isTimerVisible = remember { mutableStateOf(false) }

            if (showAlert.value) {
                Alerts.ShowAlert(
                    onClick = { showAlert.value = false },
                    title = "Invalid Time",
                    text = "Please enter a valid time."
                )
            }

            // track the countdown in a state so Compose will recompose:
            val currentSetTime = remember { mutableStateOf(0) }

            LaunchedEffect(timerSetr.value) {
                if (timerSetr.value != null) {
                    currentSetTime.value = timerSetr.value!!.value
                    // run a loop that decrements currentSetTime every second
                    while (currentSetTime.value > 0) {
                        delay(1000)
                        currentSetTime.value -= 1
                    }
                    // done
                    isTimerVisible.value = false
                    timerSetr.value = null
                }
            }

            if (timerSetr.value != null) {
                Log.d(TAG, "Starting timer for set: ${timerSetr.value}")

                isTimerVisible.value = true
                StartTimer(
                    onFinishCB = {
                        isTimerVisible.value = it
                        if (it) {
                            nhelper.sendNotificationIfUnfocused(
                                title = "Set Timer",
                                message = "Timer completed for ${exercise.exercise.title}",
                                intents = listOf(
                                    "action" to "com.ion606.workoutapp.action.OPEN_ACTIVE_EXERCISE",
                                    "exerciseId" to exercise.exercise.exerciseId
                                )
                            )
                            timerSetr.value!!.isDone = true
                            timerSetr.value = null
                        }
                    }, remainingTime = timerSetr.value!!.value
                )
            } else if (restTimer.intValue > 0) {
                StartTimer(
                    onFinishCB = {
                        if (it) {
                            restTimer.intValue = 0
                            Log.d(TAG, "Rest timer completed")

                            nhelper.sendNotificationIfUnfocused(
                                title = "Rest Timer",
                                message = "Rest timer completed for ${exercise.exercise.title}",
                                intents = listOf(
                                    "action" to "com.ion606.workoutapp.action.OPEN_ACTIVE_EXERCISE",
                                    "exerciseId" to exercise.exercise.exerciseId
                                )
                            )
                        }
                    }, restTimer.intValue, "Rest Timer"
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF121212)), // Dark background color
                contentAlignment = Alignment.BottomCenter
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                Column(
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(20.dp))

                    // "+" Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Start)
                    ) {
                        // go back?
                        IconButton(
                            onClick = {
                                triggerExerciseSave(exercise, superset, true)
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBackIosNew,
                                contentDescription = "Go Back",
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = {
                                // Add a new set
                                itemList.value = itemList.value.toMutableList().apply {
                                    add(ExerciseSetDataObj(0)) // default
                                }

                                exercise.let {
                                    it.reps?.add(ExerciseSetDataObj(0))
                                }
                                exercise.let {
                                    it.times?.add(ExerciseSetDataObj(0))
                                }
                                exercise.let {
                                    it.weight?.add(ExerciseSetDataObj(0))
                                }

                                superset.updateExercise(exercise)

                                coroutineScope.launch {
                                    val targetIndex = itemList.value.size - 1
                                    val currentIndex = listState.firstVisibleItemIndex + 2

                                    // Scroll incrementally to the target index
                                    if (currentIndex < targetIndex) {
                                        for (index in currentIndex..targetIndex) {
                                            listState.animateScrollToItem(index)
                                        }
                                    } else if (currentIndex > targetIndex) {
                                        for (index in currentIndex downTo targetIndex) {
                                            listState.animateScrollToItem(index)
                                        }
                                    }
                                    listState.animateScrollToItem(index = itemList.value.size - 1)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBB86FC))
                        ) {
                            Text(text = "+", color = Color.White, fontSize = 18.sp)
                        }
                    }

                    Text(
                        text = exercise.exercise.title, style = TextStyle(
                            color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold
                        )
                    )

                    Button(
                        onClick = { isInfoExpanded.value = !isInfoExpanded.value },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBB86FC))
                    ) {
                        Text(
                            text = if (isInfoExpanded.value) "Hide Exercise Info" else "Show Exercise Info",
                            color = Color.White
                        )
                    }

                    // Animated exercise information
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(animatedHeight)
                            .background(Color(0xFF1E1E1E))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = exercise.exercise.description, style = TextStyle(
                                color = Color.Gray, fontSize = 16.sp
                            )
                        )
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f) // Let the list fill available vertical space
                    ) {
                        itemsIndexed(items = itemList.value,
                            key = { _, item -> item.id } // Use a stable key for each item
                        ) { i, setItem ->
                            var isVisible by remember { mutableStateOf(true) }
                            var editingTimer by remember { mutableStateOf(false) }

                            // permamnt string to not tirgger the timer
                            var restTimeStr by remember { mutableIntStateOf(setItem.restTime) }

                            AnimatedVisibility(
                                visible = isVisible,
                                enter = slideInHorizontally(animationSpec = tween(300)),
                                exit = slideOutHorizontally(animationSpec = tween(300))
                            ) {
                                // no editing while timer is live
                                if (!editingTimer || restTimer.intValue > 0) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 0.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Timer,
                                            contentDescription = "Timer Icon",
                                            tint = Color.White,
                                            modifier = Modifier
                                                .size(35.dp)
                                                .padding(start = 10.dp, bottom = 0.dp)
                                                .align(Alignment.CenterVertically)
                                        )
                                        Text(text = "${convertSecondsToTimeString(restTimeStr)} rest",
                                            color = Color.White,
                                            modifier = Modifier
                                                .padding(
                                                    start = 10.dp, bottom = 0.dp
                                                )
                                                .height(35.dp)
                                                .align(Alignment.CenterVertically)
                                                .clickable {
                                                    if (restTimer.intValue <= 0) editingTimer = true
                                                })
                                    }
                                } else {
                                    Alerts.CreateAlertDialog(
                                        title = "Edit Rest Time",
                                        context,
                                        true,
                                    ) {
                                        val reststr = it!!.toIntOrNull();
                                        if (reststr == null || reststr <= 0) return@CreateAlertDialog

                                        Log.d(TAG, "Rest time changed to $reststr")

                                        restTimeStr = reststr
                                        itemList.value = itemList.value.toMutableList().apply {
                                            this[i] = setItem.copy(restTime = reststr)
                                        }

                                        if (exercise.exercise.timeBased) exercise.times =
                                            itemList.value
                                        else exercise.reps = itemList.value
                                        exercise.restTime = reststr

                                        triggerExerciseSave(exercise, superset, false)
                                        editingTimer = false
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Transparent)
                                    .clip(RoundedCornerShape(12.dp))
                                    .padding(0.dp)
                            ) {
                                // Mutable state for horizontal swipe offset
                                var xOffset by remember { mutableStateOf(0f) }
                                val animatedXOffset by animateFloatAsState(targetValue = xOffset,
                                    animationSpec = tween(300),
                                    finishedListener = {
                                        if (xOffset >= CENTER_RAD) {
                                            isVisible = false
                                        }
                                    })

                                if (!isVisible) {
                                    // Delay actual removal
                                    LaunchedEffect(Unit) {
                                        delay(300) // Wait for animation to finish
                                        itemList.value = itemList.value.toMutableList().apply {
                                            removeAt(i)
                                        }

                                        if (exercise.exercise.timeBased) exercise.times = itemList.value
                                        else exercise.reps = itemList.value

                                        Log.d(TAG, "removed set $setItem from $exercise")
                                        triggerExerciseSave(exercise, superset, false)
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(95.dp)
                                        .background(Color.Red)
                                        .clip(RoundedCornerShape(20.dp))
                                        .align(Alignment.CenterStart)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_launcher_background),
                                        contentDescription = "Trash Icon",
                                        modifier = Modifier
                                            .size(70.dp)
                                            .padding(16.dp)
                                            .align(Alignment.CenterStart)
                                    )
                                }

                                Card(modifier = Modifier
                                    .zIndex(1f) // Card remains on top
                                    .offset { IntOffset(animatedXOffset.roundToInt(), 0) }
                                    .fillMaxWidth()
                                    .pointerInput(Unit) {
                                        if (!setItem.isDone) { // Disable drag gestures if item is disabled
                                            detectHorizontalDragGestures(onHorizontalDrag = { change, dragAmount ->
                                                change.consume()
                                                xOffset += dragAmount
                                            }, onDragEnd = {
                                                xOffset = if (xOffset > CENTER_RAD) {
                                                    10000f // Move fully out
                                                } else {
                                                    0f // Snap back
                                                }
                                            })
                                        }
                                    }, colors = CardDefaults.cardColors(
                                    containerColor = if (setItem.isDone) Color.DarkGray else Color(
                                        0xFF1E1E1E
                                    ) // Dark gray for disabled
                                )
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .height(75.dp)
                                            .fillMaxWidth()
                                    ) {
                                        val t = convertSecondsToTimeString(setItem.value).split(":")
                                        // Time or Reps on the left
                                        if (exercise.exercise.timeBased) {
                                            InputFieldCompact(value = t[0], // Minutes
                                                label = "MM",
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight(),
                                                enabled = !setItem.isDone,
                                                onChange = {
                                                    if (it.trim()
                                                            .isEmpty()
                                                    ) return@InputFieldCompact
                                                    val newTime =
                                                        (stringToInt(it) * 60) + stringToInt(t[1])
                                                    itemList.value =
                                                        itemList.value.toMutableList().apply {
                                                            this[i] = setItem.copy(value = newTime)
                                                        }
                                                    exercise.times = itemList.value
                                                    triggerExerciseSave(exercise, superset, false)
                                                })

                                            Text(
                                                text = ":", modifier = Modifier.padding(
                                                    top = 16.dp, start = 8.dp, end = 8.dp
                                                ), style = TextStyle(
                                                    color = if (setItem.isDone) Color.Gray else Color.White, // Gray for disabled
                                                    fontSize = 20.sp
                                                )
                                            )

                                            InputFieldCompact(value = t[1], // Seconds
                                                label = "SS",
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight(),
                                                enabled = !setItem.isDone,
                                                onChange = {
                                                    if (it.trim()
                                                            .isEmpty()
                                                    ) return@InputFieldCompact
                                                    val newTime =
                                                        stringToInt(it) + (stringToInt(t[0]) * 60)
                                                    itemList.value =
                                                        itemList.value.toMutableList().apply {
                                                            this[i] = setItem.copy(value = newTime)
                                                        }
                                                    exercise.times = itemList.value
                                                    triggerExerciseSave(exercise, superset, false)
                                                })
                                        } else {
                                            InputField(value = setItem.value.toString(),
                                                label = "Reps",
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .align(Alignment.CenterVertically),
                                                enabled = !setItem.isDone,
                                                onChange = {
                                                    if (it.trim().isEmpty()) return@InputField
                                                    val updatedReps = stringToInt(it)
                                                    itemList.value =
                                                        itemList.value.toMutableList().apply {
                                                            this[i] =
                                                                setItem.copy(value = updatedReps)
                                                        }
                                                    exercise.reps = itemList.value
                                                    triggerExerciseSave(exercise, superset, false)
                                                })
                                        }

                                        Spacer(modifier = Modifier.width(16.dp))

                                        InputField(value = exercise.weight?.getOrNull(
                                            itemList.value.indexOf(
                                                setItem
                                            )
                                        )?.value?.toString() ?: "",
                                            label = "Weight (lbs)",
                                            modifier = Modifier.weight(1f),
                                            enabled = !setItem.isDone,
                                            onChange = {
                                                if (it.trim().isEmpty()) return@InputField
                                                val updatedWeight = stringToInt(it)
                                                exercise.weight =
                                                    exercise.weight?.toMutableList()?.apply {
                                                        this[itemList.value.indexOf(setItem)] =
                                                            ExerciseSetDataObj(updatedWeight)
                                                    }
                                                triggerExerciseSave(exercise, superset, false)
                                            })

                                    }
                                }
                            }
                        }
                    }
                }

                // Start/Complete button
                if (restTimer.intValue <= 0 && itemList.value.find { !it.isDone } != null) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        if (exercise.exercise.timeBased) {
                            Button(onClick = {
                                val tstr = itemList.value.find { !it.isDone }
                                if (tstr!!.value <= 0) {
                                    showAlert.value = true
                                    return@Button
                                }

                                timerSetr.value = tstr
                            }) {
                                Image(
                                    Icons.Default.Timer, contentDescription = "Start Timer"
                                )
                            }
                        }

                        Button(
                            onClick = {
                                goToNextExercise(
                                    superset,
                                    restTimer,
                                    itemList,
                                    activeExercise,
                                    exercise,
                                    { it, superset -> triggerExerciseSave(it, superset, false) },
                                    advanceToNextExercise
                                )
                            }, modifier = Modifier.align(Alignment.Bottom)
                        ) {
                            Text(
                                text = "Log Set", color = Color.White
                            )
                        }
                    }
                } else {
                    Button(onClick = {
                        exercise.isDone = true
                        triggerExerciseSave(exercise, superset, true)
                    }) {
                        Text(
                            text = "Complete Exercise", color = Color.White
                        )
                    }
                }
            }
        }

        private fun goToNextExercise(
            superset: SuperSet,
            restTimer: MutableIntState,
            itemList: MutableState<MutableList<ExerciseSetDataObj>>,
            activeExercise: State<ActiveExercise?>,
            exercise: ActiveExercise,
            triggerExerciseSave: (ActiveExercise, SuperSet) -> Unit,
            advanceToNextExercise: (ActiveExercise?, SuperSet) -> Unit
        ) {
            // Mark the current set as done
            val i = if (exercise.exercise.timeBased) {
                exercise.times!!.indexOfFirst { !it.isDone }
            } else {
                exercise.reps!!.indexOfFirst { !it.isDone }
            }

            if (exercise.exercise.timeBased && i != -1) {
                exercise.times!![i] = exercise.times?.get(i)?.apply {
                    restTime = restTimer.intValue
                    isDone = true
                    Log.d(TAG, "Marking time set as done: $this")
                }!!
            } else if (!exercise.exercise.timeBased && i != -1) {
                exercise.reps!![i] = exercise.reps?.get(i)?.apply {
                    restTime = restTimer.intValue
                    isDone = true
                    Log.d(TAG, "Marking rep set as done: $this")
                }!!
            }

            Log.d(
                TAG,
                "Going to next exercise --> ${superset.isOnLastExercise()} | ${superset.currentExerciseIndex} | ${superset.exercises.size}"
            )

            triggerExerciseSave(exercise, superset)

            if (superset.isOnLastExercise()) {
                restTimer.intValue = superset.getCurrentExercise()?.restTime ?: 0
            }

            val nextExercise = superset.goToNextExercise()
            Log.d(TAG, "Switching to next exercise: $nextExercise")
            advanceToNextExercise(nextExercise, superset)
        }
    }
}

