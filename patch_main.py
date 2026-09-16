with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    lines = f.readlines()

new_imports = [
    "import androidx.compose.foundation.layout.padding\n",
    "import androidx.compose.material3.Scaffold\n",
    "import androidx.compose.material3.SnackbarDuration\n",
    "import androidx.compose.material3.SnackbarHost\n",
    "import androidx.compose.material3.SnackbarHostState\n",
    "import androidx.compose.material3.SnackbarResult\n",
    "import androidx.compose.runtime.LaunchedEffect\n",
    "import androidx.compose.runtime.rememberCoroutineScope\n",
    "import androidx.compose.ui.platform.LocalContext\n",
    "import kotlinx.coroutines.Dispatchers\n",
    "import kotlinx.coroutines.launch\n",
    "import kotlinx.coroutines.withContext\n",
    "import org.json.JSONObject\n",
    "import java.net.URL\n",
    "import com.example.update.AppUpdateManager\n",
    "import com.example.BuildConfig\n"
]

lines = lines[:18] + new_imports + lines[18:]

index_of_kaachuphoolapp = next(i for i, line in enumerate(lines) if "fun KaachuPhoolApp(" in line)

# Find the start of AnimatedContent
index_of_animatedcontent = next(i for i, line in enumerate(lines[index_of_kaachuphoolapp:]) if "AnimatedContent(" in line) + index_of_kaachuphoolapp

new_code = """
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val appUpdateManager = remember { AppUpdateManager.getInstance(context) }
    
    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            try {
                val response = URL("https://api.github.com/repos/patelhet0507/kacchooisdaphoolisda/releases/latest").readText()
                val json = JSONObject(response)
                val tagName = json.optString("tag_name", "")
                val tagCode = tagName.filter { it.isDigit() }.toIntOrNull() ?: 0
                val currentCode = BuildConfig.VERSION_CODE
                
                if (tagCode > currentCode) {
                    withContext(Dispatchers.Main) {
                        val result = snackbarHostState.showSnackbar(
                            message = "New update available ($tagName)",
                            actionLabel = "Update",
                            duration = SnackbarDuration.Indefinite
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            val releaseInfo = com.example.update.GithubReleaseInfo(
                                tagName = tagName,
                                releaseTitle = json.optString("name", tagName),
                                releaseNotes = json.optString("body", ""),
                                publishedAt = json.optString("published_at", ""),
                                apkDownloadUrl = json.optJSONArray("assets")?.optJSONObject(0)?.optString("browser_download_url"),
                                apkFileName = json.optJSONArray("assets")?.optJSONObject(0)?.optString("name"),
                                apkSizeBytes = json.optJSONArray("assets")?.optJSONObject(0)?.optLong("size") ?: 0L
                            )
                            appUpdateManager.startDownload(releaseInfo)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
"""

lines.insert(index_of_animatedcontent, new_code)

# Add padding modifier to AnimatedContent
for i in range(index_of_animatedcontent + 1, len(lines)):
    if "label = \"screen_transition\"" in lines[i]:
        lines[i] = lines[i].replace("label = \"screen_transition\"", "label = \"screen_transition\",\n        modifier = Modifier.padding(paddingValues)")
        break

# Add closing brace for Scaffold
lines.append("    }\n")

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.writelines(lines)

