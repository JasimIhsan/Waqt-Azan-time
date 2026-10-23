package com.waqt.app.domain.model

import com.batoulapps.adhan.Prayer
import java.util.Date

/**
 * Individual entry for a prayer/solar event in the daily schedule.
 */
data class PrayerScheduleItem(
    val prayer: Prayer,
    val name: String,
    val time: Date,
    val formattedTime: String,
    val isNext: Boolean = false
)

/**
 * Full daily schedule model containing 5 daily prayers + sunrise.
 */
data class PrayerSchedule(
    val date: Date,
    val fajr: Date,
    val sunrise: Date,
    val dhuhr: Date,
    val asr: Date,
    val maghrib: Date,
    val isha: Date,
    val items: List<PrayerScheduleItem> = emptyList()
)
