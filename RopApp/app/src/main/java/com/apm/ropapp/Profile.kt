package com.apm.ropapp

import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract.Profile
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.apm.ropapp.databinding.EditprofileBinding
import com.apm.ropapp.databinding.ProfileBinding


public class Profile : AppCompatActivity() {
    private lateinit var binding: ProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize your views here
        // For example, to set a click listener on the floating action button:
        binding.configButton.setOnClickListener {
            intent = Intent(this, EditProfile::class.java)
            Log.d("TAG", "Editing profile")
            startActivity(intent)
        }
        binding.infoButton.setOnClickListener {
            Log.d("TAG", "Info button clicked")
        }

        binding.statButton.setOnClickListener {
            Log.d("TAG", "Stats button clicked")
        }


        // Similarly, initialize other views and set their behavior
    }
}