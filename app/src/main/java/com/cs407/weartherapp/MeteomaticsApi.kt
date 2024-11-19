//the api we're using: https://www.meteomatics.com/en/weather-api/#about
package com.cs407.weartherapp

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MeteomaticsApi {
    @GET("/{datetime}/{latitude},{longitude}/t_2m:C")
    fun getTemperature(
        @Path("datetime") datetime: String,
        @Path("latitude") latitude: Double,
        @Path("longitude") longitude: Double,
        @Query("username") username: String,
        @Query("password") password: String
    ): Call<WeatherResponse>
}