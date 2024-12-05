package com.example.newkotlinconnector

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val errorInterceptor = Interceptor { chain ->
        val response = chain.proceed(chain.request())
        if (!response.isSuccessful) {
            throw Exception("HTTP error: ${response.code} - ${response.message}")
        }
        response
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(errorInterceptor)
        .callTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val api: ParametersAPIInterface by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.meteomatics.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
            .create(ParametersAPIInterface::class.java)
    }
}
