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
        //the list inspirational quotes and daily affirmations
        val messages = if (prefs.getBoolean("daily_affirmations", true)) {
            listOf(
                "   I am capable of achieving great things.",
                "   I believe in myself and my abilities.",
                " Today, I choose to think positive and create a wonderful day for myself.",
                " I am in charge of how I feel, and today I am choosing happiness.",
                "   I trust myself to make the right decision.",
                "   I am proud of myself and all that I have accomplished.",
                "   I give myself permission to do what is right for me.",
                "   I am worthy of respect and acceptance.",
                "   My contributions to the world are valuable.",
                "  I am a powerful creator. I create the life I want."
            )
        } else {
            listOf(
                "\"Sunshine is a welcome thing. It brings a lot of brightness.\"",
                "\"Wherever you go, no matter what the weather, always bring your own sunshine.\"",
                "\"The best thing one can do when it's raining is to let it rain.\"",
                "\"Life isn't about waiting for the storm to pass...It's about learning to dance in the rain.\"",
                "\"A positive attitude will lead to positive outcomes.\"",
                "\"There is no such thing as bad weather, only different kinds of good weather.\"",
                "\"To appreciate the beauty of a snowflake, it is necessary to stand out in the cold.\"",
                "\"Just for the record, darling, not all positive change feels positive in the beginning.\"",
                "\"Adopt the pace of nature: her secret is patience.\"",
                "\"Keep your face always toward the sunshine—and shadows will fall behind you.\""
            )
        }

        val quoteText: TextView = binding.quoteText
        val randomQuote = messages.random()
        quoteText.text = randomQuote

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
        // Show loading state
        weatherText.text = "Loading weather data..."
        recommendationText.text = "Fetching recommendations..."
        weatherIcon.setImageResource(0) // Remove any existing icon

        val username = "university_challa_supritha"
        val password = "L4eq0QO12t"
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
                    recommendationText.text = ""
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
                    setWeatherVisual(temperature)
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

    private fun setWeatherVisual(temperature: Double) {
        when {
            temperature <= 32 -> weatherIcon.setImageResource(R.drawable.ic_freezing)
            temperature in 32.0..50.0 -> weatherIcon.setImageResource(R.drawable.ic_cold)
            temperature in 50.0..60.0 -> weatherIcon.setImageResource(R.drawable.ic_mild)
            temperature in 60.0..75.0 -> weatherIcon.setImageResource(R.drawable.ic_warm)
            else -> weatherIcon.setImageResource(R.drawable.ic_hot)
        }
    }

    private fun getRecommendation(temperature: Double): String {
        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val stylePreference = prefs.getString("StylePreference", "casual")?.lowercase() ?: "casual"

        return when {
            temperature < 32 -> when (stylePreference) {
                "formal" -> "Wrap up like royalty in an elegant overcoat, thick scarf, and stylish gloves. ❄️🧤"
                "sporty" -> "Gear up for a frosty adventure with a sports jacket and thermal wear. Perfect for a brisk jog or a snowball fight! ⛄🏃"
                else -> "It's cuddle weather! Stay toasty with a thick jacket and your warmest gloves. Perfect for sipping hot cocoa by the fire! 🔥☕"
            }
            temperature in 32.0..50.0 -> when (stylePreference) {
                "formal" -> "Step out in style with a sleek trench coat and a cozy sweater. Don’t forget a hot latte to complete the vibe! ☕🧥"
                "sporty" -> "A hoodie and track pants will keep you warm during your morning workout. Stay active, stay healthy! 🏋️‍♂️"
                else -> "Layer up with a comfy jacket and enjoy the crisp air. Great for a walk in the park or a casual meetup. 🌳🚶"
            }
            temperature in 50.0..60.0 -> when (stylePreference) {
                "formal" -> "A stylish blazer and a fine scarf make the perfect combo for this cool weather. Ideal for both office and evening walks. 🌆👔"
                "sporty" -> "A windbreaker and joggers are your best friends for a windy day. Hit the trails or the track with some flair! 🌬️🏃"
                else -> "A light jacket and your trusty jeans are all you need to rock the day. Add a beanie for some extra flair! 🧢👖"
            }
            temperature in 60.0..75.0 -> when (stylePreference) {
                "formal" -> "Light and breezy fabrics are the order of the day. Dress to impress with minimal effort and maximum comfort. 🌼👗"
                "sporty" -> "T-shirt and shorts weather is here! Perfect for a beach volleyball game or a picnic. 🌞🏖️"
                else -> "Embrace the warmth with a tee and jeans. Throw in sunglasses and a hat for a perfect sunny day out. 😎🌞"
            }
            else -> when (stylePreference) {
                "formal" -> "Float through the heat in elegant linen. Ideal for outdoor weddings or a stroll down the boulevard. 🌷👔"
                "sporty" -> "Light tank tops and shorts are your go-to for a heatwave. Stay hydrated and keep cool! 💧🩳"
                else -> "Airy fabrics and loose clothing will keep you cool as a cucumber. Sunscreen and water are must-haves today! 🌞💧"
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
