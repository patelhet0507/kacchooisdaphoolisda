package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import com.example.update.AppUpdateManager
import com.example.BuildConfig
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.GameMode
import com.example.model.ScoringRule
import com.example.ui.screens.GamePlayScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.RulesGuideScreen
import com.example.ui.screens.ScorecardScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.screens.MultiplayerLobbyScreen
import com.example.viewmodel.MultiplayerViewModel
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.ScorecardViewModel

import com.example.ui.screens.LoadingScreen

enum class AppScreen {
    LOADING,
    HOME,
    MULTIPLAYER_LOBBY,
    GAME_PLAY,
    SCORECARD,
    RULES
}

class MainActivity : ComponentActivity() {
    
    // Track loading state at the activity level so it resets on fresh launch
    private var hasCompletedInitialLoading = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        try {
            com.example.update.DailyNotificationManager.scheduleDailyReminder(applicationContext)
        } catch (_: Exception) {}
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    KaachuPhoolApp(
                        initialScreen = if (hasCompletedInitialLoading) AppScreen.HOME else AppScreen.LOADING,
                        onLoadingComplete = { hasCompletedInitialLoading = true }
                    )
                }
            }
        }
    }
}

@Composable
fun KaachuPhoolApp(
    initialScreen: AppScreen = AppScreen.LOADING,
    onLoadingComplete: () -> Unit = {}
) {
    var currentScreen by remember(initialScreen) { mutableStateOf(initialScreen) }
    val gameViewModel: GameViewModel = viewModel()
    val scorecardViewModel: ScorecardViewModel = viewModel()
    val multiplayerViewModel: MultiplayerViewModel = viewModel()


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
    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "screen_transition",
        modifier = Modifier.padding(paddingValues)
    ) { screen ->
        when (screen) {
            AppScreen.LOADING -> {
                LoadingScreen(
                    onLoadingComplete = {
                        onLoadingComplete()
                        currentScreen = AppScreen.HOME
                    }
                )
            }
            AppScreen.HOME -> {
                HomeScreen(
                    onStartGame = { userName, mode, scoringRule, difficulty ->
                        gameViewModel.startNewGame(
                            gameMode = mode,
                            scoringRule = scoringRule,
                            userName = userName,
                            botCount = 3,
                            botDifficulty = difficulty
                        )
                        currentScreen = AppScreen.GAME_PLAY
                    },
                    onOpenMultiplayerLobby = {
                        currentScreen = AppScreen.MULTIPLAYER_LOBBY
                    },
                    onOpenScorecard = {
                        currentScreen = AppScreen.SCORECARD
                    },
                    onOpenRules = {
                        currentScreen = AppScreen.RULES
                    },
                    multiplayerViewModel = multiplayerViewModel
                )
            }

            AppScreen.MULTIPLAYER_LOBBY -> {
                MultiplayerLobbyScreen(
                    viewModel = multiplayerViewModel,
                    onStartGame = { userName, mode, scoringRule, playerCount ->
                        val code = multiplayerViewModel.roomCode.value ?: ""
                        if (code.isNotEmpty()) {
                            val isHost = multiplayerViewModel.currentRoom.value?.hostName == userName ||
                                    (multiplayerViewModel.currentRoom.value?.players?.firstOrNull() == userName)
                            gameViewModel.startMultiplayerGame(
                                roomId = code,
                                localPlayerName = userName,
                                gameMode = mode,
                                scoringRule = scoringRule,
                                isHost = isHost
                            )
                        } else {
                            val botCount = (4 - playerCount).coerceAtLeast(0)
                            gameViewModel.startNewGame(
                                gameMode = mode,
                                scoringRule = scoringRule,
                                userName = userName,
                                botCount = botCount
                            )
                        }
                        currentScreen = AppScreen.GAME_PLAY
                    },
                    onBackClick = {
                        multiplayerViewModel.leaveRoom()
                        currentScreen = AppScreen.HOME
                    }
                )
            }

            AppScreen.GAME_PLAY -> {
                GamePlayScreen(
                    viewModel = gameViewModel,
                    onBackClick = {
                        gameViewModel.exitGame()
                        multiplayerViewModel.leaveRoom()
                        currentScreen = AppScreen.HOME
                    }
                )
            }

            AppScreen.SCORECARD -> {
                ScorecardScreen(
                    viewModel = scorecardViewModel,
                    onBackClick = {
                        currentScreen = AppScreen.HOME
                    }
                )
            }

            AppScreen.RULES -> {
                RulesGuideScreen(
                    onBackClick = {
                        currentScreen = AppScreen.HOME
                    }
                )
            }
        }
    }
}
    }
