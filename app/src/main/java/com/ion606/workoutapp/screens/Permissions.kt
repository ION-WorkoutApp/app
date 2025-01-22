package com.ion606.workoutapp.screens


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController

@Composable
fun WarningBox(title: String, body: String, onSettingsClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(
                border = BorderStroke(2.dp, Color.Yellow), // yellow border
                shape = RoundedCornerShape(8.dp)
            )
            .background(
                color = Color(0xFF121212), // dark gray background
                shape = RoundedCornerShape(8.dp)
            )
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = title,
                color = Color.Yellow,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = body,
                color = Color.White,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Button(onClick = onSettingsClick) {
                Text(text = "Open Settings")
            }
        }
    }
}


fun openAppSettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    )
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class PermissionsManager {
    // utility function to check if a permission is dangerous
    private fun isDangerousPermission(permission: String): Boolean {
        val dangerousPermissions = listOf(
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACTIVITY_RECOGNITION,
            Manifest.permission.RECEIVE_BOOT_COMPLETED // dangerous on some versions
        )
        return dangerousPermissions.contains(permission)
    }

    @SuppressLint("ObsoleteSdkInt") // idk if the build version would be different on different phones
    @Composable
    fun PermissionsScreen(
        context: Context, navController: NavController, showNextButton: Boolean = true
    ) {
        val activity = context as? Activity ?: return // ensure we have an activity context

        val permissions = listOf(
            PermissionItem(
                permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    listOf(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    emptyList()
                },
                name = "Notifications",
                description = "We need this permission to notify you about rest time completion, workouts per week completed, etc",
                required = true
            ), PermissionItem(
                permissions = listOf(Manifest.permission.ACCESS_FINE_LOCATION),
                name = "Location",
                description = "Enable this if you want location-based workout suggestions and services."
            ), PermissionItem(
                permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    listOf(Manifest.permission.ACTIVITY_RECOGNITION)
                } else {
                    emptyList()
                },
                name = "Activity Recognition",
                description = "We need this permission to track your physical activities like walking and running."
            )
        )

        // state to track the status of each permission
        val permissionStates = remember { mutableStateMapOf<String, Boolean>() }

        // initialize permission states
        permissions.forEach { permissionItem ->
            permissionItem.permissions.forEach { permission ->
                permissionStates[permission] = ContextCompat.checkSelfPermission(
                    context, permission
                ) == PackageManager.PERMISSION_GRANTED
            }
        }

        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            result.forEach { (permission, granted) ->
                Log.d("PermissionsScreen", "Permission: $permission, Granted: $granted")
                permissionStates[permission] = granted
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF121212)) // dark theme background
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.padding(16.dp))

            Text(
                text = "Permissions", style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold, color = Color.White
                ), modifier = Modifier
                    .padding(bottom = 16.dp)
                    .align(Alignment.CenterHorizontally)
            )

            WarningBox(title = "Important!",
                body = "Once you grant a permission, you can only revoke it from the app settings!",
                onSettingsClick = { openAppSettings(context) })

            permissions.forEach { permissionItem ->
                val isGranted = permissionItem.permissions.all { permissionStates[it] == true }
                PermissionItemView(permissionItem = permissionItem,
                    isGranted = isGranted,
                    onToggle = { isEnabled ->
                        Log.d("Permissions", "Permission ${permissionItem.name} is $isEnabled")

                        if (isEnabled) {
                            val dangerousPermissions =
                                permissionItem.permissions.filter { isDangerousPermission(it) }
                            val deniedPermissions = dangerousPermissions.filter {
                                ContextCompat.checkSelfPermission(
                                    context, it
                                ) != PackageManager.PERMISSION_GRANTED
                            }

                            if (deniedPermissions.isNotEmpty()) {
                                val showRationale = deniedPermissions.any { permission ->
                                    ActivityCompat.shouldShowRequestPermissionRationale(
                                        activity, permission
                                    )
                                }

                                if (showRationale) {
                                    Log.d(
                                        "PermissionsScreen",
                                        "Showing rationale for: $deniedPermissions"
                                    )
                                    // TODO: Show a dialog explaining why the app needs the permissions
                                } else {
                                    Log.d(
                                        "PermissionsScreen",
                                        "Requesting dangerous permissions: $deniedPermissions"
                                    )
                                    permissionLauncher.launch(deniedPermissions.toTypedArray())
                                }
                            } else {
                                Log.d("PermissionsScreen", "All permissions already granted.")
                            }
                        } else {

                            permissionItem.permissions.forEach { permission ->
                                permissionStates[permission] = false
                            }
                        }
                    })
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter
        ) {
            if (showNextButton) {
                Button(onClick = {
                    navController.navigate("home")
                },
                    modifier = Modifier
                        .padding(16.dp)
                        .height(40.dp)
                        .fillMaxWidth(),
                    enabled = permissions.filter { it.required }.all {
                        it.permissions.all { perm -> permissionStates[perm] == true }
                    } ?: true // this true is needed for some reason
                ) {
                    Text(
                        text = "Next", fontSize = 17.sp
                    )
                }
            }
        }
    }

    @Composable
    fun PermissionItemView(
        permissionItem: PermissionItem, isGranted: Boolean, onToggle: (Boolean) -> Unit
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .background(Color(0xFF1E1E1E)) // card-like background
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = permissionItem.name, style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold, color = Color.White
                    )
                )
                Text(
                    text = permissionItem.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.Gray
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )
                if (permissionItem.required) {
                    Text(
                        text = "Required", style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold, color = Color.Red
                        ), modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            Switch(
                checked = isGranted,
                onCheckedChange = { isEnabled -> onToggle(isEnabled) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF03DAC5),
                    uncheckedThumbColor = Color(0xFF757575),
                    checkedTrackColor = Color(0xFF018786),
                    uncheckedTrackColor = Color(0xFF424242)
                )
            )
        }
    }

    data class PermissionItem(
        val permissions: List<String>,
        val name: String,
        val description: String,
        val required: Boolean = false
    )
}
