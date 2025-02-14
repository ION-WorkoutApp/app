package com.ion606.workoutapp.screens.activeExercise

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.ion606.workoutapp.MainActivity
import com.ion606.workoutapp.R
import com.ion606.workoutapp.dataObjects.ActiveExercise
import com.ion606.workoutapp.dataObjects.ExerciseMeasureType
import com.ion606.workoutapp.dataObjects.ExerciseSetDataObj
import com.ion606.workoutapp.dataObjects.SuperSet
import com.ion606.workoutapp.dataObjects.SuperSetDao
import com.ion606.workoutapp.elements.InputField
import com.ion606.workoutapp.elements.InputFieldCompact
import com.ion606.workoutapp.elements.Tooltip
import com.ion606.workoutapp.helpers.Alerts
import com.ion606.workoutapp.helpers.NotificationManager
import com.ion606.workoutapp.helpers.convertSecondsToTimeString
import com.ion606.workoutapp.logic.StartTimer
import com.ion606.workoutapp.managers.UserManager
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


class DisplayActiveExercise {
    companion object {
        private fun getStoredTime(context: Context, key: String = "restTime"): Int {
            val prefs: SharedPreferences = context.getSharedPreferences(key, MODE_PRIVATE)
            return prefs.getInt("seconds", 0)
        }

        private fun setStoredTime(context: Context, rt: Int, key: String = "restTime") {
            val prefs: SharedPreferences = context.getSharedPreferences(key, MODE_PRIVATE)
            val editor = prefs.edit()
            editor.putInt("seconds", rt)
            editor.apply()
        }


        @Composable
        private fun TimeHelper(
            nhelper: NotificationManager,
            exercise: ActiveExercise,
            timerSetr: MutableState<ExerciseSetDataObj?>,
            currentTimerSet: Int,
            cb: (Boolean) -> Unit
        ) {
            val timerTitle = if (exercise.exercise.perSide) {
                if (currentTimerSet == 1) "Left Side Timer"
                else "Right Side Timer"
            } else {
                "Set Timer"
            }

            StartTimer(headerText = timerTitle,
                remainingTime = timerSetr.value!!.value,
                onFinishCB = { didFinish ->
                    if (didFinish) {
                        nhelper.sendNotificationIfUnfocused(
                            title = "Set Timer",
                            message = "Timer completed for ${exercise.exercise.title}",
                            intents = listOf(
                                "action" to "com.ion606.workoutapp.action.OPEN_ACTIVE_EXERCISE",
                                "exerciseId" to exercise.exercise.exerciseId
                            )
                        )
                    }
                    cb(didFinish)
                })
        }


        @SuppressLint("MutableCollectionMutableState", "UnusedContentLambdaTargetStateParameter")
        @Composable
        fun DisplayActiveExerciseScreen(
            activeExercise: State<ActiveExercise?>,
            triggerExerciseSave: (ActiveExercise, SuperSet, Boolean) -> Unit,
            currentSuperset: State<SuperSet?>,
            context: Context,
            nhelper: NotificationManager,
            userManager: UserManager,
            advanceToNextExercise: (ActiveExercise?, SuperSet) -> Unit,
            dao: SuperSetDao
        ) {
            val exercise = activeExercise.value ?: return
            val superset = currentSuperset.value ?: return

            Log.d(TAG, "Displaying active exercise: $exercise")

            // show/hide expanded info
            val isInfoExpanded = remember { mutableStateOf(false) }
            val focusManager = LocalFocusManager.current;

            // timers & rest
            val timerSetr = remember { mutableStateOf<ExerciseSetDataObj?>(null) }
            val secondaryTimerSetr = remember { mutableStateOf<ExerciseSetDataObj?>(null) }
            val showAlert = remember { mutableStateOf(false) }
            val restTimer = remember { mutableIntStateOf(this.getStoredTime(context)) }
            val isTimerVisible = remember { mutableStateOf(false) }
            val currentSetTime = remember { mutableIntStateOf(0) }

            // units
            val userWeightUnit = userManager.getUserData()?.weightUnit ?: "lbs"
            val userDistanceUnit = userManager.getUserData()?.distanceUnit ?: "km"

            // items in the LazyColumn
            val itemList = remember { mutableStateListOf<ExerciseSetDataObj>() }
            val listState = rememberLazyListState()
            val coroutineScope = rememberCoroutineScope()
            var shouldLogSet by remember { mutableStateOf(false) }

            // update itemList whenever our superset or exercise changes
            LaunchedEffect(currentSuperset.value, exercise) {
                val updatedSuperSet = currentSuperset.value
                val newExercise = updatedSuperSet?.exercises?.find { it.id == exercise.id }
                if (newExercise?.inset != null) {
                    itemList.clear()
                    itemList.addAll(newExercise.inset!!)
                }
            }

            if (shouldLogSet) {
                val tempTime = superset.getCurrentExercise()?.restTime ?: 0

                if (!superset.isOnLastExercise() || tempTime <= 0) {
                    LaunchedEffect(Unit) {
                        coroutineScope.launch {
                            logSet(
                                superset,
                                restTimer,
                                exercise,
                                userManager,
                                dao,
                                triggerExerciseSave,
                                advanceToNextExercise
                            )
                            shouldLogSet = false
                        }
                    }
                } else {
                    restTimer.intValue = tempTime
                    shouldLogSet = false
                }
            }

            fun saveChanges() {
                exercise.inset = itemList.toMutableList()
                superset.updateExercise(exercise)
                triggerExerciseSave(exercise, superset, false)
            }

            if (showAlert.value) {
                Alerts.ShowAlert(
                    onClick = { showAlert.value = false },
                    title = "Invalid Time",
                    text = "Please enter a valid time."
                )
            }

            // region: timers
            // primary timer effect
            LaunchedEffect(timerSetr.value) {
                timerSetr.value?.let { setItem ->
                    currentSetTime.intValue = setItem.value
                    while (currentSetTime.intValue > 0) {
                        delay(1000)
                        setStoredTime(context, currentSetTime.intValue, "repTimer")
                        currentSetTime.intValue -= 1
                    }
                    isTimerVisible.value = false
                    if (exercise.exercise.perSide) {
                        // start the secondary timer
                        secondaryTimerSetr.value = setItem
                    } else {
                        // done with the timer
                        timerSetr.value = null
                        setStoredTime(context, 0, "repTimer")
                    }
                }
            }

            // secondary timer effect
            LaunchedEffect(secondaryTimerSetr.value) {
                secondaryTimerSetr.value?.let { setItem ->
                    currentSetTime.intValue = setItem.value
                    while (currentSetTime.intValue > 0) {
                        delay(1000)
                        setStoredTime(context, currentSetTime.intValue, "repTimer")
                        currentSetTime.intValue -= 1
                    }
                    setStoredTime(context, 0, "repTimer")
                    isTimerVisible.value = false
                    secondaryTimerSetr.value = null
                }
            }

            // show timers if primary/secondary is in progress
            if (timerSetr.value != null || secondaryTimerSetr.value != null) {
                isTimerVisible.value = true

                // primary
                timerSetr.value?.let { primarySet ->
                    TimeHelper(nhelper, exercise, timerSetr, 0) { finished ->
                        if (finished && exercise.exercise.perSide) {
                            secondaryTimerSetr.value = primarySet
                        }
                        if (!exercise.exercise.perSide) {
                            timerSetr.value = null
                            secondaryTimerSetr.value = null
                            isTimerVisible.value = false
                            shouldLogSet = true
                        }
                    }
                }

                // secondary
                secondaryTimerSetr.value?.let { secondSet ->
                    TimeHelper(nhelper, exercise, secondaryTimerSetr, 1) { finished ->
                        if (finished) {
                            secondaryTimerSetr.value = null
                            timerSetr.value = null
                            isTimerVisible.value = false
                            shouldLogSet = true
                        }
                    }
                }

            } else if (restTimer.intValue > 0) {
                Log.d(TAG, "Rest timer is active with time ${restTimer.intValue}")

                // show notif now because it's a progressive
                // not ifunfocused because the user might click away during rest and still want the progress bar
                val pManager = nhelper.sendNotification(
                    title = "Rest Timer",
                    message = "Starting rest timer for ${exercise.exercise.title}",
                    intents = listOf(
                        "action" to "com.ion606.workoutapp.action.OPEN_ACTIVE_EXERCISE",
                        "exerciseId" to exercise.exercise.exerciseId
                    ),
                    isProgress = true
                )

                if (pManager == null) Log.d(
                    TAG, "PManager is null, timer bar will not be displayed"
                );

                // show rest timer if needed
                StartTimer(headerText = "Rest Timer",
                    remainingTime = restTimer.intValue,
                    onTickCB = { remainingTime ->
                        setStoredTime(context, remainingTime)
                        if (pManager == null) return@StartTimer

                        pManager.setProgress(
                            restTimer.intValue, restTimer.intValue - remainingTime, false
                        )
                        nhelper.updateNotification(pManager);
                    },
                    onFinishCB = { _ ->
                        // doesn't matter if it was cancelled or not bc the time needs to be logged either way
                        setStoredTime(context, 0)

                        // remove progress bar
                        pManager?.setProgress(0, 0, false)
                        restTimer.intValue = 0

                        Log.d(TAG, "Rest timer completed");

                        // cancel progress notification
                        nhelper.cancelNotification()

                        nhelper.sendNotificationIfUnfocused(
                            title = "Rest Timer",
                            message = "Rest timer completed for ${exercise.exercise.title}",
                            intents = listOf(
                                "action" to "com.ion606.workoutapp.action.OPEN_ACTIVE_EXERCISE",
                                "exerciseId" to exercise.exercise.exerciseId
                            )
                        )

                        coroutineScope.launch {
                            logSet(
                                superset,
                                restTimer,
                                exercise,
                                userManager,
                                dao,
                                triggerExerciseSave,
                                advanceToNextExercise
                            )
                            shouldLogSet = false
                        }
                    })
            }

            // endregion

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF121212))
                    .clickable(indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { focusManager.clearFocus(); }),
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

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Start)
                    ) {
                        IconButton(onClick = {
                            triggerExerciseSave(exercise, superset, true)
                        }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBackIosNew,
                                contentDescription = "Go Back",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))

                        // add set
                        Button(
                            onClick = {
                                exercise.isDone = false
                                itemList.add(ExerciseSetDataObj(value = 0))
                                saveChanges()

                                // also create new weight entry
                                exercise.weight?.add(ExerciseSetDataObj(value = 0))
                                superset.updateExercise(exercise)

                                coroutineScope.launch {
                                    val targetIndex = itemList.size - 1
                                    val currentIndex = listState.firstVisibleItemIndex + 2

                                    if (currentIndex < targetIndex) {
                                        for (idx in currentIndex..targetIndex) {
                                            listState.animateScrollToItem(idx)
                                        }
                                    } else {
                                        for (idx in currentIndex downTo targetIndex) {
                                            listState.animateScrollToItem(idx)
                                        }
                                    }
                                    listState.animateScrollToItem(targetIndex)
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

                    // expanded info
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                            .background(Color(0xFF1E1E1E))
                    ) {
                        if (isInfoExpanded.value) {
                            Column(
                                modifier = Modifier
                                    .padding(10.dp)
                                    .background(Color.Transparent)
                            ) {
                                Text(
                                    modifier = Modifier.background(Color.Transparent),
                                    text = exercise.exercise.description,
                                    style = TextStyle(
                                        color = Color.LightGray,
                                        fontSize = 16.sp,
                                        lineHeight = 25.sp
                                    )
                                )
                            }
                        }
                    }

                    // region: sets list
                    CreateSetsList(
                        listState,
                        itemList,
                        restTimer,
                        context,
                        exercise,
                        triggerExerciseSave,
                        superset,
                        userDistanceUnit,
                        userWeightUnit
                    ) { saveChanges() }

                    // endregion: sets list
                }

                // region: bottom row (start timer / log set)
                if (restTimer.intValue <= 0) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.background(Color.Transparent)
                    ) {
                        // time-based start
                        if (ExerciseMeasureType.useTime(exercise.exercise.measureType)) {
                            Button(onClick = {
                                val nextUndoneSet = itemList.find { !it.isDone }
                                if (nextUndoneSet == null || nextUndoneSet.value <= 0) {
                                    showAlert.value = true
                                    return@Button
                                }
                                timerSetr.value = nextUndoneSet
                            }) {
                                Image(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = "Start Timer"
                                )
                            }
                        }

                        // "Log Set" / proceed to next
                        if (!exercise.isDone) {
                            Button(
                                onClick = { shouldLogSet = true },
                                modifier = Modifier
                                    .align(Alignment.Bottom)
                                    .padding(start = 10.dp)
                            ) {
                                Text(text = "Log Set", color = Color.White)
                            }
                        } else {
                            Button(
                                onClick = {
                                    advanceToNextExercise(null, superset)
                                },
                                modifier = Modifier
                                    .align(Alignment.Bottom)
                                    .padding(start = 10.dp)
                            ) {
                                Text(text = "Next Exercise", color = Color.White)
                            }
                        }
                    }
                } else {
                    Log.d(
                        TAG,
                        "Timer is still running or rest timer is active with time ${restTimer.intValue}"
                    )
                }
                // endregion
            }
        }


        @Composable
        private fun ColumnScope.CreateSetsList(
            listState: LazyListState,
            itemList: SnapshotStateList<ExerciseSetDataObj>,
            restTimer: MutableIntState,
            context: Context,
            exercise: ActiveExercise,
            triggerExerciseSave: (ActiveExercise, SuperSet, Boolean) -> Unit,
            superset: SuperSet,
            userDistanceUnit: String,
            userWeightUnit: String,
            saveChanges: () -> Unit
        ) {
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // to update the list every time anything changes, make the key EVERYTHING
                itemsIndexed(items = itemList, key = { _, item ->
                    "${item.id}-${item.value}-${item.distance}-${item.restTime}"
                }) { i, setItem ->
                    var isVisible by remember { mutableStateOf(true) }
                    var editingTimer by remember { mutableStateOf(false) }
                    var restTimeStr by remember { mutableIntStateOf(setItem.restTime) }
                    var showToolTip by remember { mutableStateOf(false) }

                    if (showToolTip) {
                        Tooltip(text = "Cannot edit rest time intermediate exercises!",
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            onDismiss = { showToolTip = false })
                    }

                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInHorizontally(animationSpec = tween(300)),
                        exit = slideOutHorizontally(animationSpec = tween(300))
                    ) {
                        // region

                        if (!editingTimer) {
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
                                        .padding(start = 10.dp, bottom = 13.dp)
                                        .align(Alignment.CenterVertically)
                                )
                                Text(text = "${convertSecondsToTimeString(restTimeStr)} rest",
                                    color = Color.White,
                                    modifier = Modifier
                                        .padding(start = 5.dp, bottom = 0.dp)
                                        .height(35.dp)
                                        .align(Alignment.CenterVertically)
                                        .clickable(enabled = (itemList.last() != setItem && !setItem.isDone)) {
                                            if (!superset.isOnLastExercise()) showToolTip = true
                                            else if (restTimer.intValue <= 0) editingTimer = true
                                            else Log.d(
                                                TAG,
                                                "Rest timer is active, cannot edit rest time....How did you even do this?"
                                            )
                                        })
                            }
                        } else {
                            // user wants to edit rest time
                            Alerts.CreateAlertDialog(
                                title = "Edit Rest Time",
                                context = context,
                                isTimeInput = true,
                                currentValue = itemList[i].restTime
                            ) { input ->
                                val originalVal = itemList[i].restTime
                                val restVal = input?.toIntOrNull()
                                if (restVal == null || restVal <= 0) {
                                    editingTimer = false;
                                    return@CreateAlertDialog
                                }
                                Log.d(TAG, "Rest time changed to $restVal")

                                restTimeStr = restVal

                                // loop through and update all exercises with the same rest timer
                                for (idx in i until itemList.size) {
                                    if (itemList[idx].restTime == originalVal) {
                                        itemList[idx] = itemList[idx].copy(restTime = restVal)
                                    }
                                }

                                exercise.inset = itemList
                                exercise.restTime = restVal
                                triggerExerciseSave(exercise, superset, false)
                                editingTimer = false
                            }
                        }
                        // endregion

                        // region: the card that can be swiped left to delete
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Transparent)
                                .clip(RoundedCornerShape(12.dp))
                                .padding(0.dp, top = 30.dp)
                        ) {
                            var xOffset by remember { mutableStateOf(0f) }
                            val animatedXOffset by animateFloatAsState(targetValue = xOffset,
                                animationSpec = tween(300),
                                finishedListener = {
                                    if (xOffset >= CENTER_RAD) {
                                        isVisible = false
                                    }
                                })

                            if (!isVisible) {
                                // remove item after anim
                                LaunchedEffect(Unit) {
                                    delay(300)

                                    val ind = itemList.indexOf(setItem)
                                    if (ind < 0) {
                                        Log.d(TAG, "Item not found in list")
                                        return@LaunchedEffect
                                    }
                                    itemList.remove(setItem)
                                    exercise.weight?.removeAt(ind)

                                    saveChanges()

                                    exercise.inset = itemList
                                    Log.d(TAG, "removed set $setItem from $exercise")
                                    triggerExerciseSave(exercise, superset, false)
                                }
                            }

                            // background "delete" color
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(95.dp)
                                    .background(if (xOffset > 0) Color.Red else Color.Transparent)
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

                            // the foreground card
                            Card(modifier = Modifier
                                .zIndex(1f)
                                .offset { IntOffset(animatedXOffset.roundToInt(), 0) }
                                .fillMaxWidth()
                                .pointerInput(Unit) {
                                    if (!setItem.isDone) {
                                        detectHorizontalDragGestures(onHorizontalDrag = { change, dragAmount ->
                                            change.consume()
                                            xOffset += dragAmount
                                        }, onDragEnd = {
                                            xOffset = if (xOffset > CENTER_RAD) {
                                                10000f
                                            } else {
                                                0f
                                            }
                                        })
                                    }
                                }, colors = CardDefaults.cardColors(
                                containerColor = if (setItem.isDone) Color.DarkGray
                                else Color(0xFF1E1E1E)
                            )
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .height(100.dp)
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                ) {
                                    // region: left input
                                    // we can break out 3 categories:
                                    // 1) distance-based
                                    // 2) time-based
                                    // 3) rep-based
                                    when (exercise.exercise.measureType) {
                                        ExerciseMeasureType.DISTANCE_BASED -> {/*
                                                              if distance is stored in 'setItem.distance':
                                                              show an InputField for distance,
                                                              ignoring 'value' or letting it remain 0
                                                            */
                                            InputField(value = setItem.distance?.toString() ?: "",
                                                label = "Distance ($userDistanceUnit)",
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .align(Alignment.CenterVertically),
                                                enabled = !setItem.isDone,
                                                onChange = { newVal ->
                                                    val newDist = stringToInt(newVal)

                                                    // change later sets if they have the same distance
                                                    for (idx in i until itemList.size) {
                                                        if (itemList[idx].distance == setItem.distance) {
                                                            itemList[idx] =
                                                                itemList[idx].copy(distance = newDist)
                                                        }
                                                    }

                                                    exercise.inset = itemList
                                                    triggerExerciseSave(
                                                        exercise, superset, false
                                                    )
                                                })
                                        }

                                        // time-based or rep-based is decided by the helper function
                                        else -> {
                                            if (ExerciseMeasureType.useTime(exercise.exercise.measureType)) {
                                                // show time input
                                                val t =
                                                    convertSecondsToTimeString(setItem.value).split(
                                                        ":"
                                                    )
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 8.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(80.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.Center
                                                    ) {
                                                        InputFieldCompact(value = t[0],
                                                            label = "MM",
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .fillMaxHeight(),
                                                            enabled = !setItem.isDone,
                                                            onChange = { mmStr ->
                                                                if (mmStr.trim()
                                                                        .isEmpty()
                                                                ) return@InputFieldCompact
                                                                val newTime =
                                                                    (stringToInt(mmStr) * 60) + stringToInt(
                                                                        t[1]
                                                                    )

                                                                // change later sets if they have the same time
                                                                for (idx in i until itemList.size) {
                                                                    if (itemList[idx].value == setItem.value) {
                                                                        itemList[idx] =
                                                                            itemList[idx].copy(value = newTime)
                                                                    }
                                                                }

                                                                exercise.inset = itemList
                                                                triggerExerciseSave(
                                                                    exercise, superset, false
                                                                )
                                                            })
                                                        if (exercise.exercise.perSide) {
                                                            Column(
                                                                horizontalAlignment = Alignment.CenterHorizontally
                                                            ) {
                                                                Text(
                                                                    text = "(Per Side)",
                                                                    style = TextStyle(
                                                                        color = Color.Gray,
                                                                        fontSize = 14.sp,
                                                                        fontStyle = FontStyle.Italic
                                                                    ),
                                                                    modifier = Modifier.padding(
                                                                        top = 0.dp, bottom = 0.dp
                                                                    )
                                                                )
                                                                Spacer(
                                                                    modifier = Modifier.height(
                                                                        22.dp
                                                                    )
                                                                )
                                                                Text(
                                                                    text = ":",
                                                                    modifier = Modifier.padding(
                                                                        horizontal = 8.dp
                                                                    ),
                                                                    style = TextStyle(
                                                                        color = if (setItem.isDone) Color.Gray
                                                                        else Color.White,
                                                                        fontSize = 20.sp
                                                                    )
                                                                )
                                                            }
                                                        } else {
                                                            Text(
                                                                text = ":",
                                                                modifier = Modifier.padding(
                                                                    horizontal = 8.dp
                                                                ),
                                                                style = TextStyle(
                                                                    color = if (setItem.isDone) Color.Gray
                                                                    else Color.White,
                                                                    fontSize = 20.sp
                                                                )
                                                            )
                                                        }
                                                        InputFieldCompact(value = t[1],
                                                            label = "SS",
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .fillMaxHeight(),
                                                            enabled = !setItem.isDone,
                                                            onChange = { ssStr ->
                                                                if (ssStr.trim()
                                                                        .isEmpty()
                                                                ) return@InputFieldCompact
                                                                val newTime =
                                                                    stringToInt(ssStr) + (stringToInt(
                                                                        t[0]
                                                                    ) * 60)

                                                                // change later sets if they have the same time
                                                                for (idx in i until itemList.size) {
                                                                    if (itemList[idx].value == setItem.value) {
                                                                        itemList[idx] =
                                                                            itemList[idx].copy(value = newTime)
                                                                    }
                                                                }

                                                                exercise.inset = itemList
                                                                triggerExerciseSave(
                                                                    exercise, superset, false
                                                                )
                                                            })
                                                    }
                                                }
                                            } else {
                                                // rep-based
                                                InputField(value = setItem.value.toString(),
                                                    label = "Reps",
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .align(Alignment.CenterVertically),
                                                    enabled = !setItem.isDone,
                                                    onChange = { newVal ->
                                                        if (newVal.trim()
                                                                .isEmpty()
                                                        ) return@InputField
                                                        val updatedReps = stringToInt(newVal)

                                                        // change later sets if they have the same reps
                                                        for (idx in i until itemList.size) {
                                                            if (itemList[idx].value == setItem.value) {
                                                                itemList[idx] =
                                                                    itemList[idx].copy(value = updatedReps)
                                                            }
                                                        }

                                                        exercise.inset = itemList
                                                        triggerExerciseSave(
                                                            exercise, superset, false
                                                        )
                                                    })
                                            }
                                        }
                                    }
                                    // endregion

                                    Spacer(modifier = Modifier.width(16.dp))

                                    // region: weight input
                                    InputField(value = exercise.weight?.getOrNull(i)?.value?.toString()
                                        ?: "",
                                        label = "weight (${userWeightUnit})",
                                        modifier = Modifier.weight(1f),
                                        enabled = !setItem.isDone,
                                        onChange = { newVal ->
                                            if (newVal.trim().isEmpty()) return@InputField;
                                            val oldWeight =
                                                exercise.weight?.getOrNull(i)?.value ?: 0;
                                            val updatedWeight = stringToInt(newVal);

                                            // update all following sets with the same weight
                                            for (idx in i until itemList.size) {
                                                if (exercise.weight?.getOrNull(idx)?.value == oldWeight) {
                                                    exercise.weight?.getOrNull(idx)?.value =
                                                        updatedWeight;
                                                }
                                            }

                                            triggerExerciseSave(exercise, superset, false);
                                        });

                                    // endregion
                                }
                            }
                        }
                        // endregion
                    }
                }
            }
        }


        private fun logSet(
            currentSuperset: SuperSet,
            restTimer: MutableIntState,
            exercise: ActiveExercise,
            userManager: UserManager,
            dao: SuperSetDao,
            triggerExerciseSave: (ActiveExercise, SuperSet, Boolean) -> Unit,
            advanceToNextExercise: (ActiveExercise?, SuperSet) -> Unit
        ) {
            Log.d(TAG, "Logging set for exercise: $exercise with rest time ${restTimer.intValue}")
            goToNextExercise(
                currentSuperset,
                restTimer,
                exercise,
                userManager,
                dao,
                { ae, sup -> triggerExerciseSave(ae, sup, false) },
                advanceToNextExercise
            )
        }

        private fun goToNextExercise(
            superset: SuperSet,
            restTimer: MutableIntState,
            exercise: ActiveExercise,
            userManager: UserManager,
            dao: SuperSetDao,
            triggerExerciseSave: (ActiveExercise, SuperSet) -> Unit,
            advanceToNextExercise: (ActiveExercise?, SuperSet) -> Unit
        ) {
            exercise.stopStopwatch()
            val idx = exercise.inset!!.indexOfFirst { !it.isDone }
            if (idx >= 0) {
                exercise.inset!![idx] = exercise.inset!![idx].apply {
                    this.isDone = true

                    if (ExerciseMeasureType.useTime(exercise.exercise.measureType)) {
                        Log.d(TAG, "Marking time-based set as done: $this")
                    } else {
                        Log.d(TAG, "Marking rep/distance set as done: $this")
                    }
                }
            }

            // all done
            if (exercise.inset?.none { !it.isDone } == true) {
                exercise.markAsDone(userWeight = userManager.getUserData()?.weight ?: 0f)
            } else Log.d(TAG, "Exercise is not done yet: $exercise")

            triggerExerciseSave(exercise, superset)
            val nextExercise = superset.goToNextExercise()
            nextExercise?.startStopwatch()

            Log.d(TAG, "Switching to next exercise: $nextExercise")
            advanceToNextExercise(nextExercise, superset)
        }
    }
}

