@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package org.koitharu.volumeicon.config

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceActivity
import org.koitharu.volumeicon.R
import org.koitharu.volumeicon.config.AppSettings.Companion.KEY_NOTIFICATION_POLICY

@SuppressLint("ExportedPreferenceActivity")
class SettingsActivity : PreferenceActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.pref_root)
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        findPreference(KEY_NOTIFICATION_POLICY)?.bindSummary()
    }

    override fun onDestroy() {
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    override fun onSharedPreferenceChanged(
        sharedPreferences: SharedPreferences?,
        key: String?
    ) {
        when (key) {
            KEY_NOTIFICATION_POLICY -> findPreference(key)?.bindSummary()
        }
    }

    private fun Preference.bindSummary() {
        when (this) {
            is ListPreference -> {
                val valueIndex = entryValues.indexOf(value)
                summary = entries.getOrNull(valueIndex)
            }
        }
    }
}