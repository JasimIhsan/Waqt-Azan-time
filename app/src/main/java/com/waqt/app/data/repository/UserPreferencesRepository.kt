package com.waqt.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Madhab
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "azan_user_preferences")

data class UserPreferences(
    val latitude: Double = 21.4225, // Default to Mecca
    val longitude: Double = 39.8262,
    val localityName: String = "Mecca",
    val hasLocation: Boolean = false,
    val calculationMethod: CalculationMethod = CalculationMethod.MUSLIM_WORLD_LEAGUE,
    val madhab: Madhab = Madhab.SHAFI,
    val notificationsEnabled: Boolean = true,
    val selectedSoundId: String = "zen_chime",
    val notifyAtAzanTime: Boolean = true,
    val notifyBeforeEnd30Min: Boolean = true,
    val notifyBeforeEnd15Min: Boolean = true,
    val notifyBeforeEnd5Min: Boolean = true,
    val hasSeenWidgetPrompt: Boolean = false
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val KEY_LATITUDE = doublePreferencesKey("key_latitude")
        val KEY_LONGITUDE = doublePreferencesKey("key_longitude")
        val KEY_LOCALITY_NAME = stringPreferencesKey("key_locality_name")
        val KEY_HAS_LOCATION = booleanPreferencesKey("key_has_location")
        val KEY_CALC_METHOD = stringPreferencesKey("key_calc_method")
        val KEY_MADHAB = stringPreferencesKey("key_madhab")
        val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("key_notifications_enabled")
        val KEY_SELECTED_SOUND_ID = stringPreferencesKey("key_selected_sound_id")
        val KEY_NOTIFY_AT_AZAN = booleanPreferencesKey("key_notify_at_azan")
        val KEY_NOTIFY_BEFORE_30 = booleanPreferencesKey("key_notify_before_30")
        val KEY_NOTIFY_BEFORE_15 = booleanPreferencesKey("key_notify_before_15")
        val KEY_NOTIFY_BEFORE_5 = booleanPreferencesKey("key_notify_before_5")
        val KEY_HAS_SEEN_WIDGET_PROMPT = booleanPreferencesKey("key_has_seen_widget_prompt")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val lat = preferences[PreferencesKeys.KEY_LATITUDE] ?: 21.4225
            val lng = preferences[PreferencesKeys.KEY_LONGITUDE] ?: 39.8262
            val locality = preferences[PreferencesKeys.KEY_LOCALITY_NAME] ?: "Mecca"
            val hasLocation = preferences[PreferencesKeys.KEY_HAS_LOCATION] ?: false
            val calcMethodName = preferences[PreferencesKeys.KEY_CALC_METHOD]
                ?: CalculationMethod.MUSLIM_WORLD_LEAGUE.name
            val madhabName = preferences[PreferencesKeys.KEY_MADHAB] ?: Madhab.SHAFI.name

            val notificationsEnabled = preferences[PreferencesKeys.KEY_NOTIFICATIONS_ENABLED] ?: true
            val selectedSoundId = preferences[PreferencesKeys.KEY_SELECTED_SOUND_ID] ?: "zen_chime"
            val notifyAtAzan = preferences[PreferencesKeys.KEY_NOTIFY_AT_AZAN] ?: true
            val notifyBefore30 = preferences[PreferencesKeys.KEY_NOTIFY_BEFORE_30] ?: true
            val notifyBefore15 = preferences[PreferencesKeys.KEY_NOTIFY_BEFORE_15] ?: true
            val notifyBefore5 = preferences[PreferencesKeys.KEY_NOTIFY_BEFORE_5] ?: true
            val hasSeenWidgetPrompt = preferences[PreferencesKeys.KEY_HAS_SEEN_WIDGET_PROMPT] ?: false

            val calcMethod = try {
                CalculationMethod.valueOf(calcMethodName)
            } catch (e: Exception) {
                CalculationMethod.MUSLIM_WORLD_LEAGUE
            }

            val madhab = try {
                Madhab.valueOf(madhabName)
            } catch (e: Exception) {
                Madhab.SHAFI
            }

            UserPreferences(
                latitude = lat,
                longitude = lng,
                localityName = locality,
                hasLocation = hasLocation,
                calculationMethod = calcMethod,
                madhab = madhab,
                notificationsEnabled = notificationsEnabled,
                selectedSoundId = selectedSoundId,
                notifyAtAzanTime = notifyAtAzan,
                notifyBeforeEnd30Min = notifyBefore30,
                notifyBeforeEnd15Min = notifyBefore15,
                notifyBeforeEnd5Min = notifyBefore5,
                hasSeenWidgetPrompt = hasSeenWidgetPrompt
            )
        }

    suspend fun getUserPreferences(): UserPreferences {
        return userPreferencesFlow.first()
    }

    suspend fun saveLocation(latitude: Double, longitude: Double, locality: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_LATITUDE] = latitude
            preferences[PreferencesKeys.KEY_LONGITUDE] = longitude
            preferences[PreferencesKeys.KEY_LOCALITY_NAME] = locality
            preferences[PreferencesKeys.KEY_HAS_LOCATION] = true
        }
    }

    suspend fun saveCalculationSettings(method: CalculationMethod, madhab: Madhab) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_CALC_METHOD] = method.name
            preferences[PreferencesKeys.KEY_MADHAB] = madhab.name
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setSelectedSoundId(soundId: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_SELECTED_SOUND_ID] = soundId
        }
    }

    suspend fun setNotificationAlertPreferences(
        atAzan: Boolean,
        before30: Boolean,
        before15: Boolean,
        before5: Boolean
    ) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_NOTIFY_AT_AZAN] = atAzan
            preferences[PreferencesKeys.KEY_NOTIFY_BEFORE_30] = before30
            preferences[PreferencesKeys.KEY_NOTIFY_BEFORE_15] = before15
            preferences[PreferencesKeys.KEY_NOTIFY_BEFORE_5] = before5
        }
    }

    suspend fun setHasSeenWidgetPrompt(seen: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_HAS_SEEN_WIDGET_PROMPT] = seen
        }
    }
}
