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

    fun doOnNotificationPolicyChanged(block: (NotificationPolicy) -> Unit): AutoCloseable {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_NOTIFICATION_POLICY) {
                block(notificationPolicy)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        return AutoCloseable {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    companion object {

        const val KEY_NOTIFICATION_POLICY = "notify_policy"
    }
}