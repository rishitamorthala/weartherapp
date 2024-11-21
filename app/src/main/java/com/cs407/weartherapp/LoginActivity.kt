package com.cs407.weartherapp

import com.cs407.weartherapp.MainActivity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cs407.weartherapp.databinding.ActivityLoginBinding
import kotlinx.coroutines.*
import com.cs407.weartherapp.SignUpActivity

// Kt for the login email page
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        with(binding) {
            closeButton.setOnClickListener {
                finish() // Close the LoginActivity
            }

            signInButton.setOnClickListener {
                val username = usernameInput.text.toString()
                val password = passwordInput.text.toString()

                if (validateInput(username, password)) {
                    performSignIn(username, password)
                }
            }

            needAccountText.setOnClickListener {
                // Navigate to SignUpActivity
                val intent = Intent(this@LoginActivity, SignUpActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun validateInput(username: String, password: String): Boolean {
        with(binding) {
            if (username.isEmpty()) {
                usernameLayout.error = "Username cannot be empty"
                return false
            } else {
                usernameLayout.error = null // Clear error
            }
            if (password.isEmpty()) {
                passwordLayout.error = "Password cannot be empty"
                return false
            } else {
                passwordLayout.error = null // Clear error
            }
            return true
        }
    }

    private fun performSignIn(username: String, password: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = signInUser(username, password)
                if (result) {
                    navigateToMain()
                } else {
                    binding.passwordLayout.error = "Invalid username or password"
                }
            } catch (e: Exception) {
                binding.passwordLayout.error = "Login failed: ${e.localizedMessage}"
            }
        }
    }

    private suspend fun signInUser(username: String, password: String): Boolean {
        delay(1000) // Simulate network delay
        // Here you would typically check the credentials against a network service
        return username == "admin" && password == "admin" // Dummy check for demonstration
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Finish LoginActivity to remove it from the back stack
    }
}
