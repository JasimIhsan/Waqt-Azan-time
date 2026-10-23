package com.waqt.app

import com.waqt.app.data.updater.AppUpdateManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdateManagerTest {

    @Test
    fun testVersionComparison_newerPatch() {
        assertTrue(AppUpdateManager.isNewerVersion("1.0.0", "v1.0.1"))
        assertTrue(AppUpdateManager.isNewerVersion("1.0.0", "1.0.1"))
    }

    @Test
    fun testVersionComparison_newerMinor() {
        assertTrue(AppUpdateManager.isNewerVersion("1.0.9", "v1.1.0"))
    }

    @Test
    fun testVersionComparison_newerMajor() {
        assertTrue(AppUpdateManager.isNewerVersion("1.9.9", "v2.0.0"))
    }

    @Test
    fun testVersionComparison_sameVersion() {
        assertFalse(AppUpdateManager.isNewerVersion("1.0.0", "v1.0.0"))
        assertFalse(AppUpdateManager.isNewerVersion("1.0.0", "1.0.0"))
    }

    @Test
    fun testVersionComparison_olderVersion() {
        assertFalse(AppUpdateManager.isNewerVersion("1.1.0", "v1.0.9"))
        assertFalse(AppUpdateManager.isNewerVersion("2.0.0", "v1.9.9"))
    }

    @Test
    fun testVersionComparison_extraDigits() {
        assertTrue(AppUpdateManager.isNewerVersion("1.0", "1.0.1"))
        assertFalse(AppUpdateManager.isNewerVersion("1.0.1", "1.0"))
    }

    @Test
    fun testVersionComparison_invalidOrEmpty() {
        assertFalse(AppUpdateManager.isNewerVersion("", "v1.0.0"))
        assertFalse(AppUpdateManager.isNewerVersion("1.0.0", ""))
    }
}
