package com.ion606.workoutapp.helpers

import android.app.AlertDialog
import android.content.Context
import android.text.InputType
import android.widget.EditText
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp


class Alerts {
    companion object {
        @Composable
        fun ShowAlert(
            onClick: (Boolean) -> Unit,
            title: String = "Quit Workout?",
            text: String = "Are you sure you want to end the workout? Your progress will be lost!"
        ) {
            androidx.compose.material.AlertDialog(
                modifier = Modifier.background(Color.DarkGray),
                onDismissRequest = { onClick(true) },
                title = { Text(text = title) },
                text = { Text(text = text) },
                confirmButton = {
                    Button(
                        onClick = { onClick(true) }
                    ) { Text("Confirm") }
                },
                dismissButton = {
                    Button(onClick = { onClick(false) }) {
                        Text("Cancel")
                    }
                }
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
                            modifier = Modifier.fillMaxWidth()
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
        fun createAlertDialog(
            title: String = "Title",
            context: Context,
            CB: (uText: String?) -> Unit
        ) {
            // manage dialog state
            var showDialog by remember { mutableStateOf(true) }

            if (showDialog) {
                val builder: AlertDialog.Builder = AlertDialog.Builder(context)
                builder.setTitle(title)

                val input = EditText(context)
                input.inputType =
                    InputType.TYPE_CLASS_TEXT // or InputType.TYPE_TEXT_VARIATION_PASSWORD
                builder.setView(input)

                builder.setPositiveButton("OK") { _, _ ->
                    CB(input.text.toString())
                    showDialog = false // close the dialog
                }
                builder.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                    CB(null)
                    showDialog = false // close the dialog
                }

                builder.setOnDismissListener {
                    showDialog = false // ensure the dialog doesn't reappear
                }

                builder.show()
            }
        }
    }
}