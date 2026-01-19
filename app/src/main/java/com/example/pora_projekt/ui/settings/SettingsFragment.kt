package com.example.pora_projekt.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.example.pora_projekt.R
import com.example.pora_projekt.mqtt.MqttSender


class SettingsFragment : PreferenceFragmentCompat() {
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())

        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)


    }

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
        when(key) {
            "username", "password" -> {
                MqttSender.setCredentials(
                    sharedPreferences.getString("username", "") ?: "",
                    sharedPreferences.getString("password", "") ?: ""
                )
                MqttSender.restart()
            }
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
    }
}