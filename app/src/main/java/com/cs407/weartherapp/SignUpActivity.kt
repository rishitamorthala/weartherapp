package com.cs407.weartherapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cs407.weartherapp.databinding.ActivitySignupBinding
import kotlinx.coroutines.*
//kt file for activity_loggin.xml page
class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
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

        binding.loginText.setOnClickListener {
            // Navigate back to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun validateInput(firstName: String, lastName: String, username: String, password: String, gender: String, birthday: String): Boolean {
        var isValid = true
        // Validation logic here, simplified for brevity
        return isValid
    }

    private fun performSignUp(firstName: String, lastName: String, username: String, password: String, gender: String, birthday: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Simulate network request or database operation
                delay(1000)  // Simulate delay for signUp
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "User registered successfully!", Toast.LENGTH_LONG).show()
                    finish()  // Optionally finish this activity
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "Failed to register: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
