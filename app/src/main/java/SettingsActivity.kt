package com.cs407.weartherapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

            Toast.makeText(
                this,
                if (isChecked) "Dark mode enabled" else "Light mode enabled",
                Toast.LENGTH_SHORT
            ).show()
        }

        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        notificationsSwitch.isChecked = prefs.getBoolean("notifications_enabled", false)
        themeSwitch.isChecked = prefs.getBoolean("dark_mode_enabled", false)

        val logoutButton = findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            AuthManager.logout()
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_preferences -> {
                    val intent = Intent(this, PreferencesActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_settings -> true
                else -> false
            }
        }
    }
}