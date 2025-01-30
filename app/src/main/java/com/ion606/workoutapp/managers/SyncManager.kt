package com.ion606.workoutapp.managers

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.ion606.workoutapp.BuildConfig
import com.ion606.workoutapp.helpers.URLHelpers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Headers.Companion.toHeaders
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import kotlin.coroutines.resume

private const val TAG = "SyncManager"

class SyncManager(private var baseURL: String? = null, private var context: Context? = null) {
    private val headers = mutableMapOf("content-type" to "application/json");
    private var authManager: AuthManager? = null;

    init {
        if (context != null) this.authManager = AuthManager(context!!, this);
    }

    // wrapped in a function so as to not accidentally change the base URL
//    TODO: implement saving
    fun changeBaseURL(newURL: String) {
        this.baseURL = newURL;
    }

    fun getBaseURL(): String? {
        return this.baseURL;
    }

    fun setHeader(headername: String, headerVal: String) {
        this.headers[headername] = headerVal;
    }

    fun populateData(token: String, baseURL: String) {
        this.changeBaseURL(baseURL);
        this.setHeader("authorization", token);
    }

    // helper callback function for sendData
    fun sendDataCB(
        scope: CoroutineScope,
        payload: Map<String, Any>,
        endpoint: String? = this.baseURL,
        path: String? = null,
        method: String = "POST",
        authManager: AuthManager? = this.authManager,
        cb: (Pair<Boolean, Any?>) -> Unit
    ) {
        scope.launch {
            val result = sendData(payload, endpoint, path, method, authManager)
            cb(result)
        }
    }

    private fun extractResponseMessage(jsonString: String?): String? {
        return try {
            if (jsonString == null) return null
            val jsonObject = JSONObject(jsonString)
            if (jsonObject.length() == 1) { // if only one key
                jsonObject.optString("message", jsonObject.optString("error", jsonString))
            } else {
                jsonString
            }
        } catch (e: Exception) {
            jsonString
        }
    }

    suspend fun sendData(
        payload: Map<String, Any>,
        endpoint: String? = this.baseURL,
        path: String? = null,
        method: String = "POST",
        authManager: AuthManager? = null
    ): Pair<Boolean, Any?> {
        val baseURLToUse = URLHelpers.transformURL(endpoint) ?: return Pair(false, "URL is null")

        val jsonData: String
        try {
            jsonData = Gson().toJson(payload)
        } catch (e: Exception) {
            Log.d(TAG, "Error: Failed to serialize data: ${e.message}")
            e.printStackTrace()
            return Pair(false, "Failed to serialize data")
        }

        val requestBody = jsonData.toRequestBody("application/json".toMediaTypeOrNull())
        val urlToUse = if (!path.isNullOrEmpty()) "$baseURLToUse/$path" else baseURLToUse

        if (path == "isindebugmode") Log.d(TAG, "debug pinging $baseURLToUse")
        else if (BuildConfig.SENSITIVE_LOGGING_ENABLED) Log.d(
            TAG, "Debug: Sending a $method request to $urlToUse with data $jsonData"
        )

        val request = Request.Builder().url(urlToUse).headers(headers.toMap().toHeaders())
            .method(method, if (method == "GET" || method == "HEAD") null else requestBody).build()

        return suspendCancellableCoroutine { continuation ->
            OkHttpClient().newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.d(TAG, "Error: Failed to sync data to $urlToUse: ${e.message}")
                    e.printStackTrace()

                    if (continuation.isActive) continuation.resume(
                        Pair(
                            false, "Failed to sync data"
                        )
                    )
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use { // Ensure the response body is closed
                        if (response.isSuccessful) {
                            val responseBody = response.body?.string()

                            if (BuildConfig.SENSITIVE_LOGGING_ENABLED && path != "isindebugmode") Log.d(
                                TAG, "Debug: Data synced successfully: $responseBody"
                            )

                            if (continuation.isActive) continuation.resume(Pair(true, extractResponseMessage(responseBody)))
                            else null;
                        } else {
                            val errmsg = response.body?.string() ?: response.message
                            Log.d(
                                TAG,
                                "Error: Server error. Code: ${response.code}, Message: ${errmsg}, Url: $urlToUse"
                            )

                            if (errmsg.contains("Token expired", ignoreCase = true)) {
                                Log.d(TAG, "Token expired. Attempting to refresh.")

                                if (authManager == null) {
                                    Log.d(TAG, "Auth manager is null. Cannot refresh token.")
                                    if (continuation.isActive) continuation.resume(
                                        Pair(
                                            false, "Token expired"
                                        )
                                    )

                                    return
                                }

                                // Launch a coroutine to refresh the token
                                CoroutineScope(Dispatchers.IO).launch {
                                    val refreshed = authManager.refreshToken()
                                    if (refreshed) {
                                        sendData(payload, endpoint, path, method, authManager).let {
                                            // don't use extractResponseMessage because it might be smth else?
                                            if (continuation.isActive) continuation.resume(it)
                                        }
                                    } else {
                                        Log.d(TAG, "Failed to refresh token. User needs to log in.")
                                        if (continuation.isActive) continuation.resume(
                                            Pair(
                                                false, "Failed to refresh token"
                                            )
                                        )
                                    }
                                }
                            } else if (errmsg.contains("Invalid token", ignoreCase = true)) {
                                Log.d(TAG, "Invalid token. User needs to log in.")

                                if (continuation.isActive) continuation.resume(Pair(false, extractResponseMessage(errmsg)))
                                else null;
                            } else {
                                if (continuation.isActive) continuation.resume(Pair(false, extractResponseMessage(errmsg)))
                                else null;
                            }
                        }
                    }
                }
            })
        }
    }


    fun pingServer(url: String? = null): Boolean {
        val u = url ?: this.baseURL;
        if (u.isNullOrEmpty()) return false;

        // try pinging three times
        for (i in 1..3) {
            Log.d(TAG, "DEBUG: Pinging server at $u");

            val request = Request.Builder().url("$u/ping").head().build();

            try {
                val response = OkHttpClient().newCall(request).execute()
                if (response.use { response.isSuccessful }) return true
                else Log.d(TAG, "Error: Failed to ping server on attempt $i")
            } catch (e: Exception) {
                Log.d(TAG, "Error: Failed to ping server with error $e on attempt $i")
                e.printStackTrace()
            }

            // wait 1 second before trying again
            Thread.sleep(1000)
        }

        return false;
    }
}
