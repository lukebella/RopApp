package com.apm.ropapp

import android.content.Intent
import android.os.Bundle
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
            startActivity(intent)
        }

        binding.guardar.setOnClickListener {
            intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        binding.categoria.setOnClickListener {
            intent = Intent(this, AddCategories::class.java)
            startActivity(intent)
        }
        binding.estilo.setOnClickListener {
            intent = Intent(this, AddStyle::class.java)
            startActivity(intent)
        }
        binding.detalles.setOnClickListener {
            intent = Intent(this, AddDetails::class.java)
            startActivity(intent)
        }
        binding.temporada.setOnClickListener {
            intent = Intent(this, AddSeason::class.java)
            startActivity(intent)
        }
    }
}