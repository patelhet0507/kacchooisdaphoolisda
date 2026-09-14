package com.example.model

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val isUnlocked: Boolean = false,
    val progress: Int = 0,
    val maxProgress: Int = 1,
    val category: String = "Gameplay" // Gameplay, Social, Master
)

object AchievementData {
    val ALL_ACHIEVEMENTS = listOf(
        Achievement(
            id = "first_win",
            title = "First Victory",
            description = "Win your very first Kaachu Phool match!",
            emoji = "🏆",
            maxProgress = 1,
            category = "Gameplay"
        ),
        Achievement(
            id = "wins_5",
            title = "Rising Star",
            description = "Win 5 matches in single player or multiplayer.",
            emoji = "⭐",
            maxProgress = 5,
            category = "Gameplay"
        ),
        Achievement(
            id = "wins_10",
            title = "Kaachu Master",
            description = "Win 10 total matches and dominate the table.",
            emoji = "👑",
            maxProgress = 10,
            category = "Gameplay"
        ),
        Achievement(
            id = "perfect_game",
            title = "Perfect Game",
            description = "Complete a full match hitting 100% of your bids accurately!",
            emoji = "🌟",
            maxProgress = 1,
            category = "Master"
        ),
        Achievement(
            id = "high_scorer",
            title = "High Scorer",
            description = "Score over 50 total points in a single match.",
            emoji = "🚀",
            maxProgress = 1,
            category = "Gameplay"
        ),
        Achievement(
            id = "card_veteran",
            title = "Dedicated Player",
            description = "Play 5 total matches.",
            emoji = "🃏",
            maxProgress = 5,
            category = "Gameplay"
        ),
        Achievement(
            id = "socialite",
            title = "Socialite",
            description = "Add at least 1 friend to your friends list.",
            emoji = "👥",
            maxProgress = 1,
            category = "Social"
        ),
        Achievement(
            id = "hook_master",
            title = "Hook Master",
            description = "Win a match on Hard Difficulty or in Online Multiplayer.",
            emoji = "🎯",
            maxProgress = 1,
            category = "Master"
        )
    )
}
