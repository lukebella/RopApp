package com.apm.ropapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.apm.ropapp.databinding.SelectItemBinding

class SelectItem : AppCompatActivity() {

    private lateinit var binding: SelectItemBinding
    private lateinit var buttonSelected: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = SelectItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.floatingBackButton.setOnClickListener {
            intent = Intent(this, MainActivity::class.java)
            Log.d("TAG","Back to Main Activity")
            startActivity(intent)
        }

        binding.floatingAddButton.setOnClickListener {
            intent = Intent(this, ArrangeItems::class.java)
            Log.d("TAG","Continue to Arrange Items")
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

        binding.button3.setOnClickListener {
            selectButton(binding.button3)
        }

        binding.button4.setOnClickListener {
            selectButton(binding.button4)
        }
    }

    private fun selectButton(newButton: Button) {
        buttonSelected.isSelected = false
        newButton.isSelected = true
        buttonSelected = newButton
    }
}