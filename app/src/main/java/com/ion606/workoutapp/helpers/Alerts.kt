package com.ion606.workoutapp.helpers

import android.app.AlertDialog
import android.content.Context
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.password
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp


class Alerts {
    companion object {
        @Composable
        fun ShowAlert(
            onClick: (Boolean) -> Unit,
            title: String = "Quit Workout?",
            text: String = "Are you sure you want to end the workout? Your progress will be lost!",
            oneButton: Boolean = false
        ) {
            AlertDialog(
                modifier = Modifier.background(Color.DarkGray),
                onDismissRequest = { onClick(true) },
                title = { Text(text = title) },
                text = { Text(text = text) },
                confirmButton = if (oneButton) {
                    // center the button if there's only one
                    {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Button(onClick = { onClick(true) }) {
                                Text("Confirm")
                            }
                        }
                    }
                } else {
                    // align normally when there are two buttons
                    {
                        Button(onClick = { onClick(true) }) {
                            Text("Confirm")
                        }
                    }
                },
                dismissButton = if (!oneButton) {
                    {
                        Button(onClick = { onClick(false) }) {
                            Text("Cancel")
                        }
                    }
                } else null
            )
        }


        @Composable
        fun ConfirmDeleteAccountDialog(
            onDismiss: () -> Unit,
            onDelete: (String) -> Unit
        ) {
            var password by remember { mutableStateOf("") }
            var passwordVisible by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { onDismiss() },
                title = {
                    Text("Delete Account")
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Are you sure you want to delete your account? This action is irreversible.")
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Enter Password") },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                val icon =
                                    if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(icon, contentDescription = "Toggle password visibility")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { this.password() }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onDelete(password)
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { onDismiss() }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        @Composable
        fun CreateDropdownDialog(
            title: String,
            context: Context,
            options: List<String>,
            cb: (uText: String?) -> Unit
        ) {
            val builder: AlertDialog.Builder = AlertDialog.Builder(context)
            builder.setTitle(title)

            builder.setItems(options.toTypedArray()) { _, which ->
                cb(options[which])
            }

            builder.show()
        }

        @Composable
        fun CreateAlertDialog(
            title: String = "Title",
            context: Context,
            isTimeInput: Boolean = false,
            currentValue: Any? = null,
            CB: (uText: String?) -> Unit
        ) {
            val builder: AlertDialog.Builder = AlertDialog.Builder(context)
            builder.setTitle(title)

            if (isTimeInput) {
                // create a layout for minute and second inputs
                val container = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(16, 16, 16, 16)
                    gravity = Gravity.CENTER
                }

                val (minute, second) = if (currentValue is Int) {
                    intArrayOf(currentValue / 60, currentValue % 60)
                }
                else intArrayOf(0, 0)

                // minute picker
                val minutePicker = NumberPicker(context).apply {
                    minValue = 0
                    maxValue = 59
                    value = minute
                }
                container.addView(minutePicker)

                // add a colon separator for readability
                val colonView = TextView(context).apply {
                    text = ":"
                    textSize = 20f
                    setPadding(8, 0, 8, 0)
                }
                container.addView(colonView)

                // second picker
                val secondPicker = NumberPicker(context).apply {
                    minValue = 0
                    maxValue = 59
                    value = second
                }
                container.addView(secondPicker)

                builder.setView(container)

                builder.setPositiveButton("OK") { _, _ ->
                    // get the selected minute and second
                    CB("${minutePicker.value * 60 + secondPicker.value}")
                }
            } else {
                // create an edit text for text input
                val input = EditText(context).apply {
                    inputType = InputType.TYPE_CLASS_TEXT
                }
                builder.setView(input)

                builder.setPositiveButton("OK") { _, _ ->
                    CB(input.text.toString())
                }
            }

            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
                CB("")
            }

            builder.show()
        }
    }
}