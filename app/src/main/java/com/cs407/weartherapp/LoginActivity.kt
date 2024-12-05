package com.cs407.weartherapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cs407.weartherapp.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AuthManager.init(applicationContext)

        if (AuthManager.getCurrentUser() != null) {
            navigateToMain()
            return
        }

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
                //navigation to signup
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
                usernameLayout.error = null
            }
            if (password.isEmpty()) {
                passwordLayout.error = "Password cannot be empty"
                return false
            } else {
                passwordLayout.error = null
            }
            return true
        }
    }

    private fun performSignIn(username: String, password: String) {
        if (AuthManager.login(username, password)) {
            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
            navigateToMain()
        } else {
            binding.passwordLayout.error = "Invalid username or password"
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}