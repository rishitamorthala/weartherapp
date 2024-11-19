package com.cs407.weartherapp

import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cs407.weartherapp.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var weatherText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        weatherText = findViewById(R.id.weather_text)

        // Call fetchMadisonWeather to display the weather
        fetchMadisonWeather()
    }

    private fun fetchMadisonWeather() {
        val username = "school_morthala_rishita"
        val password = "Y40Iu42uWd"
        val credentials = "$username:$password"
        val authHeader = "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)

        // Use fixed datetime for debugging
        val now = "2024-11-18T20:05:00.000-06:00"
        val latitude = 43.074761 // Full precision coordinates
        val longitude = -89.3837613 // Full precision coordinates
        val model = "mix" // Optional model parameter

        Log.d("MainActivity", "Formatted datetime: $now")
        Log.d("MainActivity", "Calling URL: https://api.meteomatics.com/$now/t_2m:F/$latitude,$longitude/json?model=$model")
        Log.d("MainActivity", "Authorization Header: $authHeader")

        // Pass the `authHeader` parameter correctly
        val call = RetrofitClient.instance.getTemperature(now, latitude, longitude, model, authHeader)
        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                Log.d("MainActivity", "Response Code: ${response.code()}")
                if (!response.isSuccessful) {
                    val errorBody = response.errorBody()?.string()
                    Log.e("MainActivity", "Error response: $errorBody")
                    weatherText.text = "Failed to fetch weather data. Code: ${response.code()}"
                    return
                }

                val weatherData = response.body()
                val temperature = weatherData?.data?.firstOrNull()
                    ?.coordinates?.firstOrNull()
                    ?.dates?.firstOrNull()
                    ?.value

                if (temperature != null) {
                    Log.d("MainActivity", "Madison Temperature: $temperature°F")
                    weatherText.text = "Madison Temperature: $temperature°F"
                } else {
                    Log.d("MainActivity", "Temperature data not available")
                    weatherText.text = "Temperature data not available"
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Log.e("MainActivity", "API Call Failed: ${t.localizedMessage}")
                t.printStackTrace()
                weatherText.text = "API Call Failed: ${t.message}"
                Toast.makeText(this@MainActivity, "API Call Failed", Toast.LENGTH_LONG).show()
            }
        })
    }
}