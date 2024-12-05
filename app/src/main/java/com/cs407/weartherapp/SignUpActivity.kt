package com.cs407.weartherapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cs407.weartherapp.databinding.ActivitySignupBinding
import kotlinx.coroutines.*

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Handle Sign Up button
        binding.signupButton.setOnClickListener {
            val firstName = binding.firstName.text.toString().trim()
            val lastName = binding.lastName.text.toString().trim()
            val username = binding.username.text.toString().trim()
            val password = binding.password.text.toString()
            val gender = binding.gender.text.toString().trim()
            val birthday = binding.birthday.text.toString().trim()

            if (validateInput(firstName, lastName, username, password, gender, birthday)) {
                performSignUp(firstName, lastName, username, password, gender, birthday)
            }
        }

        // Handle "Already have an account?" button
        binding.loginText.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()  // Optional: Finish SignUpActivity to prevent going back to it
        }
    }

    private fun validateInput(firstName: String, lastName: String, username: String, password: String, gender: String, birthday: String): Boolean {
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

        if (gender.isEmpty()) {
            binding.gender.error = "Gender cannot be empty"
            isValid = false
        } else {
            binding.gender.error = null
        }

        if (birthday.isEmpty()) {
            binding.birthday.error = "Birthday cannot be empty"
            isValid = false
        } else {
            binding.birthday.error = null
        }

        return isValid
    }

    private fun performSignUp(firstName: String, lastName: String, username: String, password: String, gender: String, birthday: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                delay(1000)  // Simulate network request or database operation
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "User registered successfully!", Toast.LENGTH_LONG).show()
                    navigateToPreferences()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "Failed to register: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun navigateToPreferences() {
        val intent = Intent(this, PreferencesActivity::class.java)
        startActivity(intent)
        finish()  // Finish SignUpActivity to remove it from the back stack
    }
}
