package com.ion606.workoutapp.managers

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.navigation.NavController

private const val TAG = "dataManager"

class DataManager(context: Context, private val sm: SyncManager) {
    val authManager = AuthManager(context, sm)

    data class AccountLoginReturnObject(val success: Boolean, val message: String?)

    @Deprecated("Use login instead", replaceWith = ReplaceWith("login()"))
    fun isLoggedIn(): Boolean {
        return !authManager.loadToken().isNullOrEmpty()
    }

    fun loadURL() : String? {
        return sm.getBaseURL()
    }

    suspend fun login(data: Map<String, String>? = null, baseURL: String? = null): AuthManager.AuthResult {
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

    suspend fun checkDebugMode(url: String? = this.loadURL()): Pair<Boolean, Any?> {
        return sm.sendData(emptyMap(), path = "isindebugmode", method = "HEAD", endpoint = url);
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
}
