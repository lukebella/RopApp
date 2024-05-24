package com.apm.ropapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.apm.ropapp.databinding.SelectItemBinding

class SelectItem : AppCompatActivity() {

    private lateinit var binding: SelectItemBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = SelectItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.floatingBackButton.setOnClickListener {
            intent = Intent(this, CreateOutfit::class.java)
            Log.d("SelectItem","Back to Create Outfit")
            startActivity(intent)
        }

    }
}