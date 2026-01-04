package com.example.pora_projekt.ui.gforce

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GforceViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "G-force"
    }
    val text: LiveData<String> = _text
}