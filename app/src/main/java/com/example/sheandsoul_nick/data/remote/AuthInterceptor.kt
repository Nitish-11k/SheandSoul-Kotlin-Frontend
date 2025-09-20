package com.example.sheandsoul_nick.data.remote

import com.example.sheandsoul_nick.features.auth.presentation.AuthViewModel
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val viewModel: AuthViewModel) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = viewModel.token

        // If a token exists, add it to the request header
        val newRequest = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }
        return chain.proceed(newRequest)
    }
}