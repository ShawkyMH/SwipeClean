package com.swipeclean.app.data

import android.content.Context
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class UpdateManager(private val context: Context) {

    companion object {
        // GitHub raw file endpoint for update manifest
        const val DEFAULT_UPDATE_URL = "https://raw.githubusercontent.com/ShawkyMH/SwipeClean/main/update.json"
    }

    fun getInstalledVersionCode(): Long {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
        } catch (e: Exception) {
            1L
        }
    }

    fun getInstalledVersionName(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }

    suspend fun checkUpdate(updateUrl: String = DEFAULT_UPDATE_URL): Result<AppUpdateInfo?> = withContext(Dispatchers.IO) {
        try {
            val url = URL(updateUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "SwipeClean-Android")
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val jsonString = reader.use { it.readText() }
                connection.disconnect()

                val json = JSONObject(jsonString)
                val remoteVersionCode = json.optInt("versionCode", 0)
                val currentVersionCode = getInstalledVersionCode()

                if (remoteVersionCode > currentVersionCode) {
                    val updateInfo = AppUpdateInfo(
                        versionCode = remoteVersionCode,
                        versionName = json.optString("versionName", "1.0"),
                        isMandatory = json.optBoolean("isMandatory", false),
                        titleAr = json.optString("titleAr", "تحديث جديد متوفر"),
                        titleEn = json.optString("titleEn", "New update available"),
                        changelogAr = json.optString("changelogAr", ""),
                        changelogEn = json.optString("changelogEn", ""),
                        downloadUrl = json.optString("downloadUrl", "https://github.com/ShawkyMH/SwipeClean/releases")
                    )
                    Result.success(updateInfo)
                } else {
                    Result.success(null) // Already up to date
                }
            } else {
                connection.disconnect()
                Result.failure(Exception("HTTP Error: $responseCode"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
