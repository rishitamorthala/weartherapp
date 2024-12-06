package com.cs407.weartherapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class PreferencesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.preferences)

        setupSubmitButton()
        setupBottomNavigation()
        loadExistingPreferences()
    }

    private fun setupSubmitButton() {
        val submitButton = findViewById<Button>(R.id.buttonSubmit)
        submitButton.setOnClickListener {
            if (validateSelections()) {
                savePreferences()
                //go to home page
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Please select all preferences!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateSelections(): Boolean {
        val tempGroup = findViewById<RadioGroup>(R.id.radioGroupTemperature)
        val windGroup = findViewById<RadioGroup>(R.id.radioGroupWind)
        val styleGroup = findViewById<RadioGroup>(R.id.styleGroup)

        return tempGroup.checkedRadioButtonId != -1 &&
                windGroup.checkedRadioButtonId != -1 &&
                styleGroup.checkedRadioButtonId != -1
    }

    private fun savePreferences() {
        val currentUser = AuthManager.getCurrentUser()
        if (currentUser != null) {
            val temperaturePreference = when (findViewById<RadioGroup>(R.id.radioGroupTemperature).checkedRadioButtonId) {
                R.id.radioCold -> "cold"
                R.id.radioHot -> "hot"
                R.id.radioNeither -> "neither"
                else -> "neither"
            }

            val windPreference = when (findViewById<RadioGroup>(R.id.radioGroupWind).checkedRadioButtonId) {
                R.id.radioHighWind -> "high"
                R.id.radioModerateWind -> "moderate"
                R.id.radioLowWind -> "low"
                else -> "moderate"
            }

            val stylePreference = when (findViewById<RadioGroup>(R.id.styleGroup).checkedRadioButtonId) {
                R.id.radioCasual -> "casual"
                R.id.radioFormal -> "formal"
                R.id.radioSporty -> "sporty"
                else -> "casual"
            }

            val preferences = WeatherPreferences(
                temperaturePreference = temperaturePreference,
                windSensitivity = windPreference,
                stylePreference = stylePreference
            )

            AuthManager.saveWeatherPreferences(currentUser.username, preferences)
            Toast.makeText(this, "Preferences saved successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadExistingPreferences() {
        val currentUser = AuthManager.getCurrentUser()
        if (currentUser != null) {
            val preferences = AuthManager.getWeatherPreferences(currentUser.username)
            if (preferences != null) {
                val tempGroup = findViewById<RadioGroup>(R.id.radioGroupTemperature)
                val tempId = when (preferences.temperaturePreference) {
                    "cold" -> R.id.radioCold
                    "hot" -> R.id.radioHot
                    else -> R.id.radioNeither
                }
                tempGroup.check(tempId)

                val windGroup = findViewById<RadioGroup>(R.id.radioGroupWind)
                val windId = when (preferences.windSensitivity) {
                    "high" -> R.id.radioHighWind
                    "moderate" -> R.id.radioModerateWind
                    else -> R.id.radioLowWind
                }
                windGroup.check(windId)

                val styleGroup = findViewById<RadioGroup>(R.id.styleGroup)
                val styleId = when (preferences.stylePreference) {
                    "casual" -> R.id.radioCasual
                    "formal" -> R.id.radioFormal
                    else -> R.id.radioSporty
                }
                styleGroup.check(styleId)
            }
        }
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.navigation_preferences -> true
                else -> false
            }
        }
        bottomNavigationView.selectedItemId = R.id.navigation_preferences
    }
}