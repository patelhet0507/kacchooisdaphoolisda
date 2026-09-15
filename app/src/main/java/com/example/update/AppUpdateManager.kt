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
import kotlinx.coroutines.*
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
    data class Downloading(val progressPercent: Int, val speedKbps: Double = 0.0) : DownloadStatus
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
    private var progressPollingJob: Job? = null

    val currentVersionName: String
        get() = BuildConfig.VERSION_NAME

    val currentVersionCode: Int
        get() = BuildConfig.VERSION_CODE

    /**
     * Checks the GitHub repository releases API for newer APK builds.
     * Implements a 24-hour cache for successful checks to avoid rate limiting.
     */
    suspend fun checkForUpdates(
        repoOwnerAndName: String = "patelhet0507/kacchooisdaphoolisda",
        force: Boolean = false
    ): UpdateCheckState {
        val prefs = context.getSharedPreferences("app_update_prefs", Context.MODE_PRIVATE)
        val lastCheckTime = prefs.getLong("last_check_time", 0L)
        val currentTime = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L

        if (!force && (currentTime - lastCheckTime < oneDayMillis)) {
            Log.d("AppUpdateManager", "Skipping update check: last check was less than 24h ago.")
            return UpdateCheckState.Idle
        }

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
                        } else if (response.code == 403) {
                            "GitHub API Rate Limit Exceeded (HTTP 403). Please try again in an hour or check manually at the repository URL."
                        } else {
                            "GitHub API HTTP ${response.code}: ${response.message}"
                        }
                        
                        // We still update last check time even on 403 to back off
                        if (response.code == 403) {
                            prefs.edit().putLong("last_check_time", currentTime).apply()
                        }
                        
                        val errorState = UpdateCheckState.Error(msg)
                        _updateState.value = errorState
                        return@withContext errorState
                    }

                    // Successful check, update timestamp
                    prefs.edit().putLong("last_check_time", currentTime).apply()

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
        // Use a unique filename with timestamp to avoid parsing errors from corrupted/cached partial downloads
        val timestamp = System.currentTimeMillis() / 1000
        val fileName = "KaachuPhool_${release.tagName.replace(".", "_")}_$timestamp.apk"

        try {
            _downloadStatus.value = DownloadStatus.Downloading(0, 0.0)

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = Uri.parse(downloadUrl)

            val request = DownloadManager.Request(uri).apply {
                setTitle("Downloading Kaachu Phool ${release.tagName}")
                setDescription("Fetching latest game update...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                
                // Be more aggressive to start the download immediately
                setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    setRequiresCharging(false)
                    setRequiresDeviceIdle(false)
                }
            }

            activeDownloadId = downloadManager.enqueue(request)
            
            // Start polling for progress
            startProgressPolling(activeDownloadId)

            // Register broadcast receiver to know when download completes
            registerDownloadReceiver(activeDownloadId, fileName)

        } catch (e: Exception) {
            Log.e("AppUpdateManager", "Download initiation error", e)
            _downloadStatus.value = DownloadStatus.Failed(e.localizedMessage ?: "Download failed")
        }
    }

    private fun startProgressPolling(downloadId: Long) {
        progressPollingJob?.cancel()
        progressPollingJob = CoroutineScope(Dispatchers.IO).launch {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            var isFinished = false
            var lastDownloaded = 0L
            var lastTime = System.currentTimeMillis()
            
            while (isActive && !isFinished) {
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = downloadManager.query(query)
                
                if (cursor != null && cursor.moveToFirst()) {
                    val bytesDownloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    val totalBytesIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                    val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    
                    if (bytesDownloadedIndex != -1 && totalBytesIndex != -1) {
                        val downloaded = cursor.getLong(bytesDownloadedIndex)
                        val total = cursor.getLong(totalBytesIndex)
                        
                        val currentTime = System.currentTimeMillis()
                        val timeDeltaSec = (currentTime - lastTime) / 1000.0
                        
                        val speed = if (timeDeltaSec > 0) {
                            val bytesDelta = downloaded - lastDownloaded
                            (bytesDelta / 1024.0) / timeDeltaSec
                        } else 0.0
                        
                        lastDownloaded = downloaded
                        lastTime = currentTime
                        
                        if (total > 0) {
                            val progress = ((downloaded * 100) / total).toInt()
                            _downloadStatus.value = DownloadStatus.Downloading(
                                progressPercent = progress.coerceIn(0, 100),
                                speedKbps = speed
                            )
                        }
                    }
                    
                    val status = if (statusIndex != -1) cursor.getInt(statusIndex) else -1
                    if (status == DownloadManager.STATUS_SUCCESSFUL || status == DownloadManager.STATUS_FAILED) {
                        isFinished = true
                    }
                    cursor.close()
                } else {
                    isFinished = true
                }
                
                if (!isFinished) {
                    delay(800) // Poll every 800ms
                }
            }
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
        progressPollingJob?.cancel()
        progressPollingJob = null
        
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

            // Handle 'latest' tag by treating it as a generic update if not in a dev environment,
            // but for production builds, we rely on semantic version comparison.
            if (tagVersion.equals("latest", ignoreCase = true)) {
                return false // Don't trigger 'latest' as newer by default
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
