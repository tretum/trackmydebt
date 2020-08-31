package com.mmutert.trackmydebt.ui.settings

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.mmutert.trackmydebt.R
import com.mmutert.trackmydebt.util.ThemeHelper

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        val nightModePref = findPreference<ListPreference>(getString(R.string.pref_night_mode_key))
        if (nightModePref != null) {
            nightModePref.onPreferenceChangeListener =
                    Preference.OnPreferenceChangeListener { _: Preference, newValue: Any ->
                        ThemeHelper.applyTheme(newValue as String, requireContext())
                        true
                    }
        }
    }
}