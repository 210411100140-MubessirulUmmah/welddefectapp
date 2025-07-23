package com.example.welddefectdetector


import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @Multipart
    @POST("detect")
    fun uploadImage(
        @Part file: MultipartBody.Part
    ): Call<DetectionResponse>
}


