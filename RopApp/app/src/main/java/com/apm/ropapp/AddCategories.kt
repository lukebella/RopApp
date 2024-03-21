package com.apm.ropapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.apm.ropapp.databinding.AddchoicecategoryBinding

class AddCategories : AppCompatActivity() {

    private lateinit var binding: AddchoicecategoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = AddchoicecategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButtonChoice.setOnClickListener {

            intent = Intent(this, AddClothes::class.java)
            Log.d("TAG","Back to add clothes")
            startActivity(intent)
        }

        binding.confirm.setOnClickListener {
            intent = Intent(this, AddClothes::class.java)
            Log.d("TAG","Categories Added")
            startActivity(intent)
        }


    }
}