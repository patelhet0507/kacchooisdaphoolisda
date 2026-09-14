package com.example.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

data class GithubReleaseInfo(
    val tagName: String,
    val releaseTitle: String,
    val releaseNotes: String,
    val publishedAt: String,
    val apkDownloadUrl: String?,
    val apkFileName: String?,
    val apkSizeBytes: Long
)

sealed interface UpdateCheckState {
    data object Idle : UpdateCheckState
    data object Checking : UpdateCheckState
    data class UpdateAvailable(val release: GithubReleaseInfo) : UpdateCheckState
    data class UpToDate(val currentVersion: String) : UpdateCheckState
    data class Error(val message: String) : UpdateCheckState
}

sealed interface DownloadStatus {
    data object Idle : DownloadStatus
    data class Downloading(val progressPercent: Int) : DownloadStatus
    data class ReadyToInstall(val apkUri: Uri, val file: File) : DownloadStatus
    data class Failed(val reason: String) : DownloadStatus
}

class AppUpdateManager private constructor(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val _updateState = MutableStateFlow<UpdateCheckState>(UpdateCheckState.Idle)
    val updateState: StateFlow<UpdateCheckState> = _updateState.asStateFlow()

    private val _downloadStatus = MutableStateFlow<DownloadStatus>(DownloadStatus.Idle)
    val downloadStatus: StateFlow<DownloadStatus> = _downloadStatus.asStateFlow()

    private var activeDownloadId: Long = -1L
    private var downloadCompleteReceiver: BroadcastReceiver? = null

    val currentVersionName: String
        get() = BuildConfig.VERSION_NAME

    val currentVersionCode: Int
        get() = BuildConfig.VERSION_CODE

    /**
     * Checks the GitHub repository releases API for newer APK builds.
     */
    suspend fun checkForUpdates(repoOwnerAndName: String = "patelhet0507/kacchooisdaphoolisda"): UpdateCheckState {
        _updateState.value = UpdateCheckState.Checking
        return withContext(Dispatchers.IO) {
            try {
                val cleanRepo = repoOwnerAndName.trim()
                    .removePrefix("https://github.com/")
                    .removeSuffix("/")

                val apiUrl = "https://api.github.com/repos/$cleanRepo/releases/latest"
                val request = Request.Builder()
                    .url(apiUrl)
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "KaachuPhool-Android-App")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        val msg = if (response.code == 404) {
                            "No releases found for '$cleanRepo'. Note: The repository must be PUBLIC for the app to detect updates."
                        } else {
                            "GitHub API HTTP ${response.code}: ${response.message}"
                        }
                        val errorState = UpdateCheckState.Error(msg)
                        _updateState.value = errorState
                        return@withContext errorState
                    }

                    val body = response.body?.string() ?: throw IllegalStateException("Empty response from GitHub")
                    val json = JSONObject(body)

                    val tagName = json.optString("tag_name", "")
                    val releaseTitle = json.optString("name", tagName)
                    val releaseNotes = json.optString("body", "Bug fixes and improvements.")
                    val publishedAt = json.optString("published_at", "")

                    // Find attached APK asset
                    var apkUrl: String? = null
                    var apkName: String? = null
                    var apkSize: Long = 0L

                    val assets = json.optJSONArray("assets")
                    if (assets != null) {
                        for (i in 0 until assets.length()) {
                            val asset = assets.getJSONObject(i)
                            val name = asset.optString("name", "")
                            if (name.endsWith(".apk", ignoreCase = true)) {
                                apkUrl = asset.optString("browser_download_url")
                                apkName = name
                                apkSize = asset.optLong("size", 0L)
                                break
                            }
                        }
                    }

                    val releaseInfo = GithubReleaseInfo(
                        tagName = tagName,
                        releaseTitle = releaseTitle,
                        releaseNotes = releaseNotes,
                        publishedAt = publishedAt,
                        apkDownloadUrl = apkUrl,
                        apkFileName = apkName,
                        apkSizeBytes = apkSize
                    )

                    val isNewer = isVersionNewer(tagName, currentVersionName)

                    val resultState = if (isNewer) {
                        UpdateCheckState.UpdateAvailable(releaseInfo)
                    } else {
                        UpdateCheckState.UpToDate(currentVersionName)
                    }

                    _updateState.value = resultState
                    resultState
                }
            } catch (e: Exception) {
                Log.e("AppUpdateManager", "Check update failed", e)
                val errorState = UpdateCheckState.Error(e.localizedMessage ?: "Failed to check for updates")
                _updateState.value = errorState
                errorState
            }
        }
    }

    /**
     * Downloads the APK file using Android's system DownloadManager.
     */
    fun startDownload(release: GithubReleaseInfo) {
        val downloadUrl = release.apkDownloadUrl ?: return
        val fileName = release.apkFileName ?: "KaachuPhool-update.apk"

        try {
            _downloadStatus.value = DownloadStatus.Downloading(0)

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = Uri.parse(downloadUrl)

            val request = DownloadManager.Request(uri).apply {
                setTitle("Downloading Kaachu Phool ${release.tagName}")
                setDescription("Downloading latest game update APK...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
            }

            activeDownloadId = downloadManager.enqueue(request)

            // Register broadcast receiver to know when download completes
            registerDownloadReceiver(activeDownloadId, fileName)

        } catch (e: Exception) {
            Log.e("AppUpdateManager", "Download initiation error", e)
            _downloadStatus.value = DownloadStatus.Failed(e.localizedMessage ?: "Download failed")
        }
    }

    private fun registerDownloadReceiver(downloadId: Long, fileName: String) {
        unregisterReceiverSafe()

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(recvContext: Context?, intent: Intent?) {
                if (intent?.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
                    val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
                    if (id == downloadId) {
                        handleDownloadFinished(id, fileName)
                    }
                }
            }
        }
        downloadCompleteReceiver = receiver

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(
                receiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                Context.RECEIVER_EXPORTED
            )
        } else {
            context.registerReceiver(
                receiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )
        }
    }

    private fun handleDownloadFinished(downloadId: Long, fileName: String) {
        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)

            if (cursor != null && cursor.moveToFirst()) {
                val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                val status = if (statusIndex != -1) cursor.getInt(statusIndex) else -1

                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    val localUriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                    val localUriString = if (localUriIndex != -1) cursor.getString(localUriIndex) else null

                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val apkFile = File(downloadsDir, fileName)

                    if (apkFile.exists()) {
                        val contentUri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            apkFile
                        )
                        _downloadStatus.value = DownloadStatus.ReadyToInstall(contentUri, apkFile)
                    } else if (localUriString != null) {
                        _downloadStatus.value = DownloadStatus.ReadyToInstall(Uri.parse(localUriString), apkFile)
                    } else {
                        _downloadStatus.value = DownloadStatus.Failed("Downloaded file could not be located.")
                    }
                } else {
                    _downloadStatus.value = DownloadStatus.Failed("Download failed with status $status")
                }
                cursor.close()
            }
        } catch (e: Exception) {
            Log.e("AppUpdateManager", "Error handling download completion", e)
            _downloadStatus.value = DownloadStatus.Failed(e.localizedMessage ?: "File handling error")
        } finally {
            unregisterReceiverSafe()
        }
    }

    /**
     * Prompts the Android Package Installer to install the downloaded APK.
     */
    fun installApk(uri: Uri, apkFile: File? = null): Boolean {
        return try {
            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(installIntent)
            true
        } catch (e: Exception) {
            Log.e("AppUpdateManager", "Error launching APK install intent", e)
            false
        }
    }

    /**
     * Opens the GitHub releases page directly in the user's browser.
     */
    fun openReleasesPageInBrowser(repo: String = "patelhet0507/kacchooisdaphoolisda") {
        try {
            val cleanRepo = repo.trim().removePrefix("https://github.com/").removeSuffix("/")
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/$cleanRepo/releases"))
            browserIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(browserIntent)
        } catch (e: Exception) {
            Log.e("AppUpdateManager", "Failed to open browser", e)
        }
    }

    fun resetState() {
        _updateState.value = UpdateCheckState.Idle
        _downloadStatus.value = DownloadStatus.Idle
    }

    private fun unregisterReceiverSafe() {
        downloadCompleteReceiver?.let {
            try {
                context.unregisterReceiver(it)
            } catch (_: Exception) {
            }
            downloadCompleteReceiver = null
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AppUpdateManager? = null

        fun getInstance(context: Context): AppUpdateManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppUpdateManager(context.applicationContext).also { INSTANCE = it }
            }
        }

        /**
         * Checks if the tag version is newer than current app version.
         */
        fun isVersionNewer(tagVersion: String, currentVersion: String): Boolean {
            if (tagVersion.isBlank()) return false
            val cleanTag = tagVersion.trim().removePrefix("v").removePrefix("V")
            val cleanCur = currentVersion.trim().removePrefix("v").removePrefix("V")

            // Always consider 'latest' tag as update available for dev builds
            if (tagVersion.equals("latest", ignoreCase = true)) {
                return true
            }

            val tagParts = cleanTag.split(".").mapNotNull { it.toIntOrNull() }
            val curParts = cleanCur.split(".").mapNotNull { it.toIntOrNull() }

            val maxLen = maxOf(tagParts.size, curParts.size)
            for (i in 0 until maxLen) {
                val tagNum = tagParts.getOrElse(i) { 0 }
                val curNum = curParts.getOrElse(i) { 0 }
                if (tagNum > curNum) return true
                if (tagNum < curNum) return false
            }

            return false
        }
    }
}
