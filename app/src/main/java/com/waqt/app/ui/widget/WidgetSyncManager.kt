package com.waqt.app.ui.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.glance.appwidget.updateAll
import com.batoulapps.adhan.Prayer
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import com.waqt.app.data.calculator.PrayerCalculator
import com.waqt.app.data.receiver.PrayerAlarmReceiver
import com.waqt.app.data.repository.UserPreferences
import com.waqt.app.data.repository.UserPreferencesRepository
import java.util.Calendar
import java.util.Date

/**
 * Coordinates Jetpack Glance widget updates and exact AlarmManager scheduling
 * for:
 * 1. Widget transition moments
 * 2. On-time Azan notifications
 * 3. 30 min, 15 min, 5 min warnings before prayer end
 */
object WidgetSyncManager {

    private const val BUFFER_MILLIS = 1000L // 1 second buffer past transition to avoid clock edge jitter
    private const val WIDGET_ALARM_REQ_CODE = 50000

    /**
     * Recomputes next prayer from stored location, redraws Glance widget,
     * and arms exact transition and notification alarms.
     */
    suspend fun syncAndSchedule(context: Context) {
        val repo = UserPreferencesRepository(context)
        val prefs = repo.getUserPreferences()
        val calculator = PrayerCalculator(prefs.calculationMethod, prefs.madhab)
        val nextPrayer = calculator.getNextPrayer(prefs.latitude, prefs.longitude)

        // 1. Redraw all Glance widget instances
        try {
            WaqtWidget().updateAll(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Schedule widget transition alarm
        scheduleWidgetTransitionAlarm(context, nextPrayer.time.time + BUFFER_MILLIS)

        // 3. Schedule notification alarms (Azan on-time and 30m, 15m, 5m ending-soon warnings)
        if (prefs.notificationsEnabled) {
            scheduleNotificationAlarms(context, prefs, calculator)
        } else {
            cancelNotificationAlarms(context)
        }
    }

    private fun scheduleWidgetTransitionAlarm(context: Context, triggerAtMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = PrayerAlarmReceiver.ACTION_PRAYER_ALARM
            putExtra(PrayerAlarmReceiver.EXTRA_ALARM_TYPE, PrayerAlarmReceiver.TYPE_WIDGET_TRANSITION)
        }

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            WIDGET_ALARM_REQ_CODE,
            intent,
            flags
        )

        val finalTriggerMillis = triggerAtMillis.coerceAtLeast(System.currentTimeMillis() + 1000L)
        setExactAlarm(alarmManager, finalTriggerMillis, pendingIntent)
    }

    private fun scheduleNotificationAlarms(
        context: Context,
        prefs: UserPreferences,
        calculator: PrayerCalculator
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val now = System.currentTimeMillis()
        val coordinates = com.batoulapps.adhan.Coordinates(prefs.latitude, prefs.longitude)
        val params = calculator.getParameters()

        // We schedule for today and tomorrow
        for (dayOffset in 0..1) {
            val calendar = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, dayOffset)
            }
            val date = calendar.time
            val components = DateComponents.from(date)
            val times = PrayerTimes(coordinates, components, params)

            val nextDayCalendar = Calendar.getInstance().apply {
                time = date
                add(Calendar.DAY_OF_YEAR, 1)
            }
            val nextDayTimes = PrayerTimes(coordinates, DateComponents.from(nextDayCalendar.time), params)

            // Define prayer intervals: (Prayer, Start Time, End Time)
            val prayerIntervals = listOf(
                Triple(Prayer.FAJR, times.fajr, times.sunrise),
                Triple(Prayer.DHUHR, times.dhuhr, times.asr),
                Triple(Prayer.ASR, times.asr, times.maghrib),
                Triple(Prayer.MAGHRIB, times.maghrib, times.isha),
                Triple(Prayer.ISHA, times.isha, nextDayTimes.fajr)
            )

            prayerIntervals.forEachIndexed { prayerIndex, (prayer, startTime, endTime) ->
                val prayerName = calculator.getPrayerDisplayName(prayer)
                val prayerTimeStr = calculator.formatTime(startTime)
                val endTimeStr = calculator.formatTime(endTime)

                // A. On-Time Azan Alarm
                if (prefs.notifyAtAzanTime && startTime.time > now) {
                    val reqCode = 10000 + (dayOffset * 100) + prayerIndex
                    val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                        action = PrayerAlarmReceiver.ACTION_PRAYER_ALARM
                        putExtra(PrayerAlarmReceiver.EXTRA_ALARM_TYPE, PrayerAlarmReceiver.TYPE_AZAN)
                        putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_NAME, prayerName)
                        putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_TIME, prayerTimeStr)
                    }
                    val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    val pendingIntent = PendingIntent.getBroadcast(context, reqCode, intent, flags)
                    setExactAlarm(alarmManager, startTime.time, pendingIntent)
                }

                // B. Ending-Soon Warnings (30, 15, 5 minutes before current prayer ends)
                val warningMinutes = listOf(
                    Triple(30, prefs.notifyBeforeEnd30Min, 20000),
                    Triple(15, prefs.notifyBeforeEnd15Min, 30000),
                    Triple(5, prefs.notifyBeforeEnd5Min, 40000)
                )

                warningMinutes.forEach { (mins, enabled, baseReqCode) ->
                    if (enabled) {
                        val triggerTime = endTime.time - (mins * 60 * 1000L)
                        if (triggerTime > now && triggerTime >= startTime.time) {
                            val reqCode = baseReqCode + (dayOffset * 100) + prayerIndex
                            val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                                action = PrayerAlarmReceiver.ACTION_PRAYER_ALARM
                                putExtra(PrayerAlarmReceiver.EXTRA_ALARM_TYPE, PrayerAlarmReceiver.TYPE_ENDING_SOON)
                                putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_NAME, prayerName)
                                putExtra(PrayerAlarmReceiver.EXTRA_MINUTES_REMAINING, mins)
                                putExtra(PrayerAlarmReceiver.EXTRA_END_TIME, endTimeStr)
                            }
                            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            val pendingIntent = PendingIntent.getBroadcast(context, reqCode, intent, flags)
                            setExactAlarm(alarmManager, triggerTime, pendingIntent)
                        }
                    }
                }
            }
        }
    }

    private fun cancelNotificationAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        for (dayOffset in 0..1) {
            for (prayerIndex in 0..4) {
                val codes = listOf(
                    10000 + (dayOffset * 100) + prayerIndex,
                    20000 + (dayOffset * 100) + prayerIndex,
                    30000 + (dayOffset * 100) + prayerIndex,
                    40000 + (dayOffset * 100) + prayerIndex
                )
                codes.forEach { code ->
                    val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                        action = PrayerAlarmReceiver.ACTION_PRAYER_ALARM
                    }
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        code,
                        intent,
                        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                    )
                    if (pendingIntent != null) {
                        alarmManager.cancel(pendingIntent)
                        pendingIntent.cancel()
                    }
                }
            }
        }
    }

    private fun setExactAlarm(alarmManager: AlarmManager, triggerAtMillis: Long, pendingIntent: PendingIntent) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }
}
