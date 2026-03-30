package com.chavez.mobile2.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.chavez.mobile2.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()
            if (username.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            loginUser(username, password)
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loginUser(username: String, password: String) {
        // ✅ Use lifecycleScope
        lifecycleScope.launch {
            try {
                val response = com.chavez.mobile2.network.RetrofitClient.apiService.login(
                    com.chavez.mobile2.network.LoginRequest(username, password)
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    val user = response.body()?.user
                    Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@LoginActivity, DashboardActivity::class.java)
                    intent.putExtra("userId", user?.id?.toString() ?: "")
                    intent.putExtra("username", user?.username ?: "")
                    intent.putExtra("email", user?.email ?: "")
                    intent.putExtra("firstName", user?.firstName ?: "")
                    intent.putExtra("lastName", user?.lastName ?: "")
                    intent.putExtra("phoneNumber", user?.phoneNumber ?: "")
                    intent.putExtra("memberSince", user?.createdAt ?: "")
                    intent.putExtra("lastUpdated", user?.updatedAt ?: "")
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, response.body()?.message ?: "Login failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
