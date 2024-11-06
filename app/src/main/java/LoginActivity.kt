package com.cs407.weartherapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cs407.weartherapp.databinding.ActivityLoginBinding

//kt file for the login email page
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
                finish()
            }

            signInButton.setOnClickListener {
                val username = usernameInput.text.toString()
                val password = passwordInput.text.toString()

                if (validateInput(username, password)) {
                    performSignIn(username, password)
                }
            }

            needAccountText.setOnClickListener {
                // Navigate to registration screen
                // startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
            }
        }
    }

    private fun validateInput(username: String, password: String): Boolean {
        with(binding) {
            if (username.isEmpty()) {
                usernameLayout.error = "Username cannot be empty"
                return false
            }
            if (password.isEmpty()) {
                passwordLayout.error = "Password cannot be empty"
                return false
            }
            return true
        }
    }

    private fun performSignIn(username: String, password: String) {
        // Implement your sign-in logic here
    }
}