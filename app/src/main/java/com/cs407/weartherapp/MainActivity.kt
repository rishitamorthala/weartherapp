package com.cs407.weartherapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val locationPermissionCode = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        weatherText = findViewById(R.id.weather_text)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Set up BottomNavigationView
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    true // Already on MainActivity
                }
                R.id.navigation_preferences -> {
                    val intent = Intent(this, PreferencesActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        // Check location permission and start location updates
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
            interval = 10000 // 10 seconds
            fastestInterval = 5000 // 5 seconds
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location: Location? = locationResult.lastLocation
                if (location != null) {
                    Log.d("MainActivity", "Location: ${location.latitude}, ${location.longitude}")
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

    private fun fetchWeather(latitude: Double, longitude: Double) {
        val username = "school_morthala_rishita"
        val password = "Y40Iu42uWd"
        val credentials = "$username:$password"
        val authHeader = "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)

        // Get current time in ISO 8601 format with timezone offset
        val now: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"))
        } else {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            String.format("%tFT%<tT.000Z", calendar)
        }

        val model = "mix" // Optional model parameter

        val fullUrl = "https://api.meteomatics.com/$now/t_2m:F/$latitude,$longitude/json?model=$model"
        Log.d("FETCH_WEATHER", "Request URL: $fullUrl")
        Log.d("FETCH_WEATHER", "Authorization Header: $authHeader")

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
                    weatherText.text = "Current Temperature: $temperature°F"
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
