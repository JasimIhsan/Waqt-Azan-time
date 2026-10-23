package com.waqt.app.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.waqt.app.data.location.LocationHelper
import com.waqt.app.data.notification.NotificationHelper
import com.waqt.app.data.repository.UserPreferencesRepository
import com.waqt.app.ui.widget.WidgetSyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver triggered by AlarmManager for:
 * 1. Azan on-time notification
 * 2. Ending-soon warnings (30 min, 15 min, 5 min before current prayer ends)
 * 3. Widget transition refresh & system boot / time shifts
 */
class PrayerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repo = UserPreferencesRepository(context)
                val prefs = repo.getUserPreferences()

                val alarmType = intent?.getStringExtra(EXTRA_ALARM_TYPE)
                val prayerName = intent?.getStringExtra(EXTRA_PRAYER_NAME) ?: "Prayer"
                val prayerTime = intent?.getStringExtra(EXTRA_PRAYER_TIME) ?: ""
                val minutesRemaining = intent?.getIntExtra(EXTRA_MINUTES_REMAINING, 0) ?: 0
                val endTime = intent?.getStringExtra(EXTRA_END_TIME) ?: ""

                if (prefs.notificationsEnabled) {
                    val notificationHelper = NotificationHelper(context)
                    when (alarmType) {
                        TYPE_AZAN -> {
                            if (prefs.notifyAtAzanTime) {
                                notificationHelper.showAzanNotification(
                                    prayerName = prayerName,
                                    prayerTime = prayerTime,
                                    locality = prefs.localityName,
                                    soundId = prefs.selectedSoundId
                                )
                            }
                        }
                        TYPE_ENDING_SOON -> {
                            val shouldNotify = when (minutesRemaining) {
                                30 -> prefs.notifyBeforeEnd30Min
                                15 -> prefs.notifyBeforeEnd15Min
                                5 -> prefs.notifyBeforeEnd5Min
                                else -> false
                            }
                            if (shouldNotify) {
                                notificationHelper.showPrayerEndingSoonNotification(
                                    prayerName = prayerName,
                                    minutesRemaining = minutesRemaining,
                                    endTime = endTime,
                                    soundId = prefs.selectedSoundId
                                )
                            }
                        }
                    }
                }

                // In all cases, re-sync Glance widget and refresh upcoming alarm schedule
                WidgetSyncManager.syncAndSchedule(context)

                // If device rebooted, restore 5km displacement tracking if permission is granted
                if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
                    val locationHelper = LocationHelper(context)
                    if (locationHelper.hasLocationPermission()) {
                        locationHelper.startDisplacementTracking()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_PRAYER_ALARM = "com.waqt.app.ACTION_PRAYER_ALARM"

        const val EXTRA_ALARM_TYPE = "extra_alarm_type"
        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_PRAYER_TIME = "extra_prayer_time"
        const val EXTRA_MINUTES_REMAINING = "extra_minutes_remaining"
        const val EXTRA_END_TIME = "extra_end_time"

        const val TYPE_WIDGET_TRANSITION = "type_widget_transition"
        const val TYPE_AZAN = "type_azan"
        const val TYPE_ENDING_SOON = "type_ending_soon"
    }
}
