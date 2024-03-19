package com.apm.ropapp

import android.content.Intent
import android.os.Bundle
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
            startActivity(intent)
        }

        binding.confirm.setOnClickListener {
            intent = Intent(this, AddClothes::class.java)
            startActivity(intent)
        }


    }
}