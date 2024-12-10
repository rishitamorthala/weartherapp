package com.cs407.weartherapp

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.bottomnavigation.BottomNavigationView

class SettingsActivity : AppCompatActivity() {
    private lateinit var prefs: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)
        prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        val userFullNameText = findViewById<TextView>(R.id.userFullName)
        val currentUser = AuthManager.getCurrentUser()
        if (currentUser != null) {
            userFullNameText.text = "${currentUser.firstName} ${currentUser.lastName}"
        }

        val themeSwitch = findViewById<Switch>(R.id.themeSwitch)
        val messageSwitch = findViewById<Switch>(R.id.notificationsSwitch)


        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        themeSwitch.isChecked = currentNightMode == Configuration.UI_MODE_NIGHT_YES
        messageSwitch.isChecked = prefs.getBoolean("daily_affirmations", false)

        messageSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("daily_affirmations", isChecked).apply()
            Toast.makeText(
                this,
                if (isChecked) "Daily Affirmations enabled" else "Inspirational Quotes enabled",
                Toast.LENGTH_SHORT
            ).show()
        }



        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
            prefs.edit().putBoolean("dark_mode_enabled", isChecked).apply()

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }


            recreate()
        }



        val logoutButton = findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            AuthManager.logout()
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        setupBottomNavigation()
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
                R.id.navigation_preferences -> {
                    val intent = Intent(this, PreferencesActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.navigation_settings -> true
                else -> false
            }
        }
        bottomNavigationView.selectedItemId = R.id.navigation_settings
    }

    override fun onResume() {
        super.onResume()

        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        findViewById<Switch>(R.id.themeSwitch).isChecked = currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }
}
