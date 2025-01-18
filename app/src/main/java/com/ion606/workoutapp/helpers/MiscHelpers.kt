package com.ion606.workoutapp.helpers

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.ion606.workoutapp.dataObjects.Exercise
import java.net.NetworkInterface
import java.net.URL
import kotlin.math.min


class URLHelpers {
    companion object {
        fun getLocalIPAddress(): String? {
            try {
                val interfaces = NetworkInterface.getNetworkInterfaces()
                for (networkInterface in interfaces) {
                    if (networkInterface.isUp && !networkInterface.isLoopback && !networkInterface.displayName.contains(
                            "docker",
                            true
                        )
                    ) {
                        val addresses = networkInterface.inetAddresses
                        for (address in addresses) {
                            if (!address.isLoopbackAddress && address.hostAddress.indexOf(':') == -1) {
                                println(networkInterface);
                                return address.hostAddress // Return IPv4 address
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        fun transformURL(u: String?): String? {
            println("DEBUG: transforming $u")
            return try {
                if (u == null) return null

                val url = URL(u)
                if (url.host == "testing") {
                    val localIp = getLocalIPAddress() ?: "127.0.0.1"
                    URL("http", localIp, 1221, url.file).toString()
                } else u
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}

fun openWebPage(context: Context, url: String?) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url));
    context.startActivity(Intent.createChooser(intent, "Choose browser"))
}

fun convertSecondsToTimeString(seconds: Int, showHours: Boolean = false): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60

    // Format the time as HH:mm:ss
    return if (showHours) "%02d:%02d:%02d".format(hours, minutes, secs) else "%02d:%02d".format(minutes, secs)
}


fun String.similarityTo(query: String): Int {
    val len1 = this.length
    val len2 = query.length
    val dp = Array(len1 + 1) { IntArray(len2 + 1) }
    for (i in 0..len1) dp[i][0] = i
    for (j in 0..len2) dp[0][j] = j

    for (i in 1..len1) {
        for (j in 1..len2) {
            dp[i][j] = if (this[i - 1] == query[j - 1]) {
                dp[i - 1][j - 1]
            } else {
                min(dp[i - 1][j - 1], min(dp[i - 1][j], dp[i][j - 1])) + 1
            }
        }
    }
    return dp[len1][len2]
}

// Custom sort function based on similarity or default sort
fun List<Exercise>.sortBySimilarityOrDefault(query: String?): List<Exercise> {
    return if (!query.isNullOrEmpty()) {
        this.sortedBy { it.title.similarityTo(query) } // Sort by similarity (lower is better)
    } else {
        this.sortedBy({ it.title }) // Default sort (alphabetical)
    }
}