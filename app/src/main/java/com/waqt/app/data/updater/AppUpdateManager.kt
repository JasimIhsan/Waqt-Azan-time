package com.waqt.app.data.updater

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.FileProvider
import com.waqt.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

data class AppUpdateInfo(
    val isUpdateAvailable: Boolean,
    val currentVersion: String,
    val latestVersion: String,
    val releaseTitle: String,
    val releaseNotes: String,
    val downloadUrl: String?,
    val apkFileName: String?,
    val releasePageUrl: String,
    val publishedAt: String? = null
)

class AppUpdateManager(
    private val repoOwner: String = "JasimIhsan",
    private val candidateRepos: List<String> = listOf("Waqt-Azan-time", "Waqt", "Adhan", "waqt")
) {

    suspend fun checkForUpdates(
        currentVersion: String = BuildConfig.VERSION_NAME
    ): Result<AppUpdateInfo> = withContext(Dispatchers.IO) {
        var lastException: Exception? = null

        for (repoName in candidateRepos) {
            try {
                val endpoint = "https://api.github.com/repos/$repoOwner/$repoName/releases/latest"
                val url = URL(endpoint)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 8000
                    readTimeout = 8000
                    setRequestProperty("Accept", "application/vnd.github.v3+json")
                    setRequestProperty("User-Agent", "Waqt-App/$currentVersion")
                }

                val responseCode = connection.responseCode

                if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    // Try next candidate repository
                    continue
                }

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    val errorStream = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                    lastException = Exception("GitHub API error ($responseCode): $errorStream")
                    continue
                }

                val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseBody)

                val tagName = json.optString("tag_name", "").trim()
                val releaseTitle = json.optString("name", tagName)
                val releaseNotes = json.optString("body", "No release notes provided.")
                val htmlUrl = json.optString("html_url", "https://github.com/$repoOwner/$repoName/releases")
                val publishedAt = json.optString("published_at").takeIf { it.isNotBlank() }

                // Parse assets to find APK download URL
                var apkDownloadUrl: String? = null
                var apkFileName: String? = null

                val assetsArray = json.optJSONArray("assets")
                if (assetsArray != null) {
                    for (i in 0 until assetsArray.length()) {
                        val asset = assetsArray.getJSONObject(i)
                        val name = asset.optString("name", "")
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            apkDownloadUrl = asset.optString("browser_download_url").takeIf { it.isNotBlank() }
                            apkFileName = name
                            // Prioritize release or waqt apks if multiple
                            if (name.contains("release", ignoreCase = true) || name.contains("waqt", ignoreCase = true) || name.contains("adhan", ignoreCase = true)) {
                                break
                            }
                        }
                    }
                }

                val isNewer = isNewerVersion(currentVersion, tagName)

                return@withContext Result.success(
                    AppUpdateInfo(
                        isUpdateAvailable = isNewer,
                        currentVersion = currentVersion,
                        latestVersion = tagName.removePrefix("v").removePrefix("V"),
                        releaseTitle = releaseTitle,
                        releaseNotes = releaseNotes,
                        downloadUrl = apkDownloadUrl,
                        apkFileName = apkFileName,
                        releasePageUrl = htmlUrl,
                        publishedAt = publishedAt
                    )
                )
            } catch (e: Exception) {
                lastException = e
            }
        }

        if (lastException != null) {
            return@withContext Result.failure(lastException)
        }

        // None of the candidate repositories had a public release yet
        Result.success(
            AppUpdateInfo(
                isUpdateAvailable = false,
                currentVersion = currentVersion,
                latestVersion = currentVersion,
                releaseTitle = "No Releases",
                releaseNotes = "No public releases found on GitHub ($repoOwner/Waqt-Azan-time).",
                downloadUrl = null,
                apkFileName = null,
                releasePageUrl = "https://github.com/$repoOwner/Waqt-Azan-time/releases"
            )
        )
    }

    suspend fun downloadApk(
        downloadUrl: String,
        destinationFile: File,
        onProgress: (Int) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            var currentUrl = downloadUrl
            var connection: HttpURLConnection
            var redirects = 0

            // Handle GitHub redirect chain (api/releases -> github-releases.s3)
            while (true) {
                val url = URL(currentUrl)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15000
                    readTimeout = 20000
                    instanceFollowRedirects = false
                    setRequestProperty("User-Agent", "Waqt-App")
                }

                val status = connection.responseCode
                if (status == HttpURLConnection.HTTP_MOVED_TEMP ||
                    status == HttpURLConnection.HTTP_MOVED_PERM ||
                    status == 307 || status == 308
                ) {
                    val newUrl = connection.getHeaderField("Location")
                    connection.disconnect()
                    currentUrl = newUrl
                    redirects++
                    if (redirects > 5) throw Exception("Too many redirects")
                } else if (status == HttpURLConnection.HTTP_OK) {
                    break
                } else {
                    throw Exception("Download server returned $status")
                }
            }

            destinationFile.parentFile?.mkdirs()
            if (destinationFile.exists()) {
                destinationFile.delete()
            }

            val fileLength = connection.contentLength
            val input: InputStream = connection.inputStream
            val output = FileOutputStream(destinationFile)

            val data = ByteArray(8192)
            var total: Long = 0
            var count: Int
            val estimatedBytes = if (fileLength > 0) fileLength else 21 * 1024 * 1024

            while (input.read(data).also { count = it } != -1) {
                total += count
                val progress = ((total * 100) / estimatedBytes).toInt()
                onProgress(progress.coerceIn(1, 99))
                output.write(data, 0, count)
            }

            output.flush()
            output.close()
            input.close()
            connection.disconnect()
            onProgress(100)

            Result.success(destinationFile)
        } catch (e: Exception) {
            destinationFile.delete()
            Result.failure(e)
        }
    }

    fun canRequestPackageInstalls(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    fun openInstallPermissionSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    fun installApk(context: Context, apkFile: File) {
        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Grant explicit URI read permission to all matching package installer handlers
        val resolveInfoList = context.packageManager.queryIntentActivities(
            intent,
            android.content.pm.PackageManager.MATCH_DEFAULT_ONLY
        )
        for (resolveInfo in resolveInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            context.grantUriPermission(packageName, apkUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(intent)
    }

    companion object {
        fun isNewerVersion(currentVersion: String, remoteTag: String): Boolean {
            val cleanCurrent = currentVersion.trim().removePrefix("v").removePrefix("V")
            val cleanRemote = remoteTag.trim().removePrefix("v").removePrefix("V")

            if (cleanCurrent.isEmpty() || cleanRemote.isEmpty()) return false

            val currentParts = cleanCurrent.split(".").mapNotNull { it.toIntOrNull() }
            val remoteParts = cleanRemote.split(".").mapNotNull { it.toIntOrNull() }

            val maxLen = maxOf(currentParts.size, remoteParts.size)
            for (i in 0 until maxLen) {
                val current = currentParts.getOrElse(i) { 0 }
                val remote = remoteParts.getOrElse(i) { 0 }
                if (remote > current) return true
                if (remote < current) return false
            }
            return false
        }
    }
}
