package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.FontScaleOption
import com.example.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.uniSyncDataStore: DataStore<Preferences> by preferencesDataStore(name = "unisync_prefs")

data class LocalUserSettings(
    val languageCode: String = "en",
    val darkMode: Boolean = false,
    val fontScale: FontScaleOption = FontScaleOption.MEDIUM,
    val role: UserRole = UserRole.STUDENT,
    val aouBranch: String = "Riyadh (الرياض) — Saudi Arabia",
    val selectedMajorId: String = "cs",
    val companionGender: String = "girl",
    val profilePhotoUri: String = "",
    val alarmRingtoneId: String = "chime",
    val alarmVolume: Float = 0.85f,
    val totalPomodoroMinutes: Int = 0
)

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val LANGUAGE = stringPreferencesKey("language_code")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val FONT_SCALE = stringPreferencesKey("font_scale")
        val ROLE = stringPreferencesKey("user_role")
        val BRANCH = stringPreferencesKey("aou_branch")
        val MAJOR_ID = stringPreferencesKey("selected_major_id")
        val COMPANION = stringPreferencesKey("companion_gender")
        val PROFILE_PHOTO_URI = stringPreferencesKey("profile_photo_uri")
        val RINGTONE_ID = stringPreferencesKey("alarm_ringtone_id")
        val ALARM_VOLUME = floatPreferencesKey("alarm_volume")
        val POMODORO_MINUTES = intPreferencesKey("total_pomodoro_minutes")
    }

    val settingsFlow: Flow<LocalUserSettings> = context.uniSyncDataStore.data.map { prefs ->
        LocalUserSettings(
            languageCode = prefs[Keys.LANGUAGE] ?: "en",
            darkMode = prefs[Keys.DARK_MODE] ?: false,
            fontScale = FontScaleOption.fromKey(prefs[Keys.FONT_SCALE]),
            role = UserRole.fromKey(prefs[Keys.ROLE]),
            aouBranch = prefs[Keys.BRANCH] ?: "Riyadh (الرياض) — Saudi Arabia",
            selectedMajorId = prefs[Keys.MAJOR_ID] ?: "cs",
            companionGender = prefs[Keys.COMPANION] ?: "girl",
            profilePhotoUri = prefs[Keys.PROFILE_PHOTO_URI] ?: "",
            alarmRingtoneId = prefs[Keys.RINGTONE_ID] ?: "chime",
            alarmVolume = prefs[Keys.ALARM_VOLUME] ?: 0.85f,
            totalPomodoroMinutes = prefs[Keys.POMODORO_MINUTES] ?: 0
        )
    }

    suspend fun setLanguage(code: String) {
        context.uniSyncDataStore.edit { it[Keys.LANGUAGE] = code }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.uniSyncDataStore.edit { it[Keys.DARK_MODE] = enabled }
    }

    suspend fun setFontScale(option: FontScaleOption) {
        context.uniSyncDataStore.edit { it[Keys.FONT_SCALE] = option.key }
    }

    suspend fun setRole(role: UserRole) {
        context.uniSyncDataStore.edit { it[Keys.ROLE] = role.key }
    }

    suspend fun setBranch(branch: String) {
        context.uniSyncDataStore.edit { it[Keys.BRANCH] = branch }
    }

    suspend fun setSelectedMajor(majorId: String) {
        context.uniSyncDataStore.edit { it[Keys.MAJOR_ID] = majorId }
    }

    suspend fun setCompanionGender(gender: String) {
        context.uniSyncDataStore.edit { it[Keys.COMPANION] = gender }
    }

    suspend fun setProfilePhotoUri(uri: String) {
        context.uniSyncDataStore.edit { it[Keys.PROFILE_PHOTO_URI] = uri }
    }

    suspend fun setAlarmSettings(ringtoneId: String, volume: Float) {
        context.uniSyncDataStore.edit {
            it[Keys.RINGTONE_ID] = ringtoneId
            it[Keys.ALARM_VOLUME] = volume.coerceIn(0f, 1f)
        }
    }

    suspend fun addPomodoroMinutes(minutes: Int) {
        context.uniSyncDataStore.edit {
            val prev = it[Keys.POMODORO_MINUTES] ?: 0
            it[Keys.POMODORO_MINUTES] = prev + minutes
        }
    }
}
