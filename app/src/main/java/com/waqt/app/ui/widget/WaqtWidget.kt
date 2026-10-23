package com.waqt.app.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.batoulapps.adhan.Prayer
import com.waqt.app.MainActivity
import com.waqt.app.R
import com.waqt.app.data.calculator.PrayerCalculator
import com.waqt.app.data.repository.UserPreferencesRepository
import com.waqt.app.domain.model.NextPrayerInfo
import com.waqt.app.domain.model.PrayerSchedule
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Redesigned Modern Home Screen Widget built with Jetpack Glance.
 * Features a balanced responsive layout with hero highlights, live countdown pill,
 * location badge, and a full daily prayer timeline on medium/tall sizes.
 */
class WaqtWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repo = UserPreferencesRepository(context)
        val userPrefs = repo.getUserPreferences()
        val calculator = PrayerCalculator(userPrefs.calculationMethod, userPrefs.madhab)
        val nextPrayer = calculator.getNextPrayer(userPrefs.latitude, userPrefs.longitude)
        val fullSchedule = calculator.getFullSchedule(userPrefs.latitude, userPrefs.longitude)

        provideContent {
            WidgetContent(
                prayerInfo = nextPrayer,
                schedule = fullSchedule,
                locality = userPrefs.localityName
            )
        }
    }

    @Composable
    private fun WidgetContent(
        prayerInfo: NextPrayerInfo,
        schedule: PrayerSchedule,
        locality: String
    ) {
        val size = LocalSize.current
        val isTall = size.height >= 115.dp

        // Design Palette (matching official Waqt multi-color theme)
        val bgDeepNavy = Color(0xFF0F1512)
        val cardSurface = Color(0xFF16201B)
        val cardSurfaceVariant = Color(0xFF1E2C25)
        val accentSky = Color(0xFF69B6AF)
        val accentSkyDim = Color(0xFFAFD4A8)
        val textPrimary = Color(0xFFFFFFFF)
        val textSecondary = Color(0xFFE5F1E2)
        val textMuted = Color(0xFF90A89C)
        val emeraldTomorrow = Color(0xFFFF7262)
        val emeraldBg = Color(0xFF381C18)

        // Parse time digits and period
        val (timeDigits, timePeriod) = parseTimeParts(prayerInfo.formattedTime)

        // Countdown calculation
        val now = System.currentTimeMillis()
        val diff = prayerInfo.time.time - now
        val hours = (diff / (1000 * 60 * 60))
        val minutes = (diff / (1000 * 60)) % 60
        val countdownBadgeText = when {
            diff <= 0 -> "Now"
            hours > 0 -> "In ${hours}h ${minutes}m"
            else -> "In ${minutes}m"
        }

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(bgDeepNavy))
                .cornerRadius(20.dp)
                .padding(14.dp)
                .clickable(actionStartActivity(MainActivity::class.java))
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.Top,
                horizontalAlignment = Alignment.Start
            ) {
                // ==========================================
                // 1. TOP HEADER ROW (Badges + Location)
                // ==========================================
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Next Azan tag
                    Row(
                        modifier = GlanceModifier
                            .background(ColorProvider(cardSurfaceVariant))
                            .cornerRadius(6.dp)
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_widget_moon),
                            contentDescription = null,
                            modifier = GlanceModifier.width(11.dp).height(11.dp)
                        )
                        Spacer(modifier = GlanceModifier.width(4.dp))
                        Text(
                            text = "WAQT",
                            style = TextStyle(
                                color = ColorProvider(accentSky),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.width(6.dp))

                    // Tomorrow Badge (if applicable)
                    if (prayerInfo.isTomorrow) {
                        Box(
                            modifier = GlanceModifier
                                .background(ColorProvider(emeraldBg))
                                .cornerRadius(6.dp)
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "TOMORROW",
                                style = TextStyle(
                                    color = ColorProvider(emeraldTomorrow),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Spacer(modifier = GlanceModifier.width(6.dp))
                    }

                    // Countdown Badge
                    Box(
                        modifier = GlanceModifier
                            .background(ColorProvider(cardSurface))
                            .cornerRadius(6.dp)
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = countdownBadgeText,
                            style = TextStyle(
                                color = ColorProvider(textSecondary),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    // Locality caption with location icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_widget_location),
                            contentDescription = null,
                            modifier = GlanceModifier.width(10.dp).height(10.dp)
                        )
                        Spacer(modifier = GlanceModifier.width(3.dp))
                        Text(
                            text = locality,
                            maxLines = 1,
                            style = TextStyle(
                                color = ColorProvider(textSecondary),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.defaultWeight())

                // ==========================================
                // 2. HERO MIDDLE SECTION (Name & Large Time Card)
                // ==========================================
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Column: Prayer Name & Subtitle
                    Column(
                        modifier = GlanceModifier.defaultWeight()
                    ) {
                        Text(
                            text = prayerInfo.prayerName,
                            style = TextStyle(
                                color = ColorProvider(textPrimary),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(2.dp))
                        Text(
                            text = getPrayerSubtitle(prayerInfo.prayer),
                            style = TextStyle(
                                color = ColorProvider(textSecondary),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal
                            )
                        )
                    }

                    // Right Column: Elevated Digital Time Display Card
                    Box(
                        modifier = GlanceModifier
                            .background(ColorProvider(cardSurface))
                            .cornerRadius(12.dp)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = timeDigits,
                                style = TextStyle(
                                    color = ColorProvider(accentSky),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            if (timePeriod.isNotEmpty()) {
                                Spacer(modifier = GlanceModifier.width(4.dp))
                                Text(
                                    text = timePeriod,
                                    style = TextStyle(
                                        color = ColorProvider(accentSkyDim),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }

                // ==========================================
                // 3. BOTTOM TIMELINE ROW (For Tall / Medium widgets)
                // ==========================================
                if (isTall && schedule.items.isNotEmpty()) {
                    Spacer(modifier = GlanceModifier.defaultWeight())

                    val shortTimeFormatter = SimpleDateFormat("h:mm", Locale.getDefault())

                    Row(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .background(ColorProvider(cardSurfaceVariant))
                            .cornerRadius(10.dp)
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        schedule.items.forEachIndexed { index, item ->
                            val isUpcoming = item.isNext || (prayerInfo.isTomorrow && item.prayer == Prayer.FAJR)
                            val shortName = getPrayerShortName(item.prayer)
                            val shortTime = shortTimeFormatter.format(item.time)

                            Box(
                                modifier = GlanceModifier
                                    .defaultWeight()
                                    .background(
                                        ColorProvider(if (isUpcoming) accentSky else Color.Transparent)
                                    )
                                    .cornerRadius(7.dp)
                                    .padding(vertical = 3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = shortName,
                                        style = TextStyle(
                                            color = ColorProvider(if (isUpcoming) bgDeepNavy else textMuted),
                                            fontSize = 9.sp,
                                            fontWeight = if (isUpcoming) FontWeight.Bold else FontWeight.Medium
                                        )
                                    )
                                    Spacer(modifier = GlanceModifier.height(1.dp))
                                    Text(
                                        text = shortTime,
                                        style = TextStyle(
                                            color = ColorProvider(if (isUpcoming) bgDeepNavy else textSecondary),
                                            fontSize = 10.sp,
                                            fontWeight = if (isUpcoming) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                }
                            }

                            if (index < schedule.items.lastIndex) {
                                Spacer(modifier = GlanceModifier.width(2.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun parseTimeParts(formattedTime: String): Pair<String, String> {
        val parts = formattedTime.trim().split(" ")
        return if (parts.size >= 2) {
            Pair(parts[0], parts[1])
        } else {
            Pair(formattedTime, "")
        }
    }

    private fun getPrayerSubtitle(prayer: Prayer): String {
        return when (prayer) {
            Prayer.FAJR -> "Dawn Prayer • الفجر"
            Prayer.SUNRISE -> "Sunrise • الشروق"
            Prayer.DHUHR -> "Noon Prayer • الظهر"
            Prayer.ASR -> "Afternoon Prayer • العصر"
            Prayer.MAGHRIB -> "Sunset Prayer • المغرب"
            Prayer.ISHA -> "Night Prayer • العشاء"
            Prayer.NONE -> ""
        }
    }

    private fun getPrayerShortName(prayer: Prayer): String {
        return when (prayer) {
            Prayer.FAJR -> "FAJ"
            Prayer.SUNRISE -> "SUN"
            Prayer.DHUHR -> "DHU"
            Prayer.ASR -> "ASR"
            Prayer.MAGHRIB -> "MAG"
            Prayer.ISHA -> "ISH"
            Prayer.NONE -> ""
        }
    }
}
