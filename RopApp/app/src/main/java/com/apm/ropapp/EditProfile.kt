package com.apm.ropapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.apm.ropapp.databinding.EditprofileBinding
import com.google.firebase.auth.FirebaseAuth


class EditProfile : AppCompatActivity() {
    private lateinit var binding: EditprofileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EditprofileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize your views here
        // For example, to set a click listener on the floating action button:
        binding.cancelButton2.setOnClickListener {
            Log.d("EditProfile", "Back to Profile")
            intent = Intent(this, MainActivity::class.java)
            intent.putExtra("fragment", R.id.navigation_user)
            startActivity(intent)
        }
        binding.saveButton2.setOnClickListener {
            Log.d("EditProfile", "Edit Profile")
            intent = Intent(this, MainActivity::class.java) // Maybe editOutfit?
            intent.putExtra("fragment", R.id.navigation_user)
            startActivity(intent)
        }

        binding.editAddress.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                val address = binding.editAddress.text.toString()
                Log.d("EditProfile", "Address: $address")
                // You can now use the address variable
            }
        }

        binding.editPhone.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                val phone = binding.editPhone.text.toString()
                Log.d("EditProfile", "Phone: $phone")
                // You can now use the phone variable
            }
        }

        binding.editEmail.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                val email = binding.editEmail.text.toString()
                Log.d("EditProfile", "Email: $email")
                // You can now use the email variable
            }
        }

        binding.passwordChange.setOnClickListener() {
            intent = Intent(this, EditPassword::class.java)
            Log.d("EditProfile", "Change Password")
            startActivity(intent)
        }



        // Similarly, initialize other views and set their behavior
    }
}