package com.cs407.weartherapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cs407.weartherapp.databinding.ActivityMainBinding
import com.google.android.gms.location.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.newkotlinconnector.RetrofitInstance
import com.example.newkotlinconnector.WindResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var weatherText: TextView
    private lateinit var windText: TextView
    private lateinit var recommendationText: TextView
    private lateinit var weatherIcon: ImageView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val locationPermissionCode = 1000
    private var cityName: String = "Unknown Location"

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode_enabled", false)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        weatherText = findViewById(R.id.weather_text)
        windText = findViewById(R.id.wind_text)
        recommendationText = findViewById(R.id.recommendation_text)
        weatherIcon = findViewById(R.id.weather_icon)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val userPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val userName = userPrefs.getString("FirstName", "User")
        val greetingText = findViewById<TextView>(R.id.greeting_text)
        greetingText.text = "Hello $userName! Here's the weather for today!"

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> true
                R.id.navigation_preferences -> {
                    navigateToActivity(PreferencesActivity::class.java)
                    true
                }
                R.id.navigation_settings -> {
                    navigateToActivity(SettingsActivity::class.java)
                    true
                }
                else -> false
            }
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode
            )
        } else {
            startLocationUpdates()
        }
    }

    private fun navigateToActivity(targetActivity: Class<*>) {
        val intent = Intent(this, targetActivity).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 60000
            fastestInterval = 30000
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location: Location? = locationResult.lastLocation
                if (location != null) {
                    fetchCityName(location.latitude, location.longitude)
                    fetchWeather(location.latitude, location.longitude)
                    fetchWindData(location.latitude, location.longitude)
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private fun fetchCityName(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            cityName = addresses?.getOrNull(0)?.locality ?: "Unknown Location"
        } catch (e: Exception) {
            cityName = "Unknown Location"
        }
    }

    private fun fetchWeather(latitude: Double, longitude: Double) {
        val username = "school_morthala_rishita"
        val password = "Y40Iu42uWd"
        val credentials = "$username:$password"
        val authHeader = "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)

        val now = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"))
        val model = "mix"

        val call = RetrofitInstance.api.getTemperature(now, latitude, longitude, model, authHeader)
        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (!response.isSuccessful) {
                    weatherText.text = "Failed to fetch weather data. Code: ${response.code()}"
                    return
                }

                val weatherData = response.body()
                val temperature = weatherData?.data?.find { it.parameter == "t_2m:F" }
                    ?.coordinates?.firstOrNull()?.dates?.firstOrNull()?.value

                if (temperature != null) {
                    weatherText.text = "Current Temperature in $cityName is: $temperature°F"
                    recommendationText.text = getRecommendation(temperature)
                } else {
                    weatherText.text = "Temperature data not available"
                    recommendationText.text = ""
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                weatherText.text = "API Call Failed: ${t.message}"
                recommendationText.text = ""
                Toast.makeText(this@MainActivity, "API Call Failed", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun fetchWindData(latitude: Double, longitude: Double) {
        val username = "school_morthala_rishita"
        val password = "Y40Iu42uWd"
        val credentials = "$username:$password"
        val authHeader = "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)

        val now = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"))
        val endpoint = "https://api.meteomatics.com/$now/wind_speed_FL10:mph/$latitude,$longitude/json?model=mix"

        val call = RetrofitInstance.api.getWindData(authHeader, endpoint)
        call.enqueue(object : Callback<WindResponse> {
            override fun onResponse(call: Call<WindResponse>, response: Response<WindResponse>) {
                if (!response.isSuccessful) {
                    windText.text = "Failed to fetch wind data. Code: ${response.code()}"
                    return
                }

                val windData = response.body()
                val windSpeed = windData?.data?.find { it.parameter == "wind_speed_FL10:mph" }
                    ?.coordinates?.firstOrNull()?.dates?.firstOrNull()?.value

                if (windSpeed != null) {
                    windText.text = "Wind Speed: $windSpeed mph"
                } else {
                    windText.text = "Wind data not available"
                }
            }

            override fun onFailure(call: Call<WindResponse>, t: Throwable) {
                windText.text = "API Call Failed: ${t.message}"
                Toast.makeText(this@MainActivity, "Wind API Call Failed", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun getRecommendation(temperature: Double): String {
        return when {
            temperature < 32 -> "It's freezing! Wear a heavy coat and scarf."
            temperature in 32.0..50.0 -> "Chilly weather! A hoodie is perfect."
            temperature in 50.0..60.0 -> "A light jacket works well."
            temperature in 60.0..75.0 -> "Enjoy the weather with a t-shirt!"
            else -> "Hot weather! Stay cool and hydrated."
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
