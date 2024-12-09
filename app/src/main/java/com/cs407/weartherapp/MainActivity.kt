package com.cs407.weartherapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cs407.weartherapp.databinding.ActivityMainBinding
import com.google.android.gms.location.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import android.os.Handler


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var weatherText: TextView
    private lateinit var recommendationText: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val locationPermissionCode = 1000
    private var cityName: String = "Unknown Location"
    private var userName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        // Load and apply saved theme preference
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
        recommendationText = findViewById(R.id.recommendation_text)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        //for the saving of the user's name
        val userPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val userName = userPrefs.getString("FirstName", "User")

        val greetingText = findViewById<TextView>(R.id.greeting_text)
        greetingText.text = "Hello $userName! Here's the weather for today!"

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> true
                R.id.navigation_preferences -> {
                    val intent = Intent(this, PreferencesActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
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

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 60000 // 60 seconds
            fastestInterval = 30000 // 30 seconds
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location: Location? = locationResult.lastLocation
                if (location != null) {
                    Log.d("MainActivity", "Location: ${location.latitude}, ${location.longitude}")
                    fetchCityName(location.latitude, location.longitude)
                    fetchWeather(location.latitude, location.longitude)
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
            Log.d("MainActivity", "City Name: $cityName")
        } catch (e: Exception) {
            Log.e("MainActivity", "Geocoder failed: ${e.localizedMessage}")
            cityName = "Unknown Location"
            retryFetchCityName(latitude, longitude)
        }
    }

    private fun retryFetchCityName(latitude: Double, longitude: Double) {
        Log.d("MainActivity", "Retrying to fetch city name...")
        Handler(Looper.getMainLooper()).postDelayed({
            fetchCityName(latitude, longitude)
        }, 2000) // Retry after 2 seconds
    }

    private fun fetchWeather(latitude: Double, longitude: Double) {
        val username = "school_morthala_rishita"
        val password = "Y40Iu42uWd"
        val credentials = "$username:$password"
        val authHeader = "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)

        val now: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"))
        } else {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            String.format("%tFT%<tT.000Z", calendar)
        }

        val model = "mix"

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
                    Log.d("MainActivity", "Current Temperature: $temperature°F")
                    weatherText.text = "Current Temperature in $cityName is: $temperature°F"
                    recommendationText.text = getRecommendation(temperature)
                } else {
                    Log.d("MainActivity", "Temperature data not available")
                    weatherText.text = "Temperature data not available"
                    recommendationText.text = ""
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Log.e("MainActivity", "API Call Failed: ${t.localizedMessage}")
                t.printStackTrace()
                weatherText.text = "API Call Failed: ${t.message}"
                recommendationText.text = ""
                Toast.makeText(this@MainActivity, "API Call Failed", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun getRecommendation(temperature: Double): String {
        return when {
            temperature < 32 -> "It is below freezing! We suggest you bundle up."
            temperature in 32.0..50.0 -> "We recommend layers as it is pretty chilly."
            temperature in 50.0..60.0 -> "A hoodie will do!"
            temperature in 60.0..75.0 -> "We recommend short sleeves!"
            else -> "We suggest sunscreen!"
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}