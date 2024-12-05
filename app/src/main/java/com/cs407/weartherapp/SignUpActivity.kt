package com.cs407.weartherapp

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cs407.weartherapp.databinding.ActivitySignupBinding

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up gender spinner
        ArrayAdapter.createFromResource(
            this,
            R.array.gender_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.genderSpinner.adapter = adapter
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.signupButton.setOnClickListener {
            val firstName = binding.firstName.text.toString().trim()
            val lastName = binding.lastName.text.toString().trim()
            val username = binding.username.text.toString().trim()
            val password = binding.password.text.toString()
            val gender = binding.genderSpinner.selectedItem.toString()

            if (validateInput(firstName, lastName, username, password, gender)) {
                val userData = UserData(firstName, lastName, username, password, gender, "")
                if (AuthManager.signUp(userData)) {
                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                    navigateToPreferences()
                } else {
                    Toast.makeText(this, "Username already exists!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.loginText.setOnClickListener {
            finish()
        }
    }

    private fun validateInput(firstName: String, lastName: String, username: String, password: String, gender: String): Boolean {
        var isValid = true

        if (firstName.isEmpty()) {
            binding.firstName.error = "First name cannot be empty"
            isValid = false
        } else {
            binding.firstName.error = null
        }

        if (lastName.isEmpty()) {
            binding.lastName.error = "Last name cannot be empty"
            isValid = false
        } else {
            binding.lastName.error = null
        }

        if (username.isEmpty()) {
            binding.username.error = "Username cannot be empty"
            isValid = false
        } else {
            binding.username.error = null
        }

        if (password.isEmpty()) {
            binding.password.error = "Password cannot be empty"
            isValid = false
        } else {
            binding.password.error = null
        }

        if (gender == "Select Gender") {
            Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    private fun navigateToPreferences() {
        val intent = Intent(this, PreferencesActivity::class.java)
        startActivity(intent)
        finish()
    }
}