package com.ion606.workoutapp.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.CenterAlignedTopAppBar as TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ion606.workoutapp.dataObjects.User.Notifications
import com.ion606.workoutapp.dataObjects.User.SanitizedUserDataObj
import com.ion606.workoutapp.elements.DropdownMenuField
import com.ion606.workoutapp.helpers.Alerts
import com.ion606.workoutapp.managers.UserManager
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    navController: NavController,
    userManager: UserManager
) {
    val coroutineScope = rememberCoroutineScope()
    val notifications = userManager.getAllUserData()?.notifications

    // State variables
    var remindersEnabled by remember { mutableStateOf(notifications?.remindersEnabled ?: true) }
    var notificationFrequency by remember {
        mutableStateOf(
            notifications?.notificationFrequency ?: "daily"
        )
    }
    var preferredReminderTime by remember {
        mutableStateOf(
            notifications?.preferredReminderTime ?: "08:00 AM"
        )
    }

    val alertMsg = remember { mutableStateOf(Pair("", "")) }

    val frequencies = listOf("daily", "weekly", "none")

    if (alertMsg.value.first.isNotEmpty()) {
        Alerts.ShowAlert(
            onClick = { alertMsg.value = Pair("", "") },
            title = alertMsg.value.first,
            text = alertMsg.value.second,
            oneButton = true
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Reminders Enabled", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = remindersEnabled,
                        onCheckedChange = { remindersEnabled = it }
                    )
                }
                DropdownMenuField(
                    label = "Notification Frequency",
                    options = frequencies,
                    value = notificationFrequency,
                    onValueChange = { notificationFrequency = it }
                )
                OutlinedTextField(
                    value = preferredReminderTime,
                    onValueChange = { preferredReminderTime = it },
                    label = { Text("Preferred Reminder Time") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        // Update notifications
                        val newUser = userManager.getAllUserData()?.copy(
                            notifications = notifications?.copy(
                                remindersEnabled = remindersEnabled,
                                notificationFrequency = notificationFrequency,
                                preferredReminderTime = preferredReminderTime
                            ) ?: /* Initialize if null */ Notifications(
                                remindersEnabled = remindersEnabled,
                                notificationFrequency = notificationFrequency,
                                preferredReminderTime = preferredReminderTime
                            )
                        ) ?: return@Button

                        coroutineScope.launch {
                            val result = userManager.updateUserData(SanitizedUserDataObj(newUser))
                            if (result.first) {
                                alertMsg.value = Pair("Success", "Notifications settings updated.")
                            } else {
                                alertMsg.value = Pair(
                                    "Error",
                                    result.second ?: "Unknown error."
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save")
                }
            }
        }
    )
}
