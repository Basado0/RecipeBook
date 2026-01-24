package com.example.recipebook.network

import com.example.recipebook.api.ApiKey
import com.example.recipebook.api.MealApi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule{
    private const val BASE_URL = "https://api.spoonacular.com/recipes/"
    private const val API_KEY = ApiKey.KEY
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    // Interceptor для добавления API ключа
    private val apiKeyInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val requestWithApiKey = originalRequest.newBuilder()
            .header("x-api-key", API_KEY) // Или другой заголовок, который требует API
            .build()
        chain.proceed(requestWithApiKey)
    }

    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(apiKeyInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    val api: MealApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(MealApi::class.java)
}