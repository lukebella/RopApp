package com.apm.ropapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.apm.ropapp.databinding.ArrangeItemsBinding

class ArrangeItems : AppCompatActivity() {

    private lateinit var binding: ArrangeItemsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ArrangeItemsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.floatingBackButton.setOnClickListener {
            intent = Intent(this, MainActivity::class.java)
            Log.d("TAG","Back to Main Activity")
            startActivity(intent)
        }

        binding.saveButton.setOnClickListener {
            intent = Intent(this, ArrangeItems::class.java)
            Log.d("TAG","Continue to Arrange Items")
            startActivity(intent)
        }

    }
}