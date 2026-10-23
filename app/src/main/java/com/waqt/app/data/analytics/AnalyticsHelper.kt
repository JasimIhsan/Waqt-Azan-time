package com.waqt.app.data.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class AnalyticsHelper(context: Context) {
    private val firebaseAnalytics = FirebaseAnalytics.getInstance(context.applicationContext)

    fun logScreenView(screenName: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, "MainActivity")
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    fun logWidgetPinned() {
        val bundle = Bundle().apply {
            putString("action", "pin_widget")
        }
        firebaseAnalytics.logEvent("widget_pinned", bundle)
    }

    fun logUpdateCheck(foundUpdate: Boolean, version: String? = null) {
        val bundle = Bundle().apply {
            putBoolean("found_update", foundUpdate)
            if (version != null) {
                putString("target_version", version)
            }
        }
        firebaseAnalytics.logEvent("update_checked", bundle)
    }

    fun logToneSelected(prayer: String, soundName: String) {
        val bundle = Bundle().apply {
            putString("prayer", prayer)
            putString("sound_name", soundName)
        }
        firebaseAnalytics.logEvent("tone_selected", bundle)
    }

    fun logCalculationMethodChanged(method: String) {
        val bundle = Bundle().apply {
            putString("method", method)
        }
        firebaseAnalytics.logEvent("calc_method_changed", bundle)
    }

    fun logMadhabChanged(madhab: String) {
        val bundle = Bundle().apply {
            putString("madhab", madhab)
        }
        firebaseAnalytics.logEvent("madhab_changed", bundle)
    }

    fun logNotificationToggled(prayer: String, enabled: Boolean) {
        val bundle = Bundle().apply {
            putString("prayer", prayer)
            putBoolean("enabled", enabled)
        }
        firebaseAnalytics.logEvent("notification_toggled", bundle)
    }
}
