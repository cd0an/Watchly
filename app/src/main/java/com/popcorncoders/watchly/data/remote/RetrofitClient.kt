package com.popcorncoders.watchly.data.remote

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Singleton object to provide a single instance of Retrofit across the app
object RetrofitClient {
    private const val BASE_URL = "https://api.themoviedb.org/3/" // Base URL for all TMDb API requests
    private const val API_KEY = "8722193d9837f70bd611fb987c977f33"

    // Interceptor auto attaches the API key to every outgoing request
    private val interceptor = Interceptor { chain ->
        val original = chain.request()
        val url = original.url().newBuilder()
            .addQueryParameter("api_key", API_KEY) // Adds api_key to every request
            .build()
        chain.proceed(original.newBuilder().url(url).build()) // Sends the modified request
    }

    // OkHttpClient with the interceptor attached
    private val client = OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .build()

    // Lazy initialization ensures Retrofit is only created when needed
    val api: ApiService by lazy {
        Retrofit.Builder() // Build the Retrofit instance
            .baseUrl(BASE_URL) // Set the base URL
            .client(client)
            .addConverterFactory(GsonConverterFactory.create()) // Converts JSON responses into Kotlin data classes
            .build()
            .create(ApiService::class.java) // Creates an implementation of ApiService interface
    }
}

