package com.waqt.app.data.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.waqt.app.MainActivity
import com.waqt.app.R
import com.waqt.app.domain.model.NotificationSounds

class NotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private var mediaPlayer: MediaPlayer? = null

    /**
     * Generates a unique channel ID per sound to ensure Android updates the sound URI.
     */
    private fun getChannelId(soundId: String): String {
        return "azan_alerts_channel_$soundId"
    }

    private fun ensureChannel(soundId: String): String {
        val channelId = getChannelId(soundId)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val sound = NotificationSounds.getSoundById(soundId)
            val soundUri = Uri.parse(
                "${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/${sound.rawResId}"
            )

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .build()

            val existingChannel = notificationManager.getNotificationChannel(channelId)
            if (existingChannel == null) {
                val channel = NotificationChannel(
                    channelId,
                    "Azan & Prayer Reminders (${sound.name})",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Prayer arrival and ending warnings with custom minimal tones."
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 250, 150, 250)
                    setSound(soundUri, audioAttributes)
                    lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                }
                notificationManager.createNotificationChannel(channel)
            }
        }
        return channelId
    }

    private fun getSoundUri(soundId: String): Uri {
        val sound = NotificationSounds.getSoundById(soundId)
        return Uri.parse(
            "${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/${sound.rawResId}"
        )
    }

    private fun getLaunchPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun canPostNotification(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    /**
     * Dispatches the Azan arrival notification when a prayer time begins.
     */
    fun showAzanNotification(
        prayerName: String,
        prayerTime: String,
        locality: String,
        soundId: String
    ) {
        if (!canPostNotification()) return

        val channelId = ensureChannel(soundId)
        val soundUri = getSoundUri(soundId)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Time for $prayerName Azan ($prayerTime)")
            .setContentText("It is now time for $prayerName in $locality.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(getLaunchPendingIntent())
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 250, 150, 250))
            .build()

        notificationManager.notify(NOTIFICATION_ID_AZAN, notification)
    }

    /**
     * Dispatches a warning notification before the current prayer window ends.
     */
    fun showPrayerEndingSoonNotification(
        prayerName: String,
        minutesRemaining: Int,
        endTime: String,
        soundId: String
    ) {
        if (!canPostNotification()) return

        val channelId = ensureChannel(soundId)
        val soundUri = getSoundUri(soundId)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("$prayerName Ends in $minutesRemaining Minutes")
            .setContentText("$prayerName concludes at $endTime. Complete your prayer.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(getLaunchPendingIntent())
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 200, 100, 200))
            .build()

        notificationManager.notify(NOTIFICATION_ID_ENDING_SOON + minutesRemaining, notification)
    }

    /**
     * Dispatches an immediate test notification with the currently selected sound.
     */
    fun showTestNotification(soundId: String) {
        if (!canPostNotification()) return

        val channelId = ensureChannel(soundId)
        val sound = NotificationSounds.getSoundById(soundId)
        val soundUri = getSoundUri(soundId)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Test Azan Notification")
            .setContentText("Tone: ${sound.name} (${sound.description}). Notifications are working perfectly!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(getLaunchPendingIntent())
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 250, 150, 250))
            .build()

        notificationManager.notify(NOTIFICATION_ID_TEST, notification)
    }

    /**
     * Previews/plays any sound locally in-app.
     */
    fun playPreview(soundId: String, onCompletion: () -> Unit = {}) {
        stopPreview()
        try {
            val sound = NotificationSounds.getSoundById(soundId)
            mediaPlayer = MediaPlayer.create(context, sound.rawResId).apply {
                setOnCompletionListener {
                    stopPreview()
                    onCompletion()
                }
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onCompletion()
        }
    }

    fun stopPreview() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaPlayer = null
        }
    }

    companion object {
        const val NOTIFICATION_ID_AZAN = 1001
        const val NOTIFICATION_ID_ENDING_SOON = 2000
        const val NOTIFICATION_ID_TEST = 3001
    }
}
