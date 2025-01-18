package com.ion606.workoutapp.elements

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CreateWorkoutLogDropdown(
    buttonText: String = "Open Menu",
    modifier: Modifier = Modifier,
    onEditTime: (String) -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("Select Action") }
    var timeText by remember { mutableStateOf(TextFieldValue("")) }

    // You could modify this to fit your time editing requirement
    var isEditingTime by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Button(onClick = { expanded = true }) {
            Text(text = buttonText)
        }

        Spacer(modifier = Modifier.height(20.dp))

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                onClick = {
                    return@DropdownMenuItem;
                    selectedOption = "Edit Time"
                    expanded = false
                    isEditingTime = true
                },
                enabled = false,
                text = { Text("Edit Time") }
            )

            DropdownMenuItem(onClick = {
                selectedOption = "Delete"
                expanded = false
                onDelete()
            },
                text = { Text("Delete") })
        }

        if (isEditingTime) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = "Edit Time",
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp)
                )
                OutlinedTextField(
                    value = timeText,
                    onValueChange = { timeText = it },
                    label = { Text("Enter Time") },
                    placeholder = { Text("HH:mm") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = {
                    onEditTime(timeText.text)
                    isEditingTime = false
                }) {
                    Text("Save Time")
                }
            }
        }
    }
}
