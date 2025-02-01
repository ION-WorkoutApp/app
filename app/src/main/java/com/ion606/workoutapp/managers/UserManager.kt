package com.ion606.workoutapp.managers

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.ion606.workoutapp.dataObjects.User.Notifications
import com.ion606.workoutapp.dataObjects.User.SanitizedUserDataObj
import com.ion606.workoutapp.dataObjects.User.SocialPreferences
import com.ion606.workoutapp.dataObjects.User.UserDataObj
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private const val TAG = "UserManager"

// Extension property for DataStore
val Context.dataStore by preferencesDataStore(name = "user_preferences")

class UserManager(
    private val context: Context,
    private val dm: DataManager,
    private val sm: SyncManager
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

    /**
     * Sealed class to represent fetch user data results
     */
    sealed class FetchUserDataResult {
        data class Success(val data: UserDataObj?) : FetchUserDataResult()
        data class Error(val message: String) : FetchUserDataResult()
    }

    /**
     * Fetch exercises with optional filters
     */
    data class ExerciseResponse(
        val exercises: List<Exercise>,
        val total: Int,
        val page: Int,
        val pageSize: Int
    )

    private fun Map<String, String>.toQueryParams(): String {
        return this.entries.filter { it.value.isNotEmpty() }
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
        ) else mapOf()

        val response = sm.sendData(
            emptyMap(),
            path = "exercises/exercises?${requestData.toQueryParams()}",
            method = "GET"
        )

        if (response.first) {
            val jsonData = response.second as String
            val gson = GsonBuilder()
                .registerTypeAdapter(ExerciseMeasureType::class.java, ExerciseMeasureTypeAdapter())
                .create()

            return gson.fromJson(jsonData, ExerciseResponse::class.java)
        } else {
            Log.d(TAG, "Error fetching exercises: ${response.second}")
            return ExerciseResponse(listOf(), 0, 0, 0)
        }
    }

    /**
     * Fetch user data from backend
     */
    suspend fun fetchUserData(): FetchUserDataResult {
        val baseURL = sm.getBaseURL() ?: return FetchUserDataResult.Error("Base URL is null")
        val udata = sm.sendData(
            emptyMap(),
            path = "users/userdata",
            method = "GET",
            authManager = dm.authManager
        )

        Log.d(TAG, "DEBUG: udata: ${UserDataObj.fromString(udata.second as String)}")

        return if (udata.first) {
            FetchUserDataResult.Success(UserDataObj.fromString(udata.second as String))
        } else {
            FetchUserDataResult.Error(udata.second as String)
        }
    }

    /**
     * Fetch categories from backend
     */
    suspend fun fetchCategories(): CategoryData? {
        val response = sm.sendData(
            mapOf("field" to "bodyPart"),
            path = "exercises/categories?field=bodyPart",
            method = "GET",
            authManager = dm.authManager
        )

        if (response.first) {
            println(response.second)
            return Gson().fromJson((response.second as String), CategoryData::class.java)
        } else {
            Log.d(TAG, "Error fetching categories: ${response.second}")
            return null
        }
    }

    /**
     * Load user data into UserManager
     */
    fun loadData(userData: UserDataObj) {
        this.userData = UserDataObj(userData)
    }

    /**
     * Clear preferences from EncryptedSharedPreferences
     */
    fun clearPreferences(key: String? = null) {
        if (key == null) {
            sharedPreferences.edit().clear().apply()
        } else {
            sharedPreferences.edit().remove(key).apply()
        }
    }

    /**
     * Get sanitized user data (without sensitive info)
     */
    fun getUserData(): SanitizedUserDataObj? {
        return if (userData == null) null
        else SanitizedUserDataObj(userData!!)
    }

    /**
     * Get all user data (including sensitive info)
     */
    fun getAllUserData(): UserDataObj? {
        return userData
    }

    /**
     * Check if the provided password matches the current user's password
     */
    private fun checkPassword(password: String?): Pair<Boolean, String?> {
        if (userData == null) {
            return Pair(false, "User data is null")
        } else if (password.isNullOrBlank()) {
            return Pair(false, "Password is empty")
        } else if (userData!!.password != password) {
            return Pair(false, "Password is incorrect")
        } else return Pair(true, null)
    }

    /**
     * Update user data including password changes
     */
    suspend fun updateUserData(
        user: SanitizedUserDataObj,
        oldPassword: String? = null,
        newPassword: String? = null
    ): Pair<Boolean, String?> {
        val passChecked = checkPassword(oldPassword)
        if (newPassword != null && !passChecked.first) return passChecked
        return if (newPassword != null) {
            // User is changing password
            val updatedUser = user.toUserDataObj(newPassword)
            val response = sm.sendData(
                updatedUser.toMap(),
                path = "users/updatedetails",
                method = "PUT",
                authManager = dm.authManager
            )
            if (response.first) {
                userData = updatedUser.copy(password = "") // Exclude password
                Pair(true, null)
            } else {
                Log.e(TAG, "Failed to update user data: ${response.second}")
                Pair(false, response.second as? String ?: "Unknown error.")
            }
        } else {
            // User is updating other details without changing password
            val updatedUser = user.toUserDataObj(userData!!.password)
            val response = sm.sendData(
                updatedUser.toMap(),
                path = "users/updatedetails",
                method = "PUT",
                authManager = dm.authManager
            )
            if (response.first) {
                this.userData = updatedUser.copy()
                Pair(true, null)
            } else {
                Log.e(TAG, "Failed to update user data: ${response.second}")
                Pair(false, response.second as? String ?: "Unknown error.")
            }
        }
    }

    /**
     * Update Notifications Preferences
     */
    suspend fun updateNotifications(updatedPrefs: Notifications): Pair<Boolean, String?> {
        val currentUser = getAllUserData() ?: return Pair(false, "User data not loaded.")
        val updatedUser = currentUser.copy(notifications = updatedPrefs)
        return updateUserInBackend(updatedUser)
    }

    /**
     * Update Social Preferences
     */
    suspend fun updateSocialPreferences(updatedPrefs: SocialPreferences): Pair<Boolean, String?> {
        val currentUser = getAllUserData() ?: return Pair(false, "User data not loaded.")
        val updatedUser = currentUser.copy(socialPreferences = updatedPrefs)
        return updateUserInBackend(updatedUser)
    }

    /**
     * Generic function to update user in the backend
     */
    private suspend fun updateUserInBackend(updatedUser: UserDataObj): Pair<Boolean, String?> {
        return try {
            val response = sm.sendData(
                updatedUser.toMap(),
                path = "users/updatedetails",
                method = "PUT",
                authManager = dm.authManager
            )
            if (response.first) {
                userData = updatedUser.copy(password = "") // Exclude password
                Pair(true, null)
            } else {
                Log.e(TAG, "Failed to update user preferences: ${response.second}")
                Pair(false, response.second as? String ?: "Unknown error.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during user update: ${e.message}")
            Pair(false, e.message)
        }
    }

    /**
     * Delete Account
     */
    suspend fun deleteAccount(navController: NavController): Pair<Boolean, String?> {
        return try {
            val response = sm.sendData(
                emptyMap(),
                path = "users/deleteaccount",
                method = "DELETE",
                authManager = dm.authManager
            )
            if (response.first) {
                // Clear local data
                clearPreferences()
                dm.clearCache()
                // Navigate to restart or exit
                navController.navigate("restart_app") {
                    popUpTo("home") { inclusive = true }
                }
                Pair(true, null)
            } else {
                Log.e(TAG, "Failed to delete account: ${response.second}")
                Pair(false, response.second as? String ?: "Unknown error.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during account deletion: ${e.message}")
            Pair(false, e.message)
        }
    }

    /**
     * Toggle Minimalist Mode
     */
    suspend fun toggleMinimalistMode() {
        context.dataStore.edit { preferences ->
            val currentMode = preferences[MINIMALIST_MODE_KEY] ?: false
            Log.d(TAG, "SET USER MIN MODE TO ${!currentMode}")
            preferences[MINIMALIST_MODE_KEY] = !currentMode
        }
    }

    /**
     * Get Base URL from SyncManager
     */
    fun getURL(): String? {
        return sm.getBaseURL()
    }

    /**
     * Delete a specific workout
     */
    suspend fun deleteWorkout(workout: ParsedActiveExercise): Pair<Boolean, Any?> {
        return sm.sendData(
            mapOf("id" to workout.id),
            path = "workouts/workout",
            method = "DELETE",
            authManager = dm.authManager
        )
    }

    /**
     * Request user data export
     */
    suspend fun requestData(format: String? = null): Pair<Boolean, Any?> {
        if (format.isNullOrEmpty()) return Pair(false, "Format is empty")
        if (!listOf("json", "csv", "ics").contains(format.lowercase())) return Pair(false, "Invalid format")

        val canRequest = sm.sendData(
            emptyMap(),
            path = "udata/canrequest",
            method = "GET",
            authManager = dm.authManager
        )
        if (!canRequest.first) return canRequest

        return sm.sendData(
            mapOf("format" to format.lowercase()),
            path = "udata/export",
            method = "POST",
            authManager = dm.authManager
        )
    }

    /**
     * Check data export request status
     */
    suspend fun checkDataStatus(): Pair<Boolean, Any?> {
        return sm.sendData(
            emptyMap(),
            path = "udata/status",
            method = "GET",
            authManager = dm.authManager
        )
    }
}
