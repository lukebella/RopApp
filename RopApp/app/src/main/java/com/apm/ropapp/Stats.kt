package com.apm.ropapp

import com.apm.ropapp.StatsSeason
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.apm.ropapp.databinding.StatsBinding
import com.google.firebase.database.DatabaseReference

class Stats : AppCompatActivity() {
    private lateinit var binding: StatsBinding
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = StatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.seasonButton.setOnClickListener {
            intent = Intent(this, StatsSeason::class.java)
            Log.d("Stats", "Season Stats")
            startActivity(intent)
        }

        binding.clothesButton.setOnClickListener {
//            intent = Intent(this, ClothesStats::class.java)
            Log.d("Stats", "Clothes Stats")
            startActivity(intent)

        }
        binding.backButton.setOnClickListener {
            Log.d("Stats", "Back to MainActivity")
            finish()
        }
    }
}
