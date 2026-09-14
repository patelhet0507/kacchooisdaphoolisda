package com.example.model

enum class Emote(val emoji: String, val label: String) {
    THUMBS_UP("👍", "Good Luck!"),
    LAUGHING("😂", "Haha!"),
    THINKING("🤔", "Hmm..."),
    CLAPPING("👏", "Well Played!"),
    ANGRY("🤬", "Ouch!"),
    HEART("❤️", "Love it!"),
    SURPRISED("😮", "Wow!"),
    FIRE("🔥", "On Fire!")
}

data class ActiveEmote(
    val senderName: String,
    val emoteEmoji: String,
    val timestamp: Long = System.currentTimeMillis()
)
