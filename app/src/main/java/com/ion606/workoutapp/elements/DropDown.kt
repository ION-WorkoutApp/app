package com.ion606.workoutapp.elements

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.sp

@Composable
fun CreateWorkoutLogDropdown(
    buttonText: String = "Open Menu",
    modifier: Modifier = Modifier,
    context: Context,
    onEditTime: () -> Unit,
    onDelete: () -> Unit,
    onSave: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var isEditingTime by remember { mutableStateOf(false) }
    var timeText by remember { mutableStateOf(TextFieldValue("")) }

    Box(modifier = modifier) {
        Button(onClick = { expanded = true }) {
            Text(text = buttonText)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                onClick = {
                    expanded = false
                    isEditingTime = true
                },
                text = {
                    Text(
                        text = "Edit Time",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            )

            DropdownMenuItem(
                onClick = {
                    expanded = false
                    onDelete()
                },
                text = {
                    Text(
                        text = "Delete",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            )

            DropdownMenuItem(
                onClick = {
                    expanded = false
                    onSave()
                },
                text = {
                    Text(
                        text = "Save Workout",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            )
        }

        if (isEditingTime) onEditTime()
    }
}

