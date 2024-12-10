package com.cs407.weartherapp

import android.content.Intent
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)

        val userFullNameText = findViewById<TextView>(R.id.userFullName)
        val currentUser = AuthManager.getCurrentUser()
        if (currentUser != null) {
            userFullNameText.text = "${currentUser.firstName} ${currentUser.lastName}"
        }

        val notificationsSwitch = findViewById<Switch>(R.id.notificationsSwitch)
        val themeSwitch = findViewById<Switch>(R.id.themeSwitch)

        // Retrieve saved preferences
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode_enabled", false)
        notificationsSwitch.isChecked = prefs.getBoolean("notifications_enabled", false)

        // Set initial state of the theme switch
        themeSwitch.isChecked = isDarkMode

        // Notifications switch listener
        notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply()
            Toast.makeText(
                this,
                if (isChecked) "Daily Quotes enabled" else "Daily Quotes disabled",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Theme switch listener
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("dark_mode_enabled", isChecked).apply()

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            // Recreate activity to apply changes
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
        // Update the switch state when the activity resumes
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        findViewById<Switch>(R.id.themeSwitch).isChecked = currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }
}
