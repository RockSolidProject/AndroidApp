package com.example.pora_projekt.ui.camera

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pora_projekt.api.Hold
import com.example.pora_projekt.api.RetrofitClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class CameraViewModel : ViewModel() {
    private val _holdsResult = MutableLiveData<Pair<File, List<Hold>>?>()
    val holdsResult: LiveData<Pair<File, List<Hold>>?> = _holdsResult

    private val _statusText = MutableLiveData<String>().apply {
        value = "Ready to capture photo"
    }
    val statusText: LiveData<String> = _statusText

    private val _uploadEnabled = MutableLiveData<Boolean>().apply {
        value = false
    }
    val uploadEnabled: LiveData<Boolean> = _uploadEnabled

    private var capturedPhotoFile: File? = null

    fun setPhotoFile(file: File) {
        capturedPhotoFile = file
        _statusText.value = "Photo captured: ${file.name}"
        _uploadEnabled.value = true
    }

    fun uploadPhoto() {
        val file = capturedPhotoFile ?: run {
            _statusText.value = "No photo to upload"
            return
        }

        if (!file.exists()) {
            _statusText.value = "Photo file not found"
            return
        }

        _statusText.value = "Uploading..."
        _uploadEnabled.value = false

        val requestFile = file.asRequestBody("image/jpeg".toMediaType())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        RetrofitClient.instance.uploadPhoto(body).enqueue(object : Callback<com.example.pora_projekt.api.UploadResponse> {
            override fun onResponse(call: Call<com.example.pora_projekt.api.UploadResponse>, response: Response<com.example.pora_projekt.api.UploadResponse>) {
                if (response.isSuccessful) {
                    _statusText.value = "Upload successful!"
                    val uploadResponse = response.body()
                    Log.d("CameraViewModel", "Upload response: $uploadResponse")
                    if (uploadResponse != null && capturedPhotoFile != null) {
                        _holdsResult.value = Pair(capturedPhotoFile!!, uploadResponse.holds)
                        _holdsResult.value = null
                    }
                    capturedPhotoFile = null
                    _uploadEnabled.value = false
                } else {
                    _statusText.value = "Upload failed: ${response.code()}"
                    _uploadEnabled.value = true
                }
            }

            override fun onFailure(call: Call<com.example.pora_projekt.api.UploadResponse>, t: Throwable) {
                _statusText.value = "Upload error: ${t.message}"
                _uploadEnabled.value = true
            }
        })
    }
}