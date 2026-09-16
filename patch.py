with open('app/src/main/java/com/example/update/AppUpdateManager.kt', 'r') as f:
    lines = f.readlines()

new_lines = lines[:187] + [
    "    fun startDownload(release: GithubReleaseInfo) {\n",
    "        val timestamp = System.currentTimeMillis() / 1000\n",
    "        val fileName = \"KaachuPhool_${release.tagName.replace(\".\", \"_\")}_$timestamp.apk\"\n",
    "\n",
    "        try {\n",
    "            _downloadStatus.value = DownloadStatus.Downloading(0, 0.0)\n",
    "            \n",
    "            val request = DownloadManager.Request(Uri.parse(release.apkDownloadUrl)).apply {\n",
    "                setTitle(\"Downloading Kaachu Phool Update\")\n",
    "                setDescription(\"Downloading version ${release.tagName}\")\n",
    "                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)\n",
    "                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)\n",
    "                setAllowedOverMetered(true)\n",
    "                setAllowedOverRoaming(true)\n",
    "            }\n",
    "            \n",
    "            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager\n",
    "            val downloadId = downloadManager.enqueue(request)\n",
    "            \n",
    "            registerDownloadReceiver(downloadId, fileName)\n",
    "            startProgressPolling(downloadId)\n",
    "        } catch (e: Exception) {\n",
    "            Log.e(\"AppUpdateManager\", \"Download initiation error\", e)\n",
    "            _downloadStatus.value = DownloadStatus.Failed(e.localizedMessage ?: \"Download failed\")\n",
    "        }\n",
    "    }\n"
] + lines[235:]

with open('app/src/main/java/com/example/update/AppUpdateManager.kt', 'w') as f:
    f.writelines(new_lines)
