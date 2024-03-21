package com.apm.ropapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import com.apm.ropapp.Calendar
import com.apm.ropapp.CreateOutfit
import com.apm.ropapp.MainActivity
import com.apm.ropapp.databinding.ShareoutfitBinding


class ShareOutfit : AppCompatActivity() {
    private lateinit var binding: ShareoutfitBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ShareoutfitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize your views here
        // For example, to set a click listener on the floating action button:
        binding.cancelButton.setOnClickListener {
            intent = Intent(this, MainActivity::class.java)
            Log.d("TAG", "Back to Main Activity")
            startActivity(intent)
        }
        binding.editButton.setOnClickListener {
            intent = Intent(this, CreateOutfit::class.java) // Maybe editOutfit?
            Log.d("TAG", "Edit Outfit")
            startActivity(intent)
        }
        binding.shareButton.setOnClickListener {
            // Share the outfit
            shareContent("Check out this outfit!")
            Log.d("TAG", "Share Outfit")
        }
        binding.likeButton.setOnClickListener {
            // Like the outfit
            Log.d("TAG", "Like Outfit")
        }
        binding.calendarButton.setOnClickListener {
            // Add the outfit to the calendar
            intent = Intent(this, Calendar::class.java)
            Log.d("TAG", "Add to Calendar")
            startActivity(intent)
        }

        // Similarly, initialize other views and set their behavior
    }
    private fun shareContent(content: String) {
        ShareCompat.IntentBuilder(this)
            .setType("text/plain")
            .setChooserTitle("Share via")
            .setText(content)
            .startChooser()
    }
}