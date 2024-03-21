package com.apm.ropapp

import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract.Profile
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.apm.ropapp.databinding.EditprofileBinding


class EditProfile : AppCompatActivity() {
    private lateinit var binding: EditprofileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EditprofileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize your views here
        // For example, to set a click listener on the floating action button:
        binding.cancelButton2.setOnClickListener {
            intent = Intent(this, Profile::class.java)
            Log.d("TAG", "Back to Main Activity")
            startActivity(intent)
        }
        binding.saveButton2.setOnClickListener {
            intent = Intent(this, Profile::class.java) // Maybe editOutfit?
            Log.d("TAG", "Edit Outfit")
            startActivity(intent)
        }

        binding.editAddress.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                val address = binding.editAddress.text.toString()
                Log.d("TAG", "Address: $address")
                // You can now use the address variable
            }
        }

        binding.editPhone.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                val phone = binding.editPhone.text.toString()
                Log.d("TAG", "Phone: $phone")
                // You can now use the phone variable
            }
        }

        binding.editEmail.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                val email = binding.editEmail.text.toString()
                Log.d("TAG", "Email: $email")
                // You can now use the email variable
            }
        }

        binding.passwordChange.setOnClickListener() {
            intent = Intent(this, EditPassword::class.java)
            Log.d("TAG", "Change Password")
            startActivity(intent)
        }



        // Similarly, initialize other views and set their behavior
    }
}