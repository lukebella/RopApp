package com.apm.ropapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.apm.ropapp.databinding.ArrangeItemsBinding

class ArrangeItems : AppCompatActivity() {

    private lateinit var binding: ArrangeItemsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ArrangeItemsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.floatingBackButton.setOnClickListener {
            intent = Intent(this, CreateOutfit::class.java)
            Log.d("ArrangeItems","Back to Create Outfit")
            startActivity(intent)
        }

        binding.saveButton.setOnClickListener {
            intent = Intent(this, ShareOutfit::class.java)
            Log.d("ArrangeItems","Continue to Share Outfit")
            startActivity(intent)
        }

    }
}