package com.example.pora_projekt.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Settings"
    }
    fun updateStatus(message: String) {
        _text.value = message
    }
    val text: LiveData<String> = _text
}