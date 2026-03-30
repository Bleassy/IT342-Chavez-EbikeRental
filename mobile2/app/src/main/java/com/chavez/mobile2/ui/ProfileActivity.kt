package com.chavez.mobile2.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.chavez.mobile2.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get user info from intent extras (replace with real backend/user session in production)
        val userId = intent.getStringExtra("userId") ?: "-"
        val username = intent.getStringExtra("username") ?: "-"
        val email = intent.getStringExtra("email") ?: "-"
        val firstName = intent.getStringExtra("firstName") ?: "-"
        val lastName = intent.getStringExtra("lastName") ?: "-"
        val phoneNumber = intent.getStringExtra("phoneNumber") ?: "-"
        val memberSince = intent.getStringExtra("memberSince") ?: "-"
        val lastUpdated = intent.getStringExtra("lastUpdated") ?: "-"

        // Set all details to TextViews (ensure you have these in your layout)
        binding.tvUserId.text = "User ID: $userId"
        binding.tvUsername.text = "Username: $username"
        binding.tvEmail.text = "Email: $email"
        binding.tvFirstName.text = "First Name: $firstName"
        binding.tvLastName.text = "Last Name: $lastName"
        binding.tvPhoneNumber.text = "Phone Number: $phoneNumber"
        binding.tvMemberSince.text = "Member Since: $memberSince"
        binding.tvLastUpdated.text = "Last Updated: $lastUpdated"

        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}
