package com.apm.ropapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.apm.ropapp.databinding.HomeBinding

class Home : AppCompatActivity() {

    private lateinit var binding: HomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = HomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.noMeGusta.setOnClickListener {
            intent = Intent(this, Profile::class.java)
            Log.d("Home","Continue to Profile")
            startActivity(intent)
        }

        binding.meGusta.setOnClickListener {
            intent = Intent(this, Calendar::class.java)
            Log.d("Home","Continue to Calendar")
            startActivity(intent)
        }


    }
}