@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package org.koitharu.volumeicon.config

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceActivity
import android.preference.PreferenceGroup
import android.preference.PreferenceScreen
import org.koitharu.volumeicon.NotificationHolder
import org.koitharu.volumeicon.R
import org.koitharu.volumeicon.config.AppSettings.Companion.KEY_SYSTEM_NOTIFICATIONS_SETTINGS

@SuppressLint("ExportedPreferenceActivity")
class SettingsActivity : PreferenceActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.pref_root)
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        preferenceScreen.bindSummary()
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
    }

    private fun Preference.bindSummary() {
        when (this) {
            is ListPreference -> {
                val valueIndex = entryValues.indexOf(value)
                summary = entries.getOrNull(valueIndex)
            }
            is PreferenceGroup -> repeat(preferenceCount) { i ->
                getPreference(i).bindSummary()
            }
        }
    }
}