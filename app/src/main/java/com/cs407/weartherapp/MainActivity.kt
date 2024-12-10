package com.cs407.weartherapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var weatherText: TextView
    private lateinit var recommendationText: TextView
    private lateinit var weatherIcon: ImageView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val locationPermissionCode = 1000
    private var cityName: String = "Unknown Location"

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode_enabled", false)
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        weatherText = findViewById(R.id.weather_text)
        recommendationText = findViewById(R.id.recommendation_text)
        weatherIcon = findViewById(R.id.weather_icon)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupGreeting()
        setupBottomNavigation()

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

    private fun setupGreeting() {
        val userPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val userName = userPrefs.getString("FirstName", "User")
        val greetingText = findViewById<TextView>(R.id.greeting_text)
        greetingText.text = "Hello $userName! Here's the weather for today!"
    }

    private fun setupBottomNavigation() {
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
        bottomNavigationView.selectedItemId = R.id.navigation_home
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
            retryFetchCityName(latitude, longitude)
        }
    }

    private fun retryFetchCityName(latitude: Double, longitude: Double) {
        Handler(Looper.getMainLooper()).postDelayed({
            fetchCityName(latitude, longitude)
        }, 2000)
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
                if (!response.isSuccessful) {
                    weatherText.text = "Failed to fetch weather data. Code: ${response.code()}"
                    return
                }

                val weatherData = response.body()
                val temperature = weatherData?.data?.firstOrNull()
                    ?.coordinates?.firstOrNull()
                    ?.dates?.firstOrNull()
                    ?.value
                val condition = weatherData?.data?.firstOrNull()
                    ?.coordinates?.firstOrNull()
                    ?.dates?.firstOrNull()
                    ?.description ?: "Unknown"

                if (temperature != null) {
                    weatherText.text = "Current Temperature in $cityName is: $temperature°F"
                    recommendationText.text = getRecommendation(temperature)
                    setWeatherVisual(condition)
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

    private fun setWeatherVisual(condition: String) {
        when (condition.lowercase()) {
            "clear", "sunny" -> weatherIcon.setImageResource(R.drawable.ic_sunny)
            "cloudy" -> weatherIcon.setImageResource(R.drawable.ic_cloudy)
            "rain", "rainy" -> weatherIcon.setImageResource(R.drawable.ic_rainy)
            "snow" -> weatherIcon.setImageResource(R.drawable.ic_snow)
            else -> weatherIcon.setImageResource(R.drawable.ic_unknown_weather)
        }
    }

    private fun getRecommendation(temperature: Double): String {
        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val stylePreference = prefs.getString("StylePreference", "casual")?.lowercase() ?: "casual"

        return when {
            temperature < 32 -> when (stylePreference) {
                "formal" -> "Wear a formal coat with a scarf and gloves."
                "sporty" -> "Layer up with a sports jacket and thermal wear."
                else -> "Stay warm with a thick jacket and gloves."
            }
            temperature in 32.0..50.0 -> when (stylePreference) {
                "formal" -> "A tailored blazer or trench coat is perfect."
                "sporty" -> "Wear a warm hoodie and track pants."
                else -> "A thicker jacket is great for this weather."
            }
            temperature in 50.0..60.0 -> when (stylePreference) {
                "formal" -> "Try a stylish blazer with a scarf."
                "sporty" -> "A light jacket or windbreaker works well."
                else -> "A casual jacket and jeans are ideal."
            }
            temperature in 60.0..75.0 -> when (stylePreference) {
                "formal" -> "Opt for lightweight formal attire."
                "sporty" -> "T-shirt and shorts are perfect."
                else -> "T-shirt and jeans are great for this weather."
            }
            else -> when (stylePreference) {
                "formal" -> "Stay cool in linen formal wear."
                "sporty" -> "Wear a sports tank top and shorts."
                else -> "Light fabrics and short sleeves are ideal."
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
