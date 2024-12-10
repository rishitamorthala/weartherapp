package com.cs407.weartherapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class PreferencesActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.preferences)

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        setupSubmitButton()
        setupBottomNavigation()
        loadExistingPreferences()
    }

    private fun setupSubmitButton() {
        val submitButton = findViewById<Button>(R.id.buttonSubmit)
        submitButton.setOnClickListener {
            if (validateSelections()) {
                savePreferences()
                // Navigate to home page
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

        val recommendation = getStyleRecommendation(stylePreference)

        with(sharedPreferences.edit()) {
            putString("TemperaturePreference", temperaturePreference)
            putString("WindPreference", windPreference)
            putString("StylePreference", stylePreference)
            apply()
        }

        Toast.makeText(this, "Preferences saved successfully!\n$recommendation", Toast.LENGTH_LONG).show()
    }

    private fun getStyleRecommendation(stylePreference: String): String {
        return when (stylePreference) {
            "casual" -> "We recommend wearing a comfortable t-shirt, jeans, and sneakers for a relaxed day out."
            "formal" -> "Consider wearing a tailored blazer, dress shirt, and polished shoes for a professional look."
            "sporty" -> "Opt for a moisture-wicking t-shirt, joggers, and running shoes for an active day."
            else -> "Choose an outfit that suits your activity and the weather!"
        }
    }

    private fun loadExistingPreferences() {
        val temperaturePreference = sharedPreferences.getString("TemperaturePreference", "neither")
        val windPreference = sharedPreferences.getString("WindPreference", "moderate")
        val stylePreference = sharedPreferences.getString("StylePreference", "casual")

        val tempGroup = findViewById<RadioGroup>(R.id.radioGroupTemperature)
        val tempId = when (temperaturePreference) {
            "cold" -> R.id.radioCold
            "hot" -> R.id.radioHot
            else -> R.id.radioNeither
        }
        tempGroup.check(tempId)

        val windGroup = findViewById<RadioGroup>(R.id.radioGroupWind)
        val windId = when (windPreference) {
            "high" -> R.id.radioHighWind
            "moderate" -> R.id.radioModerateWind
            else -> R.id.radioLowWind
        }
        windGroup.check(windId)

        val styleGroup = findViewById<RadioGroup>(R.id.styleGroup)
        val styleId = when (stylePreference) {
            "casual" -> R.id.radioCasual
            "formal" -> R.id.radioFormal
            else -> R.id.radioSporty
        }
        styleGroup.check(styleId)
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.navigation_preferences -> true
                R.id.navigation_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
        bottomNavigationView.selectedItemId = R.id.navigation_preferences
    }
}
