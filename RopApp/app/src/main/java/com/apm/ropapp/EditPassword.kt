package com.apm.ropapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.apm.ropapp.databinding.EditpasswordBinding


class EditPassword : AppCompatActivity() {
    private lateinit var binding: EditpasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EditpasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize your views here
        // For example, to set a click listener on the floating action button:
        binding.cancelButton3.setOnClickListener {
            Log.d("EditPassword", "Back to EditProfile")
            intent = Intent(this, EditProfile::class.java)
            startActivity(intent)
        }
        binding.saveButton3.setOnClickListener {
            Log.d("EditPassword", "Change Password")
            intent = Intent(this, MainActivity::class.java) // Maybe editOutfit?
            intent.putExtra("fragment", R.id.navigation_user)
            startActivity(intent)
        }

        binding.newPassword.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                val newPassword = binding.newPassword.text.toString()
                Log.d("EditPassword", "New Password: $newPassword")
                // You can now use the newPassword variable
            }
        }

        binding.newPassword.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                val confirmPassword = binding.newPassword.text.toString()
                Log.d("EditPassword", "Confirm Password: $confirmPassword")
                // You can now use the confirmPassword variable
            }
        }

        // Similarly, initialize other views and set their behavior
    }
}