package com.apm.ropapp

import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract.Profile
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.apm.ropapp.databinding.EditpasswordBinding
import com.apm.ropapp.databinding.EditprofileBinding


class EditPassword : AppCompatActivity() {
    private lateinit var binding: EditpasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EditpasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize your views here
        // For example, to set a click listener on the floating action button:
        binding.cancelButton3.setOnClickListener {
            intent = Intent(this, Profile::class.java)
            Log.d("TAG", "Back to Profile")
            startActivity(intent)
        }
        binding.saveButton3.setOnClickListener {
            intent = Intent(this, Profile::class.java) // Maybe editOutfit?
            Log.d("TAG", "Edit Profile")
            startActivity(intent)
        }

        binding.newPassword.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                val newPassword = binding.newPassword.text.toString()
                Log.d("TAG", "New Password: $newPassword")
                // You can now use the newPassword variable
            }
        }

        binding.newPassword.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                val confirmPassword = binding.newPassword.text.toString()
                Log.d("TAG", "Confirm Password: $confirmPassword")
                // You can now use the confirmPassword variable
            }
        }



        // Similarly, initialize other views and set their behavior
    }
}