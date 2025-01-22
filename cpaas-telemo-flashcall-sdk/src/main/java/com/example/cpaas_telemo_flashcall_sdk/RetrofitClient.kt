package com.example.cpaas_telemo_flashcall_sdk

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private var baseUrl: String = "https://default.url/" // Default Base URL
    private var token: String? = null // Default token, can be updated dynamically

    // Logging interceptor
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Authorization interceptor
    private val authInterceptor = Interceptor { chain ->
        val original: Request = chain.request()
        val requestBuilder = original.newBuilder()
            .header("Accept", "application/json")

        // Add Authorization header dynamically
        if (!token.isNullOrEmpty()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        chain.proceed(requestBuilder.build())
    }

    // OkHttpClient with interceptors
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // Retrofit instance
    private var retrofit: Retrofit? = null

    // Public API Service
    val apiService: ApiService
        get() = getRetrofitInstance().create(ApiService::class.java)

    /**
     * Returns the Retrofit instance, reinitializing if the base URL changes.
     */
    private fun getRetrofitInstance(): Retrofit {
        if (retrofit == null || retrofit?.baseUrl().toString() != baseUrl) {
            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()
        }
        return retrofit!!
    }

    /**
     * Updates the base URL dynamically.
     * Must be called before making API calls.
     */
    fun setBaseUrl(newBaseUrl: String) {
        baseUrl = newBaseUrl
        retrofit = null // Reset Retrofit instance to reflect the new base URL
    }

    /**
     * Updates the token dynamically.
     * Must be called before making API calls.
     */
    fun setToken(newToken: String?) {
        token = newToken
    }
}