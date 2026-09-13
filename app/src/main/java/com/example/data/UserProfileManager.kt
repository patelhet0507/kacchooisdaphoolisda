package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserProfileState(
    val isLoggedIn: Boolean = false,
    val googleEmail: String = "",
    val googleName: String = "Player 1",
    val googlePhotoUrl: String = "",
    val gamesPlayed: Int = 0,
    val selectedAvatar: String = "lion",
    val selectedTableTheme: String = "emerald"
)

class UserProfileManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("kaachu_phool_user_profile", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(
        UserProfileState(
            isLoggedIn = prefs.getBoolean("is_logged_in", false),
            googleEmail = prefs.getString("google_email", "") ?: "",
            googleName = prefs.getString("google_name", "Player 1") ?: "Player 1",
            googlePhotoUrl = prefs.getString("google_photo", "") ?: "",
            gamesPlayed = prefs.getInt("games_played", 0),
            selectedAvatar = prefs.getString("selected_avatar", "lion") ?: "lion",
            selectedTableTheme = prefs.getString("selected_theme", "emerald") ?: "emerald"
        )
    )
    val state: StateFlow<UserProfileState> = _state.asStateFlow()

    fun loginWithGoogle(email: String, name: String, photoUrl: String = "") {
        prefs.edit()
            .putBoolean("is_logged_in", true)
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

    fun logout() {
        prefs.edit()
            .putBoolean("is_logged_in", false)
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

    fun incrementGamesPlayed() {
        val newCount = _state.value.gamesPlayed + 1
        prefs.edit().putInt("games_played", newCount).apply()
        _state.value = _state.value.copy(gamesPlayed = newCount)
    }

    fun setSelectedAvatar(avatarId: String) {
        prefs.edit().putString("selected_avatar", avatarId).apply()
        _state.value = _state.value.copy(selectedAvatar = avatarId)
    }

    fun setSelectedTableTheme(themeId: String) {
        prefs.edit().putString("selected_theme", themeId).apply()
        _state.value = _state.value.copy(selectedTableTheme = themeId)
    }
}
