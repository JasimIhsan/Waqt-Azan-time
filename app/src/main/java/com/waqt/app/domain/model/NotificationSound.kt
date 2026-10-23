package com.waqt.app.domain.model

import androidx.annotation.RawRes
import com.waqt.app.R

/**
 * Metadata for a selectable minimal notification sound.
 */
data class NotificationSound(
    val id: String,
    val name: String,
    val description: String,
    @RawRes val rawResId: Int
)

object NotificationSounds {
    val ALL_SOUNDS = listOf(
        NotificationSound(
            id = "zen_chime",
            name = "Zen Chime",
            description = "528 Hz soothing Solfeggio chime",
            rawResId = R.raw.tone_zen_chime
        ),
        NotificationSound(
            id = "minimal_bell",
            name = "Minimal Bell",
            description = "880 Hz crisp metallic bell",
            rawResId = R.raw.tone_minimal_bell
        ),
        NotificationSound(
            id = "gentle_marimba",
            name = "Gentle Marimba",
            description = "Warm two-note wooden motif",
            rawResId = R.raw.tone_gentle_marimba
        ),
        NotificationSound(
            id = "soft_gong",
            name = "Soft Gong",
            description = "Low meditative warm chime",
            rawResId = R.raw.tone_soft_gong
        ),
        NotificationSound(
            id = "celestial_drop",
            name = "Celestial Drop",
            description = "Pure water droplet pitch sweep",
            rawResId = R.raw.tone_celestial_drop
        ),
        NotificationSound(
            id = "serene_ting",
            name = "Serene Ting",
            description = "Clear high crystal triangle",
            rawResId = R.raw.tone_serene_ting
        ),
        NotificationSound(
            id = "peaceful_harp",
            name = "Peaceful Harp",
            description = "3-note ascending arpeggio",
            rawResId = R.raw.tone_peaceful_harp
        ),
        NotificationSound(
            id = "subtle_ripple",
            name = "Subtle Ripple",
            description = "Smooth harmonic chord",
            rawResId = R.raw.tone_subtle_ripple
        ),
        NotificationSound(
            id = "crystal_ping",
            name = "Crystal Ping",
            description = "Crisp modern acoustic ping",
            rawResId = R.raw.tone_crystal_ping
        ),
        NotificationSound(
            id = "ambient_glow",
            name = "Ambient Glow",
            description = "Gentle ambient fifth swell",
            rawResId = R.raw.tone_ambient_glow
        )
    )

    val DEFAULT_SOUND = ALL_SOUNDS.first()

    fun getSoundById(id: String): NotificationSound {
        return ALL_SOUNDS.firstOrNull { it.id == id } ?: DEFAULT_SOUND
    }
}
