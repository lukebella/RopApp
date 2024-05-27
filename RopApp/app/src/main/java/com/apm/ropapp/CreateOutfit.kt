package com.apm.ropapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.apm.ropapp.databinding.CreateoutfitBinding

class CreateOutfit : AppCompatActivity() {

    private lateinit var binding: CreateoutfitBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = CreateoutfitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            Log.d("CreateOutfit", "Back to Main Activity")
            finish()
        }

        binding.saveButton.setOnClickListener {
            finish()
            intent = Intent(this, ShareOutfit::class.java)
            Log.d("CreateOutfit", "Outfit Added")
            startActivity(intent)
        }

        val startForAccessory =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    Log.d("CreateOutfit", result.data?.extras?.get("image").toString())
                } else Log.d("CreateOutfit", "Cancelled")
            }

        binding.accesory1Button.setOnClickListener {
            intent = Intent(this, SelectItem::class.java)
            Log.d("CreateOutfit", "Add Accessory")
            intent.putExtra("selectedChip", getString(R.string.prendaAccessories))
            startForAccessory.launch(intent)
        }

        val startForBottom =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    Log.d("CreateOutfit", result.data?.extras?.get("clothesValues").toString())
                } else Log.d("CreateOutfit", "Cancelled")
            }

        binding.bottomButton.setOnClickListener {
            intent = Intent(this, SelectItem::class.java)
            Log.d("CreateOutfit", "Add Bottom")
            intent.putExtra("selectedChip", getString(R.string.prendaBottom))
            startForBottom.launch(intent)
        }

        val startForTop =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    Log.d("CreateOutfit", result.data?.extras?.get("image").toString())
                } else Log.d("CreateOutfit", "Cancelled")
            }

        binding.topButton.setOnClickListener {
            intent = Intent(this, SelectItem::class.java)
            Log.d("CreateOutfit", "Add Top")
            intent.putExtra("selectedChip", getString(R.string.prendaTop))
            startForTop.launch(intent)
        }

        val startForShoes =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    Log.d("CreateOutfit", result.data?.extras?.get("clothesValues").toString())
                } else Log.d("CreateOutfit", "Cancelled")
            }

        binding.shoesButton.setOnClickListener {
            intent = Intent(this, SelectItem::class.java)
            Log.d("CreateOutfit", "Add Shoes")
            intent.putExtra("selectedChip", getString(R.string.prendaShoes))
            startForShoes.launch(intent)
        }

    }
}
