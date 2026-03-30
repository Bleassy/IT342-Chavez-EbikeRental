package com.chavez.mobile2.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.chavez.mobile2.databinding.ActivityDashboardBinding

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set welcome message
        binding.tvWelcome.text = "Welcome! You are logged in."

        // Profile button click
        binding.btnProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("userId", getIntent().getStringExtra("userId"))
            intent.putExtra("username", getIntent().getStringExtra("username"))
            intent.putExtra("email", getIntent().getStringExtra("email"))
            intent.putExtra("firstName", getIntent().getStringExtra("firstName"))
            intent.putExtra("lastName", getIntent().getStringExtra("lastName"))
            intent.putExtra("phoneNumber", getIntent().getStringExtra("phoneNumber"))
            intent.putExtra("memberSince", getIntent().getStringExtra("memberSince"))
            intent.putExtra("lastUpdated", getIntent().getStringExtra("lastUpdated"))
            startActivity(intent)
        }

        // Logout button click with confirmation dialog
        binding.btnLogout.setOnClickListener {
            val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            builder.setTitle("Confirm Logout")
            builder.setMessage("Are you sure you want to logout?")
            builder.setPositiveButton("Yes") { dialog, _ ->
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            builder.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            val dialog = builder.create()
            dialog.show()
            // Style dialog buttons to match web (optional, requires custom dialog theme for full match)
            dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)?.setTextColor(resources.getColor(com.chavez.mobile2.R.color.primaryButton))
            dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)?.setTextColor(resources.getColor(com.chavez.mobile2.R.color.primaryText))
        }
    }
}
