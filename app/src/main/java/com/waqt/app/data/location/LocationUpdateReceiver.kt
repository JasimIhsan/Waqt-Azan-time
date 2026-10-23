package com.waqt.app.data.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.waqt.app.data.repository.UserPreferencesRepository
import com.waqt.app.ui.widget.WidgetSyncManager
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver triggered by FusedLocationProviderClient when the device
 * displaces by >= 5 km. Updates stored coordinates, reverse-geocodes city,
 * refreshes Glance widget, and recalibrates the next prayer alarm.
 */
class LocationUpdateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return

        val locationResult = LocationResult.extractResult(intent) ?: return
        val location = locationResult.lastLocation ?: return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val locationHelper = LocationHelper(context)
                val localityName = locationHelper.getLocalityName(
                    location.latitude,
                    location.longitude
                )

                // 1. Persist updated location to DataStore
                val repo = UserPreferencesRepository(context)
                repo.saveLocation(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    locality = localityName
                )

                // 2. Recalculate next prayer, refresh Glance widget, and arm new alarm
                WidgetSyncManager.syncAndSchedule(context)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_LOCATION_UPDATE = "com.waqt.app.ACTION_LOCATION_UPDATE"
    }
}
