package com.ion606.workoutapp.managers

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.navigation.NavController
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.ion606.workoutapp.helpers.URLHelpers
import org.json.JSONObject

private const val TAG = "authManager"

class AuthManager(private val context: Context, private val sm: SyncManager) {
    private val sharedPreferences: SharedPreferences

    init {
        val masterKeyAlias = try {
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        } catch (e: Exception) {
            Log.e(TAG, "KeyStore error. Resetting KeyStore.", e)
            context.deleteSharedPreferences("AppData")
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC) // Regenerate the key
        }

        sharedPreferences = try {
            EncryptedSharedPreferences.create(
                "AppData",
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Failed to initialize EncryptedSharedPreferences. Clearing corrupted data.",
                e
            )
            context.deleteSharedPreferences("AppData")
            EncryptedSharedPreferences.create(
                "AppData",
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }

    data class AuthResult(val success: Boolean, val message: String?)

    suspend fun login(
        data: Map<String, Any>? = null,
        baseURL: String? = null,
        dataManager: DataManager
    ): AuthResult {
        var token = loadToken()
        val url = baseURL ?: loadURL()

        if (!token.isNullOrEmpty() && !url.isNullOrEmpty()) {
            sm.populateData(token, url)
            return AuthResult(true, null)
        } else if (data.isNullOrEmpty() || baseURL.isNullOrEmpty()) {
            return AuthResult(false, "No token or URL found")
        }

        val credentials: Map<String, Any> = data.ifEmpty {
            mapOf(
                "username" to sharedPreferences.getString("username", null).toString(),
                "password" to sharedPreferences.getString("password", null).toString()
            )
        }

        sm.changeBaseURL(credentials["serverurl"].toString())

        val (success, retval) = sm.sendData(credentials, "$baseURL/auth/checkcredentials")

        if (success) {
            token = JSONObject(retval as String).get("token").toString()
            val refreshToken = JSONObject(retval).get("refreshToken").toString()
            saveToken(token)
            saveRefreshToken(refreshToken)
            saveURL(baseURL)
            sm.setHeader("authorization", token)
            return AuthResult(true, null)
        } else {
            return AuthResult(false, retval as String)
        }
    }

    suspend fun logout(navController: NavController) {
        val r = this.sm.sendData(mapOf(), "${loadURL()}/users/logout")

        // failed to clear the token, but that doesn't matter for the user
        if (!r.first) Log.e(TAG, "Failed to logout: ${r.second}")
        clearAuthCache(true)
        navController.navigate("login_signup")

    }

    suspend fun refreshToken(): Boolean {
        val refreshToken = loadRefreshToken()
        val baseURL = loadURL()

        if (refreshToken.isNullOrEmpty() || baseURL.isNullOrEmpty()) {
            Log.d(TAG, "No refresh token or base URL found")
            return false
        }

        val data = mapOf("refreshToken" to refreshToken)
        val (success, retval) = sm.sendData(data, "$baseURL/auth/refresh-token")
        if (success) {
            val newToken = JSONObject(retval as String).get("token").toString()
            saveToken(newToken)
            sm.setHeader("authorization", newToken)
            return true
        } else {
            Log.d(TAG, "Failed to refresh token: $retval")
            return false
        }
    }

    @SuppressLint("ApplySharedPref")
    fun clearAuthCache(clearAll: Boolean = false) {
        try {
            if (clearAll) sharedPreferences.edit().clear().commit()
            else sharedPreferences.edit().remove("auth_token").remove("refresh_token").commit()
        } catch (e: Exception) {
            if (e.message?.contains("Could not decrypt key. decryption failed") == false) Log.e(
                TAG,
                "clearAuthCache failed with error",
                e
            )
            else Log.e(TAG, "Failed to decrypt key. Clearing preferences...")
            context.deleteSharedPreferences("AppData")
        }
    }

    private fun saveToken(token: String) {
        sharedPreferences.edit().putString("auth_token", token).apply()
    }

    private fun saveRefreshToken(refreshToken: String) {
        sharedPreferences.edit().putString("refresh_token", refreshToken).apply()
    }

    private fun saveURL(url: String) {
        sharedPreferences.edit().putString("url", url).apply()
    }

    fun loadToken(): String? {
        return sharedPreferences.getString("auth_token", null)
    }

    private fun loadRefreshToken(): String? {
        return sharedPreferences.getString("refresh_token", null)
    }

    fun loadURL(): String? {
        val url = sharedPreferences.getString("url", null)
        return URLHelpers.transformURL(url)
    }
}
