package com.waqt.app.domain.model

import com.batoulapps.adhan.Prayer
import java.util.Date

/**
 * Encapsulates the next upcoming prayer information.
 */
data class NextPrayerInfo(
    val prayer: Prayer,
    val prayerName: String,
    val time: Date,
    val formattedTime: String,
    val isTomorrow: Boolean = false,
    val millisRemaining: Long = 0L
)
