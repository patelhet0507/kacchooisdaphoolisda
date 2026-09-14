package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppSettingsState(
    val soundEffectsEnabled: Boolean = true,
    val soundVolume: Float = 0.85f,
    val hapticFeedbackEnabled: Boolean = true,
    val autoSortHand: Boolean = true,
    val dealerHookWarning: Boolean = true,
    val fastBotTurns: Boolean = false,
    val is3DMode: Boolean = false,
    val autoCheckUpdates: Boolean = true,
    val githubRepo: String = "patelhet0507/kacchooisdaphoolisda"
)

class SettingsManager private constructor(context: Context) {
    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        "kaachu_phool_app_settings",
        Context.MODE_PRIVATE
    )

    private val _settings = MutableStateFlow(
        AppSettingsState(
            soundEffectsEnabled = prefs.getBoolean("sound_effects_enabled", true),
            soundVolume = prefs.getFloat("sound_volume", 0.85f),
            hapticFeedbackEnabled = prefs.getBoolean("haptic_feedback_enabled", true),
            autoSortHand = prefs.getBoolean("auto_sort_hand", true),
            dealerHookWarning = prefs.getBoolean("dealer_hook_warning", true),
            fastBotTurns = prefs.getBoolean("fast_bot_turns", false),
            is3DMode = prefs.getBoolean("is_3d_mode", false),
            autoCheckUpdates = prefs.getBoolean("auto_check_updates", true),
            githubRepo = prefs.getString("github_repo", "patelhet0507/kacchooisdaphoolisda") ?: "patelhet0507/kacchooisdaphoolisda"
        )
    )
    val settings: StateFlow<AppSettingsState> = _settings.asStateFlow()

    fun setSoundEffectsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("sound_effects_enabled", enabled).apply()
        _settings.value = _settings.value.copy(soundEffectsEnabled = enabled)
    }

    fun setSoundVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        prefs.edit().putFloat("sound_volume", clamped).apply()
        _settings.value = _settings.value.copy(soundVolume = clamped)
    }

    fun setHapticFeedbackEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("haptic_feedback_enabled", enabled).apply()
        _settings.value = _settings.value.copy(hapticFeedbackEnabled = enabled)
    }

    fun setAutoSortHand(enabled: Boolean) {
        prefs.edit().putBoolean("auto_sort_hand", enabled).apply()
        _settings.value = _settings.value.copy(autoSortHand = enabled)
    }

    fun setDealerHookWarning(enabled: Boolean) {
        prefs.edit().putBoolean("dealer_hook_warning", enabled).apply()
        _settings.value = _settings.value.copy(dealerHookWarning = enabled)
    }

    fun setFastBotTurns(enabled: Boolean) {
        prefs.edit().putBoolean("fast_bot_turns", enabled).apply()
        _settings.value = _settings.value.copy(fastBotTurns = enabled)
    }

    fun set3DMode(enabled: Boolean) {
        prefs.edit().putBoolean("is_3d_mode", enabled).apply()
        _settings.value = _settings.value.copy(is3DMode = enabled)
    }

    fun setAutoCheckUpdates(enabled: Boolean) {
        prefs.edit().putBoolean("auto_check_updates", enabled).apply()
        _settings.value = _settings.value.copy(autoCheckUpdates = enabled)
    }

    fun setGithubRepo(repo: String) {
        val trimmed = repo.trim().removePrefix("https://github.com/").removeSuffix("/")
        prefs.edit().putString("github_repo", trimmed).apply()
        _settings.value = _settings.value.copy(githubRepo = trimmed)
    }

    companion object {
        @Volatile
        private var INSTANCE: SettingsManager? = null

        fun getInstance(context: Context): SettingsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
