package com.waqt.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.waqt.app.data.location.LocationHelper
import com.waqt.app.ui.about.AboutScreen
import com.waqt.app.ui.dashboard.DashboardScreen
import com.waqt.app.ui.dashboard.DashboardViewModel
import com.waqt.app.ui.settings.NotificationSettingsScreen
import com.waqt.app.ui.settings.SettingsScreen
import androidx.compose.runtime.LaunchedEffect
import com.waqt.app.data.analytics.AnalyticsHelper
import com.waqt.app.ui.theme.AzanTheme
import com.waqt.app.ui.updater.UpdateDialog

enum class AppScreen {
    DASHBOARD,
    SETTINGS,
    NOTIFICATION_SETTINGS,
    ABOUT
}

class MainActivity : ComponentActivity() {

    private val viewModel: DashboardViewModel by viewModels()
    private val analyticsHelper by lazy { AnalyticsHelper(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AzanTheme {
                val uiState by viewModel.uiState.collectAsState()
                var currentScreen by rememberSaveable { mutableStateOf(AppScreen.DASHBOARD) }

                LaunchedEffect(currentScreen) {
                    analyticsHelper.logScreenView(currentScreen.name.lowercase())
                }

                BackHandler(enabled = currentScreen != AppScreen.DASHBOARD) {
                    currentScreen = when (currentScreen) {
                        AppScreen.NOTIFICATION_SETTINGS -> AppScreen.SETTINGS
                        else -> AppScreen.DASHBOARD
                    }
                }

                if (uiState.showUpdateDialog && uiState.availableUpdate != null) {
                    UpdateDialog(
                        updateInfo = uiState.availableUpdate!!,
                        onDismiss = { viewModel.dismissUpdateDialog() }
                    )
                }

                when (currentScreen) {
                    AppScreen.DASHBOARD -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToSettings = { currentScreen = AppScreen.SETTINGS },
                        onNavigateToAbout = { currentScreen = AppScreen.ABOUT }
                    )
                    AppScreen.SETTINGS -> SettingsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = AppScreen.DASHBOARD },
                        onNavigateToNotifications = { currentScreen = AppScreen.NOTIFICATION_SETTINGS }
                    )
                    AppScreen.NOTIFICATION_SETTINGS -> NotificationSettingsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = AppScreen.SETTINGS }
                    )
                    AppScreen.ABOUT -> AboutScreen(
                        onNavigateBack = { currentScreen = AppScreen.DASHBOARD }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val locationHelper = LocationHelper(this)
        if (locationHelper.hasLocationPermission()) {
            locationHelper.startDisplacementTracking()
        }
    }
}
