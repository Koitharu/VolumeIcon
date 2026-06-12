package org.koitharu.volumeicon.config

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.preference.PreferenceManager

class AppSettings(context: Context) {

    @Suppress("DEPRECATION")
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    val notificationPolicy: NotificationPolicy
        get() {
            val value = prefs.getString(KEY_NOTIFICATION_POLICY, null)
            return NotificationPolicy.entries.find { x -> x.name == value }
                ?: NotificationPolicy.ALWAYS
        }

    val iconTheme: IconTheme
        get() {
            val value = prefs.getString(KEY_ICON_THEME, null)
            return IconTheme.entries.find { x -> x.name == value }
                ?: IconTheme.SOLID
        }

    val beepPolicy: BeepPolicy
        get() {
            val value = prefs.getString(KEY_BEEPS, null)
            return BeepPolicy.entries.find { x -> x.name == value }
                ?: BeepPolicy.SYSTEM
        }

    val isNotificationForSpeakerOnly: Boolean
        get() = prefs.getBoolean(KEY_SPEAKER_ONLY, false)

    val isNotifyForMutedMusic: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                prefs.getBoolean(KEY_MUTED_MUSIC, false)

    val hideVolumeUi: Boolean
        get() = prefs.getBoolean(KEY_HIDE_UI, false)

    val onDeviceChangedActions: Set<String>
        get() = prefs.getStringSet(KEY_DEVICE_CHANGE_ACTIONS, emptySet()).orEmpty()

    fun doOnSettingsChanged(keys: Set<String>, block: AppSettings.() -> Unit): AutoCloseable {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key in keys) {
                block()
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        return AutoCloseable {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    companion object {

        const val KEY_NOTIFICATION_POLICY = "notify_policy"
        const val KEY_SPEAKER_ONLY = "speaker_only"
        const val KEY_MUTED_MUSIC = "muted_music"
        const val KEY_ICON_THEME = "icon_theme"
        const val KEY_SYSTEM_NOTIFICATIONS_SETTINGS = "system_notifications_settings"
        const val KEY_BEEPS = "beeps"
        const val KEY_HIDE_UI = "no_ui"
        const val KEY_DEVICE_CHANGE_ACTIONS = "device_change_actions"

        const val VALUE_SHOW_TOAST = "toast"
        const val VALUE_SHOW_UI = "volume_ui"
    }
}