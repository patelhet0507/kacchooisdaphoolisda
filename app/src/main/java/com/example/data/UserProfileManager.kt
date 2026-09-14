package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.auth.AuthManager
import com.example.auth.AuthUserState
import com.example.model.Achievement
import com.example.model.AchievementData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UserProfileState(
    val isLoggedIn: Boolean = false,
    val googleEmail: String = "",
    val googleName: String = "Player 1",
    val googlePhotoUrl: String = "",
    val gamesPlayed: Int = 0,
    val winsCount: Int = 0,
    val highestScore: Int = 0,
    val selectedAvatar: String = "lion",
    val selectedTableTheme: String = "emerald",
    val friends: List<String> = emptyList(),
    val unlockedAchievementIds: Set<String> = emptySet(),
    val achievements: List<Achievement> = emptyList()
)

class UserProfileManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("kaachu_phool_user_profile", Context.MODE_PRIVATE)
    private val authManager = AuthManager.getInstance()
    private val scope = CoroutineScope(Dispatchers.Main)

    private val savedFriends = prefs.getStringSet("friends_list", setOf("Alex (Pro)", "Sam (Master)", "Jordan"))?.toList() ?: listOf("Alex (Pro)", "Sam (Master)", "Jordan")
    private val savedUnlockedAchievements = prefs.getStringSet("unlocked_achievements", emptySet()) ?: emptySet()

    private val _state = MutableStateFlow(
        buildInitialState()
    )
    val state: StateFlow<UserProfileState> = _state.asStateFlow()

    private fun buildInitialState(): UserProfileState {
        val loggedIn = prefs.getBoolean("is_logged_in", false)
        val email = prefs.getString("google_email", "") ?: ""
        val name = prefs.getString("google_name", "Player 1") ?: "Player 1"
        val photo = prefs.getString("google_photo", "") ?: ""
        val games = prefs.getInt("games_played", 0)
        val wins = prefs.getInt("wins_count", 0)
        val highest = prefs.getInt("highest_score", 0)
        val avatar = prefs.getString("selected_avatar", "lion") ?: "lion"
        val theme = prefs.getString("selected_theme", "emerald") ?: "emerald"
        val unlockedIds = prefs.getStringSet("unlocked_achievements", emptySet()) ?: emptySet()

        val achievementList = computeAchievements(unlockedIds, games, wins, highest, savedFriends.size)

        return UserProfileState(
            isLoggedIn = loggedIn,
            googleEmail = email,
            googleName = name,
            googlePhotoUrl = photo,
            gamesPlayed = games,
            winsCount = wins,
            highestScore = highest,
            selectedAvatar = avatar,
            selectedTableTheme = theme,
            friends = savedFriends,
            unlockedAchievementIds = unlockedIds,
            achievements = achievementList
        )
    }

    private fun computeAchievements(
        unlockedIds: Set<String>,
        games: Int,
        wins: Int,
        highestScore: Int,
        friendsCount: Int
    ): List<Achievement> {
        return AchievementData.ALL_ACHIEVEMENTS.map { template ->
            val isUnlocked = unlockedIds.contains(template.id)
            val currentProgress = when (template.id) {
                "first_win" -> if (wins >= 1 || isUnlocked) 1 else 0
                "wins_5" -> wins.coerceAtMost(5)
                "wins_10" -> wins.coerceAtMost(10)
                "card_veteran" -> games.coerceAtMost(5)
                "socialite" -> if (friendsCount >= 1 || isUnlocked) 1 else 0
                "high_scorer" -> if (highestScore >= 50 || isUnlocked) 1 else 0
                else -> if (isUnlocked) template.maxProgress else 0
            }
            template.copy(
                isUnlocked = isUnlocked || currentProgress >= template.maxProgress,
                progress = currentProgress
            )
        }
    }

    init {
        scope.launch {
            authManager.authState.collect { authUser ->
                if (authUser.isLoggedIn) {
                    val name = authUser.displayName?.ifBlank { "Player" } ?: "Player"
                    val email = authUser.email ?: ""
                    val photo = authUser.photoUrl ?: ""
                    prefs.edit()
                        .putBoolean("is_logged_in", true)
                        .putString("google_email", email)
                        .putString("google_name", name)
                        .putString("google_photo", photo)
                        .apply()
                    _state.value = _state.value.copy(
                        isLoggedIn = true,
                        googleEmail = email,
                        googleName = name,
                        googlePhotoUrl = photo
                    )
                } else if (!prefs.getBoolean("local_offline_auth", false)) {
                    // Logged out
                    _state.value = _state.value.copy(
                        isLoggedIn = false,
                        googleEmail = "",
                        googleName = prefs.getString("local_custom_name", "Player 1") ?: "Player 1"
                    )
                }
            }
        }
    }

    fun loginWithGoogle(email: String, name: String, photoUrl: String = "") {
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putBoolean("local_offline_auth", true)
            .putString("google_email", email)
            .putString("google_name", name)
            .putString("google_photo", photoUrl)
            .apply()

        _state.value = _state.value.copy(
            isLoggedIn = true,
            googleEmail = email,
            googleName = name,
            googlePhotoUrl = photoUrl
        )
    }

    fun updateName(name: String) {
        if (name.isBlank()) return
        prefs.edit()
            .putString("local_custom_name", name)
            .putString("google_name", name)
            .apply()
        _state.value = _state.value.copy(googleName = name)
    }

    fun logout() {
        scope.launch {
            authManager.signOut(context)
        }
        prefs.edit()
            .putBoolean("is_logged_in", false)
            .putBoolean("local_offline_auth", false)
            .putString("google_email", "")
            .putString("google_name", "Player 1")
            .putString("google_photo", "")
            .apply()

        _state.value = _state.value.copy(
            isLoggedIn = false,
            googleEmail = "",
            googleName = "Player 1",
            googlePhotoUrl = ""
        )
    }

    suspend fun deleteAccountAndWipeData() {
        authManager.deleteAccount(context)
        prefs.edit().clear().apply()
        _state.value = UserProfileState(
            isLoggedIn = false,
            googleEmail = "",
            googleName = "Player 1",
            googlePhotoUrl = "",
            gamesPlayed = 0,
            selectedAvatar = "lion",
            selectedTableTheme = "emerald"
        )
    }

    fun incrementGamesPlayed() {
        val newCount = _state.value.gamesPlayed + 1
        val currentUnlockedIds = _state.value.unlockedAchievementIds.toMutableSet()
        if (newCount >= 5 && !currentUnlockedIds.contains("card_veteran")) {
            currentUnlockedIds.add("card_veteran")
        }
        prefs.edit()
            .putInt("games_played", newCount)
            .putStringSet("unlocked_achievements", currentUnlockedIds)
            .apply()

        val updatedAchievements = computeAchievements(currentUnlockedIds, newCount, _state.value.winsCount, _state.value.highestScore, _state.value.friends.size)
        _state.value = _state.value.copy(
            gamesPlayed = newCount,
            unlockedAchievementIds = currentUnlockedIds,
            achievements = updatedAchievements
        )
    }

    fun recordGameFinished(
        userWon: Boolean,
        userTotalScore: Int,
        perfectBids: Boolean,
        difficultyOrMultiplayer: String = "MEDIUM"
    ): List<Achievement> {
        val currentGames = _state.value.gamesPlayed + 1
        val currentWins = if (userWon) _state.value.winsCount + 1 else _state.value.winsCount
        val highest = maxOf(_state.value.highestScore, userTotalScore)

        val newlyUnlocked = mutableListOf<Achievement>()
        val currentUnlockedIds = _state.value.unlockedAchievementIds.toMutableSet()

        fun unlock(id: String) {
            if (!currentUnlockedIds.contains(id)) {
                currentUnlockedIds.add(id)
                AchievementData.ALL_ACHIEVEMENTS.find { it.id == id }?.let {
                    newlyUnlocked.add(it.copy(isUnlocked = true))
                }
            }
        }

        if (userWon) unlock("first_win")
        if (currentWins >= 5) unlock("wins_5")
        if (currentWins >= 10) unlock("wins_10")
        if (userWon && perfectBids) unlock("perfect_game")
        if (userTotalScore >= 50) unlock("high_scorer")
        if (currentGames >= 5) unlock("card_veteran")
        if (userWon && (difficultyOrMultiplayer == "HARD" || difficultyOrMultiplayer == "MULTIPLAYER")) unlock("hook_master")
        if (_state.value.friends.isNotEmpty()) unlock("socialite")

        prefs.edit()
            .putInt("games_played", currentGames)
            .putInt("wins_count", currentWins)
            .putInt("highest_score", highest)
            .putStringSet("unlocked_achievements", currentUnlockedIds)
            .apply()

        val updatedAchievements = computeAchievements(currentUnlockedIds, currentGames, currentWins, highest, _state.value.friends.size)

        _state.value = _state.value.copy(
            gamesPlayed = currentGames,
            winsCount = currentWins,
            highestScore = highest,
            unlockedAchievementIds = currentUnlockedIds,
            achievements = updatedAchievements
        )

        return newlyUnlocked
    }

    fun setSelectedAvatar(avatarId: String) {
        prefs.edit().putString("selected_avatar", avatarId).apply()
        _state.value = _state.value.copy(selectedAvatar = avatarId)
    }

    fun setSelectedTableTheme(themeId: String) {
        prefs.edit().putString("selected_theme", themeId).apply()
        _state.value = _state.value.copy(selectedTableTheme = themeId)
    }

    fun addFriend(friendName: String) {
        if (friendName.isBlank()) return
        val current = _state.value.friends.toMutableList()
        if (!current.contains(friendName.trim())) {
            current.add(friendName.trim())
            val currentUnlockedIds = _state.value.unlockedAchievementIds.toMutableSet()
            if (!currentUnlockedIds.contains("socialite")) {
                currentUnlockedIds.add("socialite")
            }
            prefs.edit()
                .putStringSet("friends_list", current.toSet())
                .putStringSet("unlocked_achievements", currentUnlockedIds)
                .apply()

            val updatedAchievements = computeAchievements(currentUnlockedIds, _state.value.gamesPlayed, _state.value.winsCount, _state.value.highestScore, current.size)
            _state.value = _state.value.copy(
                friends = current,
                unlockedAchievementIds = currentUnlockedIds,
                achievements = updatedAchievements
            )
        }
    }

    fun removeFriend(friendName: String) {
        val current = _state.value.friends.toMutableList()
        if (current.remove(friendName)) {
            prefs.edit().putStringSet("friends_list", current.toSet()).apply()
            _state.value = _state.value.copy(friends = current)
        }
    }
}
