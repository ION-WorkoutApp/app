package com.ion606.workoutapp.screens.user

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.ion606.workoutapp.dataObjects.User.UserDataObj
import com.ion606.workoutapp.helpers.Alerts
import com.ion606.workoutapp.helpers.isWithinPastMonth
import com.ion606.workoutapp.helpers.transformTimestampToDateString
import com.ion606.workoutapp.managers.UserManager
import kotlinx.coroutines.launch
import java.util.Locale
import androidx.compose.material3.CenterAlignedTopAppBar as TopAppBar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DangerZoneScreen(
    userManager: UserManager,
    navController: NavHostController,
    context: Context,
    user: UserDataObj
) {
    val coroutineScope = rememberCoroutineScope()

    var canRequestData by remember {
        mutableStateOf(
            user.lastRequestedData.isNullOrBlank() || !isWithinPastMonth(user.lastRequestedData)
        )
    }
    var checkDataStatus by remember { mutableStateOf<String?>(null) }
    var triggerDataRequest by remember { mutableStateOf(false) }
    var triggerDelete by remember { mutableStateOf(false) }
    val alertmsg = remember { mutableStateOf(Pair<String, String?>("", "")) }

    if (triggerDelete) {
        LaunchedEffect(Unit) {
            userManager.deleteAccount(navController)
        }
    }

    if (triggerDataRequest) {
        Alerts.CreateDropdownDialog(
            title = "Data Format",
            context = context,
            options = listOf("JSON", "CSV", "ICS"),
        ) { selectedFormat ->
            if (selectedFormat == null) {
                triggerDataRequest = false
            } else {
                coroutineScope.launch {
                    canRequestData = false
                    val result =
                        userManager.requestData(selectedFormat.lowercase(Locale.getDefault()))
                    if (result.first) {
                        alertmsg.value = Pair("Data request sent successfully", "")
                    } else {
                        alertmsg.value = Pair(
                            "Failed to send data request",
                            result.second?.toString() ?: ""
                        )
                        canRequestData = true
                    }
                    triggerDataRequest = false
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val r = userManager.checkDataStatus()
            checkDataStatus = if (!r.second.toString().contains("No Request Made")) {
                "Data request status: ${r.second}"
            } else "No data request made this month"
        }
    }

    if (alertmsg.value.first.isNotEmpty()) {
        Alerts.ShowAlert(
            onClick = { alertmsg.value = Pair("", "") },
            title = alertmsg.value.first,
            text = alertmsg.value.second ?: "",
            oneButton = true
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "DANGER ZONE",
                        fontSize = 24.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBackIosNew,
                            contentDescription = "Back",
                            tint = Color.Cyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF8B0000))
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(modifier = Modifier.padding(top = 16.dp))

                Button(
                    onClick = {
                        navController.navigate("permissions")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Blue,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Permissions")
                }

                // Data Request Status
                Text(
                    text = checkDataStatus ?: "Loading Status...",
                    fontSize = 15.sp,
                    color = Color.LightGray,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .align(Alignment.CenterHorizontally)
                )

                // Request Data Button
                val btnModifier = if (canRequestData) {
                    Modifier.fillMaxWidth()
                } else {
                    Modifier
                        .fillMaxWidth()
                        .alpha(0.5f)
                        .padding(vertical = 4.dp)
                }
                Button(
                    onClick = {
                        if (canRequestData) {
                            triggerDataRequest = true
                        } else {
                            alertmsg.value = Pair(
                                "Data already requested this month",
                                "You last requested data on ${transformTimestampToDateString(user.lastRequestedData)}"
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canRequestData) Color.Blue else Color.DarkGray,
                        contentColor = Color.White
                    ),
                    modifier = btnModifier
                ) {
                    Text(
                        if (canRequestData) "Request Data" else "Data already requested this month"
                    )
                }

                // Delete Account Button
                Button(
                    onClick = {
                        triggerDelete = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Delete Account")
                }
            }
        }
    )
}
