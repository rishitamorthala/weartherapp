package com.cs407.weartherapp

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface WeatherApi {
    @GET("{datetime}/t_2m:F/{latitude},{longitude}/json")
    fun getTemperature(
        @Path("datetime") datetime: String,
        @Path("latitude") latitude: Double,
        @Path("longitude") longitude: Double,
        @Query("model") model: String = "mix", // Optional: Specify a model
        @Header("Authorization") authHeader: String
    ): Call<WeatherResponse>
}