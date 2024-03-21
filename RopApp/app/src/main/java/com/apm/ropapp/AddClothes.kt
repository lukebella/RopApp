package com.apm.ropapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.apm.ropapp.databinding.AddclothesBinding

class AddClothes : AppCompatActivity() {

    private lateinit var binding: AddclothesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = AddclothesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            intent = Intent(this, MainActivity::class.java)
            Log.d("AddClothes","Back to Main Activity")
            startActivity(intent)
        }

        binding.guardar.setOnClickListener {
            intent = Intent(this, Home::class.java)
            Log.d("AddClothes","Dress Added")
            startActivity(intent)
        }

        binding.categoria.setOnClickListener {
            intent = Intent(this, AddCategories::class.java)
            Log.d("AddClothes","Add category")
            startActivity(intent)
        }
        binding.estilo.setOnClickListener {
            intent = Intent(this, AddStyle::class.java)
            Log.d("AddClothes","Add Style")
            startActivity(intent)
        }
        binding.detalles.setOnClickListener {
            intent = Intent(this, AddDetails::class.java)
            Log.d("AddClothes","Add Details")
            startActivity(intent)
        }
        binding.temporada.setOnClickListener {
            intent = Intent(this, AddSeason::class.java)
            Log.d("AddClothes","Add Season")
            startActivity(intent)
        }
    }
}