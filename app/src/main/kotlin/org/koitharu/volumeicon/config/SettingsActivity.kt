@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package org.koitharu.volumeicon.config

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.ListPreference
import android.preference.MultiSelectListPreference
import android.preference.Preference
import android.preference.PreferenceActivity
import android.preference.PreferenceGroup
import android.preference.PreferenceScreen
import org.koitharu.volumeicon.NotificationHolder
import org.koitharu.volumeicon.R
import org.koitharu.volumeicon.VolumeIconService
import org.koitharu.volumeicon.config.AppSettings.Companion.KEY_ICON_THEME
import org.koitharu.volumeicon.config.AppSettings.Companion.KEY_MUTED_MUSIC
import org.koitharu.volumeicon.config.AppSettings.Companion.KEY_NOTIFICATION_POLICY
import org.koitharu.volumeicon.config.AppSettings.Companion.KEY_SPEAKER_ONLY
import org.koitharu.volumeicon.config.AppSettings.Companion.KEY_SYSTEM_NOTIFICATIONS_SETTINGS
import org.koitharu.volumeicon.config.NotificationPolicy.ALWAYS
import org.koitharu.volumeicon.config.NotificationPolicy.NEVER
import org.koitharu.volumeicon.config.NotificationPolicy.NOT_MUTED

@SuppressLint("ExportedPreferenceActivity")
class SettingsActivity : PreferenceActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.pref_main)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        preferenceScreen.bindSummary()
        updateDependencies()
    }

    override fun onResume() {
        super.onResume()
        actionBar?.subtitle = if (VolumeIconService.isActive(this)) {
            null
        } else {
            getString(R.string.service_is_not_running)
        }
    }

    override fun onDestroy() {
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    override fun onPreferenceTreeClick(
        preferenceScreen: PreferenceScreen?,
        preference: Preference?
    ): Boolean = when (preference?.key) {
        KEY_SYSTEM_NOTIFICATIONS_SETTINGS -> {
            startActivity(NotificationHolder.settingsIntent(this))
            true
        }

        else -> super.onPreferenceTreeClick(preferenceScreen, preference)
    }

    override fun onSharedPreferenceChanged(
        sharedPreferences: SharedPreferences?,
        key: String?
    ) {
        findPreference(key)?.bindSummary()
        updateDependencies()
    }

    private fun Preference.bindSummary() {
        when (this) {
            is PreferenceGroup -> repeat(preferenceCount) { i ->
                getPreference(i).bindSummary()
            }

            is ListPreference -> {
                val valueIndex = entryValues.indexOf(value)
                summary = entries.getOrNull(valueIndex)
            }

            is MultiSelectListPreference -> {
                summary = values.mapNotNull { value ->
                    val valueIndex = entryValues.indexOf(value)
                    entries.getOrNull(valueIndex)
                }.joinToString().ifEmpty { getString(R.string.none) }
            }
        }
    }

    private fun updateDependencies() {
        val notificationPolicy = preferenceManager.sharedPreferences.getString(
            KEY_NOTIFICATION_POLICY, ALWAYS.name
        )?.let { raw -> NotificationPolicy.entries.find { it.name == raw } }
        val isNotificationsOn = notificationPolicy != NEVER
        findPreference(KEY_SPEAKER_ONLY)?.isEnabled = isNotificationsOn
        findPreference(KEY_ICON_THEME)?.isEnabled = isNotificationsOn
        findPreference(KEY_MUTED_MUSIC)?.isEnabled =
            notificationPolicy == NEVER || notificationPolicy == NOT_MUTED
    }
}