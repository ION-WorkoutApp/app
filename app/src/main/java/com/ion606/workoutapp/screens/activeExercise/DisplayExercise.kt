package com.ion606.workoutapp.screens.activeExercise

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.ion606.workoutapp.dataObjects.Exercise
import com.ion606.workoutapp.helpers.openWebPage


@Composable
fun ExerciseCardPopup(
    exercise: Exercise,
    showPopup: MutableState<Boolean> // pass the state from outside
) {
    if (showPopup.value) {
        val showDialog = remember { mutableStateOf(false) }

        if (showDialog.value) {
            val openURL = remember { mutableStateOf(false) }
            if (openURL.value) {
                openWebPage(LocalContext.current, "https://github.com/WorkoutApp-Team/data")
                openURL.value = false
            }

            AlertDialog(
                onDismissRequest = { showDialog.value = false }, // dismiss dialog when clicked outside or back button is pressed
                title = { Text("Oh Noes!") },
                text = { Text("Seems like we're missing some data for this exercise!\n\nClick below to help out!") },
                confirmButton = {
                    TextButton(onClick = {
                        showDialog.value = false;
                        openURL.value = true;
                    }) {
                        Text("Help us out!")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog.value = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        Box(
            modifier = Modifier.fillMaxSize().zIndex(9999F),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .width(320.dp)
                    .wrapContentHeight()
                    .zIndex(99999F)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    // title
                    Text(
                        text = exercise.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // description
                    if (exercise.description != "N/A") {
                        Text(
                            text = exercise.description,
                            fontSize = 14.sp,
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically, // align items vertically
                            modifier = Modifier.fillMaxWidth().padding(8.dp) // make row take full width and add padding
                                .clickable { showDialog.value = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "No description available",
                                tint = Color.White
                            )
                            Text(
                                text = "No description available",
                                fontSize = 14.sp,
                                color = Color.White,
                                modifier = Modifier.weight(1f).padding(start = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // type and body part
                    Text(
                        text = "Type: ${exercise.type} | Body Part: ${exercise.bodyPart}",
                        fontSize = 14.sp,
                        color = Color.LightGray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // level and equipment
                    Text(
                        text = "Level: ${exercise.level} | Equipment: ${exercise.equipment}",
                        fontSize = 14.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // rating
                    Text(
                        text = "Rating: ${exercise.rating} ★",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFFD700) // gold color for stars
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // rating description
                    Text(
                        text = if (exercise.ratingDescription != "N/A") "Rated as ${exercise.ratingDescription}" else "",
                        fontSize = 14.sp,
                        color = Color.LightGray
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // close button
                    Button(
                        onClick = { showPopup.value = false },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(text = "Close")
                    }
                }
            }
        }
    }
}
