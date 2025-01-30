package com.ion606.workoutapp.managers

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.NavController
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ion606.workoutapp.dataObjects.CategoryData
import com.ion606.workoutapp.dataObjects.Exercise
import com.ion606.workoutapp.dataObjects.ExerciseFilter
import com.ion606.workoutapp.dataObjects.ExerciseMeasureType
import com.ion606.workoutapp.dataObjects.ExerciseMeasureTypeAdapter
import com.ion606.workoutapp.dataObjects.ParsedActiveExercise
import com.ion606.workoutapp.dataObjects.SanitizedUserDataObj
import com.ion606.workoutapp.dataObjects.UserDataObj
import com.ion606.workoutapp.helpers.Alerts
import com.ion606.workoutapp.helpers.Alerts.Companion.ConfirmDeleteAccountDialog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


private const val TAG = "UserManager"
val Context.dataStore by preferencesDataStore(name = "user_preferences")

class UserManager(
    private val context: Context, private val dm: DataManager, private val sm: SyncManager
) {
    private val sharedPreferences: SharedPreferences
    private var userData: UserDataObj? = null
    private val MINIMALIST_MODE_KEY = booleanPreferencesKey("minimalist_mode")
    var isMinimalistMode: Boolean by mutableStateOf(false)
        private set

    val isMinimalistModeFlow: Flow<Boolean>
        get() = context.dataStore.data.map { preferences ->
            preferences[MINIMALIST_MODE_KEY] ?: false
        }


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
        runBlocking {
            isMinimalistMode = context.dataStore.data.map { preferences ->
                preferences[MINIMALIST_MODE_KEY] ?: false
            }.first()
        }

    }

    sealed class FetchUserDataResult {
        data class Success(val data: UserDataObj?) : FetchUserDataResult()
        data class Error(val message: String) : FetchUserDataResult()
    }

    data class ExerciseResponse(
        val exercises: List<Exercise>, val total: Int, val page: Int, val pageSize: Int
    )

    fun Map<String, String>.toQueryParams(): String {
        return this.entries.filter { it.value.isNotEmpty() }
            .joinToString("&") { "${it.key}=${it.value}" }
    }

    suspend fun fetchExercises(
        filter: ExerciseFilter? = ExerciseFilter(), page: Int? = 0, pageSize: Int = 20
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
            emptyMap(), path = "exercises/exercises?${requestData.toQueryParams()}", method = "GET"
        )

        if (response.first) {
            val jsonData = response.second as String
            val gson = GsonBuilder()
                .registerTypeAdapter(ExerciseMeasureType::class.java, ExerciseMeasureTypeAdapter())
                .create()

            return gson.fromJson(jsonData, ExerciseResponse::class.java)
        } else {
            Log.d(TAG, "Error: ${response.second}")
            return ExerciseResponse(listOf(), 0, 0, 0)
        }
    }

    suspend fun fetchUserData(): FetchUserDataResult {
        val baseURL = sm.getBaseURL() ?: return FetchUserDataResult.Error("Base URL is null")
        val udata = sm.sendData(mapOf(), baseURL, "users/userdata", "GET", dm.authManager)

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
            path = "exercises/categories?field=bodyPart",
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
        user: SanitizedUserDataObj, oldPassword: String? = null, newPassword: String? = null
    ): Pair<Boolean, String?> {
        val passChecked = checkPassword(oldPassword)
        if (newPassword != null && !passChecked.first) return passChecked
        else if (newPassword != null) {
            userData = user.toUserDataObj(newPassword)
            this.sm.sendData(userData!!.toMap(), path = "users/updatedetails", method = "PUT")
        } else {
            userData = user.toUserDataObj(userData!!.password)
            this.sm.sendData(userData!!.toMap(), path = "users/updatedetails", method = "PUT")
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
                    val r = sm.sendData(emptyMap(), path = "users/deleteaccount", method = "DELETE")
                    if (r.first) status.intValue = 1
                    else {
                        status.intValue = 2
                        errmsg.value = r.second as String
                    }
                }
            }
        }
    }

    // Function to toggle the preference
    suspend fun toggleMinimalistMode() {
        this.context.dataStore.edit { preferences ->
            val currentMode = preferences[MINIMALIST_MODE_KEY] ?: false
            Log.d(TAG, "SET USER MIN MODE TO ${!currentMode}")
            preferences[MINIMALIST_MODE_KEY] = !currentMode
//            this.isMinimalistMode = !currentMode
        }
    }

    fun getURL(): String? {
        return this.sm.getBaseURL()
    }

    suspend fun deleteWorkout(workout: ParsedActiveExercise): Pair<Boolean, Any?> {
        return this.sm.sendData(
            mapOf("id" to workout.id), path = "workouts/workout", method = "DELETE"
        )
    }

    suspend fun requestData(format: String? = null): Pair<Boolean, Any?> {
        if (format.isNullOrEmpty()) return Pair(false, "Format is empty")
        else if (!listOf("json", "csv", "ics").contains(format)) return Pair(
            false, "Invalid format"
        )
        val r = this.sm.sendData(emptyMap(), path = "udata/canrequest", method = "GET")
        if (!r.first) return r;

        return this.sm.sendData(
            mapOf(
                "format" to format
            ), path = "udata/export", method = "POST"
        )
    }

    suspend fun checkDataStatus(): Pair<Boolean, Any?> {
        return this.sm.sendData(emptyMap(), path = "udata/status", method = "GET")
    }
}
