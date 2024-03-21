package com.apm.ropapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.apm.ropapp.databinding.CreateoutfitBinding

class CreateOutfit : AppCompatActivity() {

    private lateinit var binding: CreateoutfitBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = CreateoutfitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            intent = Intent(this, MainActivity::class.java)
            Log.d("CreateOutfit", "Back to Main Activity")
            startActivity(intent)
        }

        binding.saveButton.setOnClickListener {
            intent = Intent(this, ArrangeItems::class.java)
            Log.d("CreateOutfit", "Outfit Added")
            startActivity(intent)
        }
        binding.accesorioButton.setOnClickListener {
            intent = Intent(this, SelectItem::class.java)
            Log.d("CreateOutfit", "Add Accessory")
            startActivity(intent)
        }
        binding.bottomButton.setOnClickListener {
            intent = Intent(this, SelectItem::class.java)
            Log.d("CreateOutfit", "Add Bottom")
            startActivity(intent)
        }
        binding.topButton.setOnClickListener {
            intent = Intent(this, SelectItem::class.java)
            Log.d("CreateOutfit", "Add Top")
            startActivity(intent)
        }
        binding.shoeButton.setOnClickListener {
            intent = Intent(this, SelectItem::class.java)
            Log.d("CreateOutfit", "Add Shoes")
            startActivity(intent)
        }
        binding.outfitName.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                val outfitName = binding.outfitName.text.toString()
                Log.d("CreateOutfit", "Outfit Name: $outfitName")
                // You can now use the outfitName variable
            }
        }
    }
}
