package com.example.sheandsoul_nick.data.remote

import com.example.sheandsoul_nick.features.auth.presentation.AuthViewModel
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://sheandsoulrender-jxkv.onrender.com"

    // Pass the ViewModel to get the token
    fun getInstance(authViewModel: AuthViewModel): ApiService {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(authViewModel)) // <-- Add the interceptor
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // <-- Use the new client with the interceptor
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiService::class.java)
    }
}