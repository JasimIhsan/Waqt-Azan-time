package com.waqt.app

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.Prayer
import com.waqt.app.data.calculator.PrayerCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.Date

class PrayerCalculatorTest {

    private lateinit var calculator: PrayerCalculator

    // Mecca Coordinates
    private val meccaLat = 21.4225
    private val meccaLng = 39.8262

    @Before
    fun setup() {
        calculator = PrayerCalculator(
            calculationMethod = CalculationMethod.MUSLIM_WORLD_LEAGUE,
            madhab = Madhab.SHAFI
        )
    }

    @Test
    fun testDailyScheduleContainsAllPrayersAndSunrise() {
        val schedule = calculator.getDailySchedule(meccaLat, meccaLng, Date())

        assertNotNull(schedule[Prayer.FAJR])
        assertNotNull(schedule[Prayer.SUNRISE])
        assertNotNull(schedule[Prayer.DHUHR])
        assertNotNull(schedule[Prayer.ASR])
        assertNotNull(schedule[Prayer.MAGHRIB])
        assertNotNull(schedule[Prayer.ISHA])

        // Verify chronological ordering
        val fajr = schedule[Prayer.FAJR]!!
        val sunrise = schedule[Prayer.SUNRISE]!!
        val dhuhr = schedule[Prayer.DHUHR]!!
        val asr = schedule[Prayer.ASR]!!
        val maghrib = schedule[Prayer.MAGHRIB]!!
        val isha = schedule[Prayer.ISHA]!!

        assertTrue(fajr.before(sunrise))
        assertTrue(sunrise.before(dhuhr))
        assertTrue(dhuhr.before(asr))
        assertTrue(asr.before(maghrib))
        assertTrue(maghrib.before(isha))
    }

    @Test
    fun testNextPrayerBeforeFajrReturnsFajrToday() {
        // Set time to 01:00 AM today
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 1)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        val next = calculator.getNextPrayer(meccaLat, meccaLng, cal.time)
        assertEquals(Prayer.FAJR, next.prayer)
        assertFalse(next.isTomorrow)
    }

    @Test
    fun testTomorrowFajrEdgeCaseWhenAllPrayersConcluded() {
        // Set time to 11:59 PM today (after Isha)
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 0)
        }

        val next = calculator.getNextPrayer(meccaLat, meccaLng, cal.time)

        // After Isha, next prayer must transition to tomorrow's Fajr
        assertEquals(Prayer.FAJR, next.prayer)
        assertTrue(next.isTomorrow)
        assertTrue(next.time.after(cal.time))
    }

    @Test
    fun testFullScheduleItemsHighlighting() {
        val fullSchedule = calculator.getFullSchedule(meccaLat, meccaLng, Date())
        assertEquals(6, fullSchedule.items.size)

        // At most one item should be marked as next
        val nextItemsCount = fullSchedule.items.count { it.isNext }
        assertTrue(nextItemsCount <= 1)
    }
}
