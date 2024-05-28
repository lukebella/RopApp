package com.apm.ropapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import com.apm.ropapp.databinding.ShareoutfitBinding


class ShareOutfit : AppCompatActivity() {
    private lateinit var binding: ShareoutfitBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ShareoutfitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val outfitName = intent.getStringExtra("outfitName")
        if (!outfitName.isNullOrEmpty()) {
            binding.textView.text = outfitName
        }

        binding.cancelButton.setOnClickListener {
            Log.d("ShareOutfit", "Back to Main Activity")
            intent = Intent()
            setResult(RESULT_OK, intent)
            finish()
        }
        binding.editButton.setOnClickListener {
            Log.d("ShareOutfit", "Edit Outfit")
            finish()
        }
        binding.shareButton.setOnClickListener {
            // Share the outfit
            shareContent("Check out this outfit!")
            Log.d("ShareOutfit", "Share Outfit")
        }

    }
    private fun shareContent(content: String) {
        ShareCompat.IntentBuilder(this)
            .setType("text/plain")
            .setChooserTitle("Share via")
            .setText(content)
            .startChooser()
    }
}