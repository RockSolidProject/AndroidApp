package com.example.pora_projekt.api

import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.Call

interface PhotoApiService {
    @Multipart
    @POST("/detect-holds")
    fun uploadPhoto(@Part photo: MultipartBody.Part): Call<UploadResponse>
}

data class UploadResponse(
    val holds : List<Hold>
)

data class Hold(
    val position: List<Int>,
    val size: List<Int>
)

