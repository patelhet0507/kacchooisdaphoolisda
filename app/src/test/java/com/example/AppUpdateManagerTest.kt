package com.example

import com.example.update.AppUpdateManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdateManagerTest {

    @Test
    fun verifyVersionComparison() {
        // Newer version tags
        assertTrue(AppUpdateManager.isVersionNewer("v1.1", "1.0"))
        assertTrue(AppUpdateManager.isVersionNewer("1.0.1", "1.0"))
        assertTrue(AppUpdateManager.isVersionNewer("v2.0.0", "1.9.9"))
        assertTrue(AppUpdateManager.isVersionNewer("latest", "1.0"))

        // Same version
        assertFalse(AppUpdateManager.isVersionNewer("v1.0", "1.0"))
        assertFalse(AppUpdateManager.isVersionNewer("1.0", "1.0"))

        // Older version tags
        assertFalse(AppUpdateManager.isVersionNewer("v0.9", "1.0"))
        assertFalse(AppUpdateManager.isVersionNewer("1.0.0", "1.0.1"))
    }
}
