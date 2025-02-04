package com.ion606.workoutapp.logic

import android.os.CountDownTimer
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.ion606.workoutapp.R
import com.ion606.workoutapp.helpers.convertSecondsToTimeString


@Composable
fun DisplayTimer(
    timeLeft: Int,
    onPause: () -> Unit
) {
    // show a text for the countdown
    Text(
        text = convertSecondsToTimeString(timeLeft),
        color = Color.White,
        fontSize = 28.sp,
        modifier = Modifier.clickable { onPause() }
    )
}


@Composable
fun StartTimer(
    onFinishCB: (Boolean) -> Unit, // returns true on success, false on failure
    onTickCB: (Int) -> Unit = {}, // returns the remaining time -- DO NOT UPDATE THE REST TIME VAR HERE
    remainingTime: Int, // in seconds
    headerText: String = "Time Remaining"
) {
    var timeLeft by remember { mutableIntStateOf(remainingTime) } // Remaining time in seconds
    var timer: CountDownTimer? by remember { mutableStateOf(null) } // Current timer instance
    var isPaused by remember { mutableStateOf(false) } // Tracks whether the timer is paused

    // Start the timer function
    fun startTimer(time: Int) {
        Log.d("TIMER", "started timer $headerText with $remainingTime seconds")
        
        timer?.cancel() // Cancel any existing timer
        timer = object : CountDownTimer(time * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeft = (millisUntilFinished / 1000).toInt()
                onTickCB(timeLeft)
            }

            override fun onFinish() {
                onFinishCB(true)
            }
        }.start()
    }

    // Handle pausing and resuming
    LaunchedEffect(Unit) {
        startTimer(timeLeft) // Start the timer when the composable is launched
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(9999f), // Ensure the timer stays on top
        contentAlignment = Alignment.BottomCenter // Align content at the bottom center
    ) {
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
//                    .padding(16.dp)
                    .align(Alignment.BottomCenter), // Align the card at the bottom
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1E1E)
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Row to align the Cancel button to the left
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                timer?.cancel()
                                onFinishCB(true)
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .align(Alignment.CenterEnd), // Align to the left
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent, // Button background
                                contentColor = Color.White // Content color for the Icon
                            ),
                            shape = androidx.compose.foundation.shape.CircleShape, // Ensures the button is circular
                            contentPadding = PaddingValues(0.dp) // Remove default padding
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Cancel,
                                contentDescription = "Cancel Icon",
                                modifier = Modifier.fillMaxSize(), // Fill the entire button
                                tint = Color.White
                            )
                        }
                    }

                    Text(
                        text = headerText,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${timeLeft.floorDiv(60)} : ${
                            (timeLeft % 60).toString().padStart(2, '0')
                        }",
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 75.sp,
                            fontFamily = FontFamily(Font(R.font.roboto_mono))
                        )
                    )
                    Spacer(modifier = Modifier.height(15.dp))
                    Button(
                        onClick = {
                            if (isPaused) {
                                // Resume the timer
                                startTimer(timeLeft)
                            } else {
                                // Pause the timer
                                timer?.cancel()
                            }
                            isPaused = !isPaused
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                            contentDescription = if (isPaused) "Resume" else "Pause"
                        )
                    }
                }
            }
        }
    }
}
