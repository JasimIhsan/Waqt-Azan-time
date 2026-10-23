package com.waqt.app.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Madhab
import com.waqt.app.data.analytics.AnalyticsHelper
import com.waqt.app.data.calculator.PrayerCalculator
import com.waqt.app.data.location.LocationHelper
import com.waqt.app.data.notification.NotificationHelper
import com.waqt.app.data.repository.UserPreferencesRepository
import com.waqt.app.BuildConfig
import com.waqt.app.data.updater.AppUpdateInfo
import com.waqt.app.data.updater.AppUpdateManager
import com.waqt.app.domain.model.NextPrayerInfo
import com.waqt.app.domain.model.PrayerSchedule
import com.waqt.app.ui.widget.WidgetSyncManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

data class DashboardUiState(
    val isLoading: Boolean = true,
    val isSyncingLocation: Boolean = false,
    val localityName: String = "Locating…",
    val latitude: Double = 21.4225,
    val longitude: Double = 39.8262,
    val hasLocation: Boolean = false,
    val nextPrayer: NextPrayerInfo? = null,
    val schedule: PrayerSchedule? = null,
    val countdownText: String = "--:--:--",
    val errorMessage: String? = null,
    val calculationMethod: CalculationMethod = CalculationMethod.MUSLIM_WORLD_LEAGUE,
    val madhab: Madhab = Madhab.SHAFI,
    val notificationsEnabled: Boolean = true,
    val selectedSoundId: String = "zen_chime",
    val notifyAtAzanTime: Boolean = true,
    val notifyBeforeEnd30Min: Boolean = true,
    val notifyBeforeEnd15Min: Boolean = true,
    val notifyBeforeEnd5Min: Boolean = true,
    val previewPlayingSoundId: String? = null,
    val showWidgetPrompt: Boolean = false,
    val isCheckingForUpdates: Boolean = false,
    val availableUpdate: AppUpdateInfo? = null,
    val updateStatusMessage: String? = null,
    val showUpdateDialog: Boolean = false
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserPreferencesRepository(application)
    private val locationHelper = LocationHelper(application)
    private val notificationHelper = NotificationHelper(application)
    private val updateManager = AppUpdateManager()
    private val analyticsHelper = AnalyticsHelper(application)
    private var calculator = PrayerCalculator()

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null
    private var hasCheckedWidgetPrompt = false

    init {
        observePreferences()
        startCountdownTicker()

        // Auto-refresh location if permission is already granted
        if (locationHelper.hasLocationPermission()) {
            refreshLocation()
        }

        // Check for updates shortly after launch so dashboard is ready
        viewModelScope.launch {
            delay(1500)
            checkForUpdates(silent = true)
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            repository.userPreferencesFlow.collect { prefs ->
                calculator = PrayerCalculator(prefs.calculationMethod, prefs.madhab)
                val nextPrayer = calculator.getNextPrayer(prefs.latitude, prefs.longitude)
                val fullSchedule = calculator.getFullSchedule(prefs.latitude, prefs.longitude)

                val promptForFirstTime = !prefs.hasSeenWidgetPrompt && !hasCheckedWidgetPrompt
                if (!hasCheckedWidgetPrompt) {
                    hasCheckedWidgetPrompt = true
                }

                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        localityName = prefs.localityName,
                        latitude = prefs.latitude,
                        longitude = prefs.longitude,
                        hasLocation = prefs.hasLocation,
                        nextPrayer = nextPrayer,
                        schedule = fullSchedule,
                        calculationMethod = prefs.calculationMethod,
                        madhab = prefs.madhab,
                        notificationsEnabled = prefs.notificationsEnabled,
                        selectedSoundId = prefs.selectedSoundId,
                        notifyAtAzanTime = prefs.notifyAtAzanTime,
                        notifyBeforeEnd30Min = prefs.notifyBeforeEnd30Min,
                        notifyBeforeEnd15Min = prefs.notifyBeforeEnd15Min,
                        notifyBeforeEnd5Min = prefs.notifyBeforeEnd5Min,
                        showWidgetPrompt = if (promptForFirstTime) true else current.showWidgetPrompt
                    )
                }
            }
        }
    }

    private fun startCountdownTicker() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (isActive) {
                val next = _uiState.value.nextPrayer
                if (next != null) {
                    val now = System.currentTimeMillis()
                    val target = next.time.time
                    val diff = target - now

                    if (diff <= 0) {
                        // Prayer transition arrived! Recompute
                        val prefs = repository.getUserPreferences()
                        val updatedNext = calculator.getNextPrayer(prefs.latitude, prefs.longitude)
                        val updatedSchedule = calculator.getFullSchedule(prefs.latitude, prefs.longitude)

                        _uiState.update {
                            it.copy(
                                nextPrayer = updatedNext,
                                schedule = updatedSchedule,
                                countdownText = "00:00:00"
                            )
                        }
                        WidgetSyncManager.syncAndSchedule(getApplication())
                    } else {
                        val hours = (diff / (1000 * 60 * 60))
                        val minutes = (diff / (1000 * 60)) % 60
                        val seconds = (diff / 1000) % 60
                        val formatted = String.format(Locale.getDefault(), "%02dh %02dm %02ds", hours, minutes, seconds)
                        _uiState.update { it.copy(countdownText = formatted) }
                    }
                }
                delay(1000L)
            }
        }
    }

    fun onPermissionResult(granted: Boolean) {
        if (granted) {
            refreshLocation()
            locationHelper.startDisplacementTracking()
        } else {
            _uiState.update {
                it.copy(errorMessage = "Location permission is required for accurate prayer times.")
            }
        }
    }

    fun refreshLocation() {
        viewModelScope.launch {
            // Also check for new app release on dashboard refresh
            checkForUpdates(silent = true)
            _uiState.update { it.copy(isSyncingLocation = true, errorMessage = null) }

            val location = locationHelper.getCurrentLocation()
            if (location != null) {
                val locality = locationHelper.getLocalityName(location.latitude, location.longitude)
                repository.saveLocation(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    locality = locality
                )

                // Arm displacement tracking, sync glance widget, and schedule alarms
                locationHelper.startDisplacementTracking()
                WidgetSyncManager.syncAndSchedule(getApplication())

                _uiState.update { it.copy(isSyncingLocation = false) }
            } else {
                _uiState.update {
                    it.copy(
                        isSyncingLocation = false,
                        errorMessage = if (!locationHelper.hasLocationPermission()) {
                            "Location permission needed for automatic prayer times."
                        } else {
                            "Could not acquire fresh GPS fix. Using last saved location."
                        }
                    )
                }
            }
        }
    }

    fun updateCalculationMethod(method: CalculationMethod) {
        analyticsHelper.logCalculationMethodChanged(method.name)
        viewModelScope.launch {
            val currentMadhab = _uiState.value.madhab
            repository.saveCalculationSettings(method, currentMadhab)
            WidgetSyncManager.syncAndSchedule(getApplication())
        }
    }

    fun updateMadhab(madhab: Madhab) {
        analyticsHelper.logMadhabChanged(madhab.name)
        viewModelScope.launch {
            val currentMethod = _uiState.value.calculationMethod
            repository.saveCalculationSettings(currentMethod, madhab)
            WidgetSyncManager.syncAndSchedule(getApplication())
        }
    }

    // ==========================================
    // NOTIFICATION CONTROLS
    // ==========================================

    fun setNotificationsEnabled(enabled: Boolean) {
        analyticsHelper.logNotificationToggled("master", enabled)
        viewModelScope.launch {
            repository.setNotificationsEnabled(enabled)
            WidgetSyncManager.syncAndSchedule(getApplication())
        }
    }

    fun selectNotificationSound(soundId: String) {
        analyticsHelper.logToneSelected("all", soundId)
        viewModelScope.launch {
            repository.setSelectedSoundId(soundId)
            WidgetSyncManager.syncAndSchedule(getApplication())
        }
    }

    fun updateNotificationAlertPreferences(
        atAzan: Boolean,
        before30: Boolean,
        before15: Boolean,
        before5: Boolean
    ) {
        viewModelScope.launch {
            repository.setNotificationAlertPreferences(atAzan, before30, before15, before5)
            WidgetSyncManager.syncAndSchedule(getApplication())
        }
    }

    fun sendTestNotification() {
        notificationHelper.showTestNotification(_uiState.value.selectedSoundId)
    }

    fun playTonePreview(soundId: String) {
        if (_uiState.value.previewPlayingSoundId == soundId) {
            notificationHelper.stopPreview()
            _uiState.update { it.copy(previewPlayingSoundId = null) }
        } else {
            _uiState.update { it.copy(previewPlayingSoundId = soundId) }
            notificationHelper.playPreview(soundId) {
                _uiState.update { it.copy(previewPlayingSoundId = null) }
            }
        }
    }

    fun stopTonePreview() {
        notificationHelper.stopPreview()
        _uiState.update { it.copy(previewPlayingSoundId = null) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun dismissWidgetPrompt() {
        _uiState.update { it.copy(showWidgetPrompt = false) }
        viewModelScope.launch {
            repository.setHasSeenWidgetPrompt(true)
        }
    }

    fun showWidgetPrompt() {
        _uiState.update { it.copy(showWidgetPrompt = true) }
    }

    fun checkForUpdates(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) {
                _uiState.update { it.copy(isCheckingForUpdates = true, updateStatusMessage = "Checking GitHub for updates...") }
            }
            val result = updateManager.checkForUpdates()
            result.fold(
                onSuccess = { info ->
                    analyticsHelper.logUpdateCheck(info.isUpdateAvailable, info.latestVersion)
                    _uiState.update { current ->
                        current.copy(
                            isCheckingForUpdates = false,
                            availableUpdate = info,
                            showUpdateDialog = if (info.isUpdateAvailable) true else current.showUpdateDialog,
                            updateStatusMessage = if (info.isUpdateAvailable) {
                                "Update available: v${info.latestVersion}"
                            } else {
                                "You're on the latest version (v${info.currentVersion})"
                            }
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { current ->
                        current.copy(
                            isCheckingForUpdates = false,
                            updateStatusMessage = if (!silent) "Failed to check: ${error.localizedMessage}" else current.updateStatusMessage
                        )
                    }
                }
            )
        }
    }

    fun dismissUpdateDialog() {
        _uiState.update { it.copy(showUpdateDialog = false) }
    }

    fun showUpdateDialog() {
        if (_uiState.value.availableUpdate != null) {
            _uiState.update { it.copy(showUpdateDialog = true) }
        } else {
            checkForUpdates(silent = false)
        }
    }

    fun simulateUpdateForTesting() {
        val testUpdate = AppUpdateInfo(
            isUpdateAvailable = true,
            currentVersion = BuildConfig.VERSION_NAME,
            latestVersion = "1.1.0",
            releaseTitle = "Waqt v1.1.0 Release",
            releaseNotes = "• Beautiful interactive home screen widget with quick action\n• Dedicated notifications and 10 custom ambient audio tones\n• Automated background location tracking\n• Seamless in-app update installer directly from dashboard",
            downloadUrl = "https://github.com/JasimIhsan/Waqt/releases/download/v1.1.0/waqt-v1.1.0.apk",
            apkFileName = "waqt-v1.1.0.apk",
            releasePageUrl = "https://github.com/JasimIhsan/Waqt/releases",
            publishedAt = "Just now"
        )
        _uiState.update {
            it.copy(
                availableUpdate = testUpdate,
                showUpdateDialog = true,
                updateStatusMessage = "Update available: v1.1.0"
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        notificationHelper.stopPreview()
    }
}
