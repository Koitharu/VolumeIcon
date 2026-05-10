package org.koitharu.volumeicon.config

import android.content.Context
import android.content.SharedPreferences
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
        const val KEY_ICON_THEME = "icon_theme"
        const val KEY_SYSTEM_NOTIFICATIONS_SETTINGS = "system_notifications_settings"
    }
}