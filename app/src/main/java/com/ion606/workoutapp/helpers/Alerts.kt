package com.ion606.workoutapp.helpers

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
                        Text("Exit")
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
                                val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
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

    }
}