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

        // Set initial switch state based on current theme
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        themeSwitch.isChecked = currentNightMode == Configuration.UI_MODE_NIGHT_YES

        notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply()

            Toast.makeText(
                this,
                if (isChecked) "Notifications enabled" else "Notifications disabled",
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

            // Recreate the activity to apply theme changes
            recreate()
        }

        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        notificationsSwitch.isChecked = prefs.getBoolean("notifications_enabled", false)

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
        // Update switch state when activity resumes
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        findViewById<Switch>(R.id.themeSwitch).isChecked = currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }
}
