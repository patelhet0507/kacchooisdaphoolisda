package com.example.model

data class AvatarItem(
    val id: String,
    val name: String,
    val emoji: String,
    val requiredGames: Int
)

data class TableThemeItem(
    val id: String,
    val name: String,
    val description: String,
    val requiredGames: Int,
    val primaryColorHex: Long,
    val surfaceColorHex: Long
)

object CustomizationData {
    val avatars = listOf(
        AvatarItem("lion", "Royal Lion", "🦁", 0),
        AvatarItem("fox", "Cunning Fox", "🦊", 2),
        AvatarItem("dragon", "Golden Dragon", "🐉", 5),
        AvatarItem("crown", "Emperor Crown", "👑", 10),
        AvatarItem("tiger", "Fierce Tiger", "🐯", 15),
        AvatarItem("lightning", "Storm Lightning", "⚡", 20)
    )

    val themes = listOf(
        TableThemeItem("emerald", "Classic Emerald", "Traditional deep green felt table", 0, 0xFF064E3B, 0xFF022C22),
        TableThemeItem("crimson", "Royal Crimson", "Luxurious red velvet casino felt", 3, 0xFF7F1D1D, 0xFF450A0A),
        TableThemeItem("midnight", "Midnight Blue", "Deep cosmic evening felt", 7, 0xFF1E3A8A, 0xFF0F172A),
        TableThemeItem("gold", "Golden Sunburst", "Rich amber and golden felt", 12, 0xFF78350F, 0xFF451A03),
        TableThemeItem("neon", "Neon Amethyst", "Vibrant purple cyber felt", 18, 0xFF581C87, 0xFF2E1065)
    )
}
