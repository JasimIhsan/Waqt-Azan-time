package com.waqt.app.data.calculator

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.CalculationParameters
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.Prayer
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import com.waqt.app.domain.model.NextPrayerInfo
import com.waqt.app.domain.model.PrayerSchedule
import com.waqt.app.domain.model.PrayerScheduleItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Core mathematical calculation engine wrapping Batoul Apps adhan-java.
 * Handles offline solar prayer computations, configurable methods/madhabs,
 * daily schedule mapping, and tomorrow's Fajr edge-case transition.
 */
class PrayerCalculator(
    var calculationMethod: CalculationMethod = CalculationMethod.MUSLIM_WORLD_LEAGUE,
    var madhab: Madhab = Madhab.SHAFI
) {

    private val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())

    fun getParameters(): CalculationParameters {
        val params = calculationMethod.parameters
        params.madhab = madhab
        return params
    }

    /**
     * Determines the single next upcoming prayer relative to [date].
     * If all prayers for today are concluded (or nextPrayer returns NONE),
     * computes tomorrow's Fajr and tags `isTomorrow = true`.
     */
    fun getNextPrayer(lat: Double, lng: Double, date: Date = Date()): NextPrayerInfo {
        val coordinates = Coordinates(lat, lng)
        val params = getParameters()
        val todayComponents = DateComponents.from(date)
        val todayTimes = PrayerTimes(coordinates, todayComponents, params)

        val next = todayTimes.nextPrayer(date)

        if (next == null || next == Prayer.NONE) {
            // Edge-case: Today's prayers are concluded; calculate tomorrow's Fajr
            val calendar = Calendar.getInstance().apply {
                time = date
                add(Calendar.DAY_OF_YEAR, 1)
            }
            val tomorrowDate = calendar.time
            val tomorrowComponents = DateComponents.from(tomorrowDate)
            val tomorrowTimes = PrayerTimes(coordinates, tomorrowComponents, params)
            val tomorrowFajr = tomorrowTimes.fajr

            return NextPrayerInfo(
                prayer = Prayer.FAJR,
                prayerName = getPrayerDisplayName(Prayer.FAJR),
                time = tomorrowFajr,
                formattedTime = formatTime(tomorrowFajr),
                isTomorrow = true,
                millisRemaining = (tomorrowFajr.time - date.time).coerceAtLeast(0L)
            )
        } else {
            val prayerDate = todayTimes.timeForPrayer(next) ?: todayTimes.fajr
            return NextPrayerInfo(
                prayer = next,
                prayerName = getPrayerDisplayName(next),
                time = prayerDate,
                formattedTime = formatTime(prayerDate),
                isTomorrow = false,
                millisRemaining = (prayerDate.time - date.time).coerceAtLeast(0L)
            )
        }
    }

    /**
     * Returns a linked map of all standard daily prayer/solar milestones.
     */
    fun getDailySchedule(lat: Double, lng: Double, date: Date = Date()): Map<Prayer, Date> {
        val coordinates = Coordinates(lat, lng)
        val params = getParameters()
        val components = DateComponents.from(date)
        val times = PrayerTimes(coordinates, components, params)

        return linkedMapOf(
            Prayer.FAJR to times.fajr,
            Prayer.SUNRISE to times.sunrise,
            Prayer.DHUHR to times.dhuhr,
            Prayer.ASR to times.asr,
            Prayer.MAGHRIB to times.maghrib,
            Prayer.ISHA to times.isha
        )
    }

    /**
     * Returns a structured [PrayerSchedule] domain model containing formatted items
     * with the next upcoming prayer distinctly highlighted.
     */
    fun getFullSchedule(lat: Double, lng: Double, date: Date = Date()): PrayerSchedule {
        val coordinates = Coordinates(lat, lng)
        val params = getParameters()
        val components = DateComponents.from(date)
        val times = PrayerTimes(coordinates, components, params)
        val next = times.nextPrayer(date)

        val orderedPrayers = listOf(
            Prayer.FAJR to times.fajr,
            Prayer.SUNRISE to times.sunrise,
            Prayer.DHUHR to times.dhuhr,
            Prayer.ASR to times.asr,
            Prayer.MAGHRIB to times.maghrib,
            Prayer.ISHA to times.isha
        )

        val items = orderedPrayers.map { (prayer, prayerTime) ->
            PrayerScheduleItem(
                prayer = prayer,
                name = getPrayerDisplayName(prayer),
                time = prayerTime,
                formattedTime = formatTime(prayerTime),
                isNext = (prayer == next)
            )
        }

        return PrayerSchedule(
            date = date,
            fajr = times.fajr,
            sunrise = times.sunrise,
            dhuhr = times.dhuhr,
            asr = times.asr,
            maghrib = times.maghrib,
            isha = times.isha,
            items = items
        )
    }

    fun formatTime(date: Date): String {
        return timeFormatter.format(date)
    }

    fun getPrayerDisplayName(prayer: Prayer): String {
        return when (prayer) {
            Prayer.FAJR -> "Fajr"
            Prayer.SUNRISE -> "Sunrise"
            Prayer.DHUHR -> "Dhuhr"
            Prayer.ASR -> "Asr"
            Prayer.MAGHRIB -> "Maghrib"
            Prayer.ISHA -> "Isha"
            Prayer.NONE -> "None"
        }
    }

    companion object {
        val defaultInstance: PrayerCalculator by lazy { PrayerCalculator() }
    }
}
