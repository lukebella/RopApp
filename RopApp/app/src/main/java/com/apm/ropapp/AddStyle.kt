package com.apm.ropapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.apm.ropapp.databinding.AddchoicestyleBinding

class AddStyle : AppCompatActivity() {

    private lateinit var binding: AddchoicestyleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = AddchoicestyleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButtonChoice.setOnClickListener {
            intent = Intent(this, AddClothes::class.java)
            Log.d("AddStyle","Back to Add Clothes")
            startActivity(intent)
        }

        binding.confirm.setOnClickListener {
            intent = Intent(this, AddClothes::class.java)
            Log.d("AddStyle","Confirm Style")
            startActivity(intent)
        }


    }
}