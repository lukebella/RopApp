package com.apm.ropapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.apm.ropapp.databinding.AddchoiceseasonBinding

class AddSeason : AppCompatActivity() {

    private lateinit var binding: AddchoiceseasonBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = AddchoiceseasonBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButtonChoice.setOnClickListener {
            intent = Intent(this, AddClothes::class.java)
            Log.d("TAG","Back to Add Clothes")
            startActivity(intent)
        }

        binding.confirm.setOnClickListener {
            intent = Intent(this, AddClothes::class.java)
            Log.d("TAG","Confirm season")
            startActivity(intent)
        }


    }
}