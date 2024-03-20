package com.apm.ropapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.apm.ropapp.databinding.AddchoiceseasonBinding
import com.apm.ropapp.databinding.CalendarBinding

class Calendar : AppCompatActivity() {

    private lateinit var binding: CalendarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = CalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButtonChoice.setOnClickListener {
            intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }


    }
}