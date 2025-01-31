package com.ion606.workoutapp.screens.user

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
import com.ion606.workoutapp.dataObjects.User.SanitizedUserDataObj
import com.ion606.workoutapp.helpers.Alerts
import com.ion606.workoutapp.managers.UserManager
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialPreferencesScreen(
    navController: NavController,
    userManager: UserManager
) {
    val coroutineScope = rememberCoroutineScope()
    val social = userManager.getAllUserData()?.socialPreferences

    // State variables
    var socialSharing by remember { mutableStateOf(social?.socialSharing ?: false) }
    var leaderboardParticipation by remember { mutableStateOf(social?.leaderboardParticipation ?: false) }
    var badgesAndAchievements by remember {
        mutableStateOf(social?.badgesAndAchievements?.joinToString(", ") ?: "")
    }

    val alertMsg = remember { mutableStateOf(Pair("", "")) }

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
                title = { Text("Social Preferences") },
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
                    Text("Social Sharing", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = socialSharing,
                        onCheckedChange = { socialSharing = it }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Leaderboard Participation", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = leaderboardParticipation,
                        onCheckedChange = { leaderboardParticipation = it }
                    )
                }

                Button(
                    onClick = {
                        // Update social preferences
                        val newUser = userManager.getAllUserData()?.copy(socialPreferences = social?.copy(
                            socialSharing = socialSharing,
                            leaderboardParticipation = leaderboardParticipation,
                            badgesAndAchievements = badgesAndAchievements.split(",")
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }
                        ) ?: /* Initialize if null */ com.ion606.workoutapp.dataObjects.User.SocialPreferences(
                            socialSharing = socialSharing,
                            leaderboardParticipation = leaderboardParticipation,
                            badgesAndAchievements = badgesAndAchievements.split(",")
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }
                        )) ?: return@Button

                        coroutineScope.launch {
                            val result = userManager.updateUserData(SanitizedUserDataObj(newUser))
                            if (result.first) {
                                alertMsg.value = Pair("Success", "Social preferences updated.")
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
