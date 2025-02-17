package com.ion606.workoutapp.managers

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.navigation.NavController
import com.google.gson.Gson
import com.ion606.workoutapp.dataObjects.ServerConfigs
import com.ion606.workoutapp.dataObjects.User.UserStats

private const val TAG = "dataManager"

class DataManager(context: Context, private val sm: SyncManager) {
    val authManager = AuthManager(context, sm)

    data class AccountLoginReturnObject(val success: Boolean, val message: String?)

    @Deprecated("Use login instead", replaceWith = ReplaceWith("login()"))
    fun isLoggedIn(): Boolean {
        return !authManager.loadToken().isNullOrEmpty()
    }

    fun loadURL(): String? {
        return sm.getBaseURL()
    }

    suspend fun login(
        data: Map<String, String>? = null, baseURL: String? = null
    ): AuthManager.AuthResult {
        return authManager.login(data, baseURL, this);
    }

    suspend fun logout(navController: NavController) {
        return authManager.logout(navController)
    }

    suspend fun createAccount(data: Map<String, Any>, baseURL: String): AccountLoginReturnObject {
        try {
            val (success, message) = sm.sendData(data, "$baseURL/auth/initaccount")
            if (success) {
                val result = authManager.login(data, baseURL, this)
                if (!result.success) return AccountLoginReturnObject(false, result.message)

                return AccountLoginReturnObject(true, null)
            } else return AccountLoginReturnObject(false, message as String)
        } catch (err: Exception) {
            Log.d(TAG, "$err")
            err.printStackTrace()
            return AccountLoginReturnObject(false, null)
        }
    }

    @SuppressLint("ApplySharedPref")
    fun clearCache() {
        this.authManager.clearAuthCache(true);
    }

    fun pingServer(url: String? = null): Boolean {
        return sm.pingServer(url)
    }

    // Returns 1 if in debug mode, 0 if not, -1 if failed to check
    suspend fun checkDebugMode(url: String? = this.loadURL()): Int {
        val r = sm.sendData(emptyMap(), path = "isindebugmode", method = "HEAD", endpoint = url);

        if (r.first) {
            return 1
        } else {
            Log.d(TAG, "Failed to check debug mode - ${r.second.toString()}")

            // 530 for cloudflared error
            if (r.second.toString().contains("404") || r.second.toString().contains("530")) {
                return -1
            }
            return 0
        }
    }

    suspend fun testConfCode(email: String, code: String, baseURL: String): Pair<Boolean, String?> {
        val data = mapOf("email" to email, "code" to code)
        val (success, message) = sm.sendData(data, "$baseURL/auth/testcode")
        return Pair(success, message as String?)
    }

    suspend fun genCode(email: String, baseURL: String): Pair<Boolean, String?> {
        val data = mapOf("email" to email)
        val (success, message) = sm.sendData(data, "$baseURL/auth/gencode")
        return Pair(success, message as String?)
    }

    suspend fun refreshToken(): Boolean {
        return this.authManager.refreshToken()
    }

    sealed class Result {
        data class Success(val data: UserStats) : Result()
        data class Error(val message: String) : Result()
    }

    suspend fun getUserStats(isRaw: Boolean = false): Result {
        val r = this.sm.sendData(payload = emptyMap(), path = "udata/stats", method = "GET")
        return if (r.first && isRaw) Result.Error(r.second as String)
        else if (r.first) Result.Success(Gson().fromJson(r.second as String, UserStats::class.java))
        else {
            Log.d(TAG, "Failed to get user stats")
            Result.Error("Failed to get user stats")
        }
    }

    sealed class ServerConfigResult {
        data class Success(val data: ServerConfigs) : ServerConfigResult()
        data class Error(val message: String) : ServerConfigResult()
    }

    suspend fun getServerConfig(endPoint: String?): ServerConfigResult {
        val r = this.sm.sendData(endpoint = endPoint, payload = emptyMap(), path = "admin/settings", method = "GET")
        return if (r.first) ServerConfigResult.Success(Gson().fromJson(r.second as String, ServerConfigs::class.java))
        else {
            Log.d(TAG, "Failed to get server config")
            ServerConfigResult.Error("Failed to get server config")
        }
    }
}
