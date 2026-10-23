package com.waqt.app.ui.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.waqt.app.data.location.LocationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Glance receiver responsible for handling AppWidget lifecycle broadcasts.
 */
class WaqtWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = WaqtWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        // First instance placed on the launcher
        CoroutineScope(Dispatchers.IO).launch {
            WidgetSyncManager.syncAndSchedule(context)
            val locationHelper = LocationHelper(context)
            if (locationHelper.hasLocationPermission()) {
                locationHelper.startDisplacementTracking()
            }
        }
    }
}
