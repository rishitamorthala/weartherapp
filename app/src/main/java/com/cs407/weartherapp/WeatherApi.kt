//retrofit interface for making GET requests based on date, time, & loc
package com.cs407.weartherapp

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface WeatherApi {
    @GET("{datetime}/{latitude},{longitude}/t_2m:C/json")
    fun getTemperature(
        @Path("datetime") datetime: String,
        @Path("latitude") latitude: Double,
        @Path("longitude") longitude: Double,
        @Header("Authorization") authHeader: String
    ): Call<WeatherResponse>
}