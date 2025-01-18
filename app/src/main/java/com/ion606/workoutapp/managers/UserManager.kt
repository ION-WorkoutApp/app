package com.ion606.workoutapp.managers

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.gson.Gson
import com.ion606.workoutapp.dataObjects.CategoryData
import com.ion606.workoutapp.dataObjects.Exercise
import com.ion606.workoutapp.dataObjects.ExerciseFilter
import com.ion606.workoutapp.dataObjects.ParsedActiveExercise
import com.ion606.workoutapp.dataObjects.SanitizedUserDataObj
import com.ion606.workoutapp.dataObjects.UserDataObj
import com.ion606.workoutapp.helpers.Alerts
import com.ion606.workoutapp.helpers.Alerts.Companion.ConfirmDeleteAccountDialog
import kotlinx.coroutines.launch

private const val TAG = "UserManager"

class UserManager(context: Context, private val dm: DataManager, private val sm: SyncManager) {
    private val sharedPreferences: SharedPreferences
    private var userData: UserDataObj? = null

    init {
        // Initialize EncryptedSharedPreferences
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        sharedPreferences = EncryptedSharedPreferences.create(
            "AppData", // File name
            masterKeyAlias, // Master key alias
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    sealed class FetchUserDataResult {
        data class Success(val data: UserDataObj?) : FetchUserDataResult()
        data class Error(val message: String) : FetchUserDataResult()
    }

    data class ExerciseResponse(
        val exercises: List<Exercise>,
        val total: Int,
        val page: Int,
        val pageSize: Int
    )

    fun Map<String, String>.toQueryParams(): String {
        return this.entries
            .filter { it.value.isNotEmpty() }
            .joinToString("&") { "${it.key}=${it.value}" }
    }

    suspend fun fetchExercises(
        filter: ExerciseFilter? = ExerciseFilter(),
        page: Int? = 0,
        pageSize: Int = 20
    ): ExerciseResponse {
        val requestData = if (filter != null) mapOf(
            "muscleGroup" to (filter.muscleGroup ?: ""),
            "equipment" to (filter.equipment ?: ""),
            "difficulty" to (filter.difficulty ?: ""),
            "term" to (filter.term ?: ""),
            "page" to page.toString(),
            "pageSize" to pageSize.toString()
        )
        else mapOf()

        val response = sm.sendData(
            emptyMap(),
            path = "exercises?${requestData.toQueryParams()}",
            method = "GET"
        )

        if (response.first) {
            val jsonData = response.second as String
            return Gson().fromJson(jsonData, ExerciseResponse::class.java)
        } else {
            Log.d(TAG, "Error: ${response.second}")
            return ExerciseResponse(listOf(), 0, 0, 0)
        }
    }

    suspend fun fetchUserData(): FetchUserDataResult {
        val baseURL = sm.getBaseURL() ?: return FetchUserDataResult.Error("Base URL is null")
        val udata = sm.sendData(mapOf(), baseURL, "userdata", "GET", dm.authManager)

        Log.d(TAG, "DEBUG: udata: ${UserDataObj.fromString(udata.second as String)}")

        return if (udata.first) {
            FetchUserDataResult.Success(UserDataObj.fromString(udata.second as String))
        } else {
            FetchUserDataResult.Error(udata.second as String)
        }
    }

    suspend fun fetchCategories(): CategoryData? {
        val response = sm.sendData(
            mapOf("field" to "bodyPart"),
            path = "categories?field=bodyPart",
            method = "GET"
        )

        if (response.first) {
            println(response.second)
            return Gson().fromJson((response.second as String), CategoryData::class.java)
        } else {
            Log.d(TAG, "Error: ${response.second}")
            return null
        }
    }

    fun loadData(userData: UserDataObj) {
        this.userData = UserDataObj(userData)
    }

    // Add helper methods for secure storage
    fun saveToPreferences(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun getFromPreferences(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    fun clearPreferences(key: String? = null) {
        if (key == null) {
            sharedPreferences.edit().clear().apply()
        } else {
            sharedPreferences.edit().remove(key).apply()
        }
    }

    fun getUserData(): SanitizedUserDataObj? {
        return if (userData == null) null
        else SanitizedUserDataObj(userData!!)
    }

    private fun checkPassword(password: String?): Pair<Boolean, String?> {
        if (userData == null) {
            return Pair(false, "User data is null")
        } else if (password.isNullOrBlank()) {
            return Pair(false, "Password is empty")
        } else if (userData!!.password != password) {
            return Pair(false, "Password is incorrect")
        } else return Pair(true, null)
    }

    // the password fields are only needed if you're changing the password or URL
    suspend fun updateUserData(
        user: SanitizedUserDataObj,
        oldPassword: String? = null,
        newPassword: String? = null
    ): Pair<Boolean, String?> {
        val passChecked = checkPassword(oldPassword)
        if (newPassword != null && !passChecked.first) return passChecked
        else if (newPassword != null) {
            userData = user.toUserDataObj(newPassword)
            this.sm.sendData(userData!!.toMap(), path = "updatedetails", method = "PUT")
        } else {
            userData = user.toUserDataObj(userData!!.password)
            this.sm.sendData(userData!!.toMap(), path = "updatedetails", method = "PUT")
        }
        return Pair(true, null);
    }

    @Composable
    fun DeleteAccount(navController: NavController) {
        val scope = rememberCoroutineScope()
        val status = remember { mutableIntStateOf(0) }
        val errmsg = remember { mutableStateOf("") }
        val confirmed = remember { mutableStateOf(false) }

        if (errmsg.value.isNotEmpty()) {
            Alerts.ShowAlert({}, "Failed to delete account!", errmsg.value)
        }

        ConfirmDeleteAccountDialog({ confirmed.value = false }, {
            val checked = checkPassword(it);
            if (checked.first) confirmed.value = true
            else errmsg.value = checked.second ?: "Failed to check password"
        });

        if (confirmed.value) {
            if (status.intValue == 1) {
                clearPreferences()
                this.dm.clearCache()
                Alerts.ShowAlert(
                    { if (it) navController.navigate("restart_app") else navController.navigate("exit_app") },
                    "Account deleted successfully",
                    "press OK to restart the app, press Exit to quit the app"
                )
            } else if (status.intValue == 2) {
                Alerts.ShowAlert({ status.intValue = 0 }, "Failed to delete account!", errmsg.value)
            }

            LaunchedEffect("deleteaccount", sm) {
                scope.launch {
                    val r = sm.sendData(emptyMap(), path = "deleteaccount", method = "DELETE")
                    if (r.first) status.intValue = 1
                    else {
                        status.intValue = 2
                        errmsg.value = r.second as String
                    }
                }
            }
        }
    }

    fun getURL(): String? {
        return this.sm.getBaseURL()
    }

    suspend fun deleteWorkout(workout: ParsedActiveExercise): Pair<Boolean, Any?> {
        return this.sm.sendData(mapOf("id" to workout.id), path = "workout", method = "DELETE")
    }
}
