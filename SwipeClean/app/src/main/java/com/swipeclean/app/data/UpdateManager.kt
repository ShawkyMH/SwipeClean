package com.swipeclean.app.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

sealed class DownloadState {
    object Idle : DownloadState()
    data class Downloading(val progress: Float, val downloadedBytes: Long, val totalBytes: Long) : DownloadState()
    data class Completed(val apkFile: File) : DownloadState()
    data class Error(val message: String) : DownloadState()
}

class UpdateManager(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "swipeclean_updates"
        const val NOTIFICATION_ID = 1001

        // Primary endpoints with fallbacks to avoid 404 or connection failures
        val UPDATE_ENDPOINTS = listOf(
            "https://raw.githubusercontent.com/ShawkyMH/SwipeClean/master/update.json",
            "https://raw.githubusercontent.com/ShawkyMH/SwipeClean/main/update.json",
            "https://api.github.com/repos/ShawkyMH/SwipeClean/releases/latest"
        )
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

    suspend fun checkUpdate(): Result<AppUpdateInfo?> = withContext(Dispatchers.IO) {
        var lastException: Exception? = null

        for (endpoint in UPDATE_ENDPOINTS) {
            try {
                val url = URL(endpoint)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 7000
                    readTimeout = 7000
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
                    val currentVersionCode = getInstalledVersionCode()

                    if (json.has("versionCode")) {
                        // Parsed update.json manifest format
                        val remoteVersionCode = json.optInt("versionCode", 0)
                        if (remoteVersionCode > currentVersionCode) {
                            val updateInfo = AppUpdateInfo(
                                versionCode = remoteVersionCode,
                                versionName = json.optString("versionName", "1.1.0"),
                                isMandatory = json.optBoolean("isMandatory", false),
                                titleAr = json.optString("titleAr", "تحديث جديد متوفر 🚀"),
                                titleEn = json.optString("titleEn", "New Update Available 🚀"),
                                changelogAr = json.optString("changelogAr", ""),
                                changelogEn = json.optString("changelogEn", ""),
                                downloadUrl = json.optString("downloadUrl", "https://github.com/ShawkyMH/SwipeClean/releases/latest/download/SwipeClean.apk")
                            )
                            return@withContext Result.success(updateInfo)
                        } else {
                            return@withContext Result.success(null) // Already up to date
                        }
                    } else if (json.has("tag_name")) {
                        // Fallback: GitHub Releases API format
                        val tagName = json.optString("tag_name", "").removePrefix("v")
                        val currentVersionName = getInstalledVersionName().removePrefix("v")

                        var downloadUrl = "https://github.com/ShawkyMH/SwipeClean/releases/latest/download/SwipeClean.apk"
                        val assetsArray = json.optJSONArray("assets")
                        if (assetsArray != null) {
                            for (i in 0 until assetsArray.length()) {
                                val assetObj = assetsArray.getJSONObject(i)
                                val assetName = assetObj.optString("name", "")
                                if (assetName.endsWith(".apk", ignoreCase = true)) {
                                    downloadUrl = assetObj.optString("browser_download_url", downloadUrl)
                                    break
                                }
                            }
                        }

                        if (isVersionGreater(tagName, currentVersionName)) {
                            val body = json.optString("body", "")
                            val updateInfo = AppUpdateInfo(
                                versionCode = (currentVersionCode + 1).toInt(),
                                versionName = tagName,
                                isMandatory = false,
                                titleAr = "تحديث جديد متوفر v$tagName 🚀",
                                titleEn = "New Update Available v$tagName 🚀",
                                changelogAr = body,
                                changelogEn = body,
                                downloadUrl = downloadUrl
                            )
                            return@withContext Result.success(updateInfo)
                        } else {
                            return@withContext Result.success(null) // Already up to date
                        }
                    }
                } else {
                    connection.disconnect()
                    lastException = Exception("HTTP Error: $responseCode at $endpoint")
                }
            } catch (e: Exception) {
                lastException = e
            }
        }

        Result.failure(lastException ?: Exception("Could not connect to update servers"))
    }

    private fun isVersionGreater(remote: String, local: String): Boolean {
        try {
            val rParts = remote.split(".").map { it.toIntOrNull() ?: 0 }
            val lParts = local.split(".").map { it.toIntOrNull() ?: 0 }
            val maxLen = maxOf(rParts.size, lParts.size)
            for (i in 0 until maxLen) {
                val r = rParts.getOrElse(i) { 0 }
                val l = lParts.getOrElse(i) { 0 }
                if (r > l) return true
                if (r < l) return false
            }
        } catch (_: Exception) {}
        return false
    }

    suspend fun downloadApk(
        apkUrl: String,
        versionName: String,
        onProgress: (Float, Long, Long) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            var currentUrl = apkUrl
            var connection: HttpURLConnection
            var redirectCount = 0

            while (true) {
                val url = URL(currentUrl)
                connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 15000
                connection.readTimeout = 25000
                connection.instanceFollowRedirects = true
                connection.setRequestProperty("User-Agent", "SwipeClean-Android")
                connection.setRequestProperty("Accept", "application/vnd.android.package-archive, application/octet-stream, */*")

                val responseCode = connection.responseCode
                if (responseCode in listOf(301, 302, 303, 307, 308)) {
                    val newLocation = connection.getHeaderField("Location")
                    connection.disconnect()
                    if (newLocation != null && redirectCount < 6) {
                        currentUrl = newLocation
                        redirectCount++
                        continue
                    }
                }
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    connection.disconnect()
                    return@withContext Result.failure(Exception("HTTP $responseCode from download server"))
                }
                break
            }

            val totalLength = connection.contentLengthLong
            val storageDir = File(context.cacheDir, "updates").apply { mkdirs() }
            val destinationFile = File(storageDir, "SwipeClean_v${versionName}.apk")
            if (destinationFile.exists()) destinationFile.delete()

            connection.inputStream.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    val buffer = ByteArray(16384)
                    var bytesRead: Int
                    var totalBytesRead = 0L

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead
                        val progress = if (totalLength > 0L) totalBytesRead.toFloat() / totalLength.toFloat() else 0f
                        onProgress(progress, totalBytesRead, totalLength)
                    }
                    output.flush()
                }
            }
            connection.disconnect()
            Result.success(destinationFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun installApk(apkFile: File): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(settingsIntent)
                    return false
                }
            }

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(installIntent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun showUpdateNotification(updateInfo: AppUpdateInfo, lang: AppLanguage) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    if (lang == AppLanguage.ARABIC) "تحديثات التطبيق" else "App Updates",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = if (lang == AppLanguage.ARABIC)
                        "إشعارات تنبيهية بتوفر إصدار جديد من تطبيق SwipeClean"
                    else
                        "Notifications about new releases for SwipeClean"
                    enableLights(true)
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo.downloadUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )

            val isAr = (lang == AppLanguage.ARABIC)
            val title = if (isAr) updateInfo.titleAr else updateInfo.titleEn
            val contentText = if (isAr)
                "إصدار جديد متوفر (v${updateInfo.versionName}). اضغط هنا للتحميل والتثبيت."
            else
                "New release available (v${updateInfo.versionName}). Tap here to download & install."

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle(title)
                .setContentText(contentText)
                .setStyle(NotificationCompat.BigTextStyle().bigText("$contentText\n\n${if (isAr) updateInfo.changelogAr else updateInfo.changelogEn}"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            notificationManager.notify(NOTIFICATION_ID, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
