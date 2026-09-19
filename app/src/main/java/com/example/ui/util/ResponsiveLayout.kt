package com.example.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Standard breakpoints matching Android Material 3 WindowSizeClass specifications:
 * - Compact: Width < 600dp (Typical phone in portrait)
 * - Medium: Width 600dp - 840dp (Large phone in landscape, small tablet, foldable)
 * - Expanded: Width > 840dp (Large tablet, desktop)
 */
enum class WindowSizeCategory {
    COMPACT,
    MEDIUM,
    EXPANDED
}

data class WindowLayoutInfo(
    val widthCategory: WindowSizeCategory,
    val isLandscape: Boolean,
    val isTablet: Boolean,
    val availableWidth: Dp,
    val availableHeight: Dp
) {
    val isCompact: Boolean get() = widthCategory == WindowSizeCategory.COMPACT
    val isMedium: Boolean get() = widthCategory == WindowSizeCategory.MEDIUM
    val isExpanded: Boolean get() = widthCategory == WindowSizeCategory.EXPANDED
}

fun calculateWindowLayoutInfo(width: Dp, height: Dp): WindowLayoutInfo {
    val category = when {
        width < 600.dp -> WindowSizeCategory.COMPACT
        width <= 840.dp -> WindowSizeCategory.MEDIUM
        else -> WindowSizeCategory.EXPANDED
    }
    val isLandscape = width > height
    val isTablet = (minOf(width, height) >= 600.dp) || width >= 840.dp
    return WindowLayoutInfo(
        widthCategory = category,
        isLandscape = isLandscape,
        isTablet = isTablet,
        availableWidth = width,
        availableHeight = height
    )
}
