package com.cs407.weartherapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class PreferencesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.preferences) // Use your preferences.xml layout directly

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    // Navigate to MainActivity
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish() // Finish PreferencesActivity to remove it from the back stack
                    true
                }
                R.id.navigation_preferences -> {
                    // Stay in PreferencesActivity
                    true
                }
                else -> false
            }
        }

        // Highlight the correct menu item
        bottomNavigationView.selectedItemId = R.id.navigation_preferences
    }
}
