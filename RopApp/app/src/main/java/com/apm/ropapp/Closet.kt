package com.apm.ropapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.apm.ropapp.databinding.ClosetBinding

class Closet : AppCompatActivity() {

    private lateinit var binding: ClosetBinding
    private lateinit var buttonSelected: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ClosetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.floatingAddButton.setOnClickListener {
            intent = Intent(this, MainActivity::class.java)
            Log.d("Closet","Back to Main Activity")
            startActivity(intent)
        }

        buttonSelected = binding.button1
        buttonSelected.isSelected = true

        binding.button1.setOnClickListener {
            selectButton(binding.button1)
        }

        binding.button2.setOnClickListener {
            selectButton(binding.button2)
        }
    }

    private fun selectButton(newButton: Button) {
        buttonSelected.isSelected = false
        newButton.isSelected = true
        buttonSelected = newButton
    }
}