package com.apm.ropapp

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.apm.ropapp.databinding.CreateoutfitBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID

class CreateOutfit : AppCompatActivity() {

    private lateinit var binding: CreateoutfitBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var outfitId: String
    private val clothesDataMap = mutableMapOf<String, Any>()
    private val clothesUriMap = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = CreateoutfitBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance(getString(R.string.database_url)).reference

        val outfitData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            intent.extras?.getSerializable("clothesValues", HashMap<String, Any>().javaClass)
        else intent.extras?.getSerializable("clothesValues")

        if (outfitData != null && outfitData is HashMap<*, *>) {
            Log.d("EditOutfit", outfitData.toString())
            outfitId = outfitData["id"].toString()
            outfitData.remove("id")
            outfitData.forEach { (key, value) -> clothesDataMap[key.toString()] = value }

            binding.outfitName.setText(clothesDataMap["outfitName"].toString())
        }

        binding.backButton.setOnClickListener {
            Log.d("CreateOutfit", "Back to Main Activity")
            finish()
        }

        binding.saveButton.setOnClickListener {
            clothesDataMap["outfitName"] = binding.outfitName.text.toString()
            uploadNewOutfit()
            finish()
            intent = Intent(this, ShareOutfit::class.java)
            Log.d("CreateOutfit", "Outfit Added")
            startActivity(intent)
        }

        val buttonMapKeyPairs = listOf(
            Pair(binding.topButton, getString(R.string.prendaTop)),
            Pair(binding.outerwearButton, getString(R.string.prendaOuterwear)),
            Pair(binding.bottomButton, getString(R.string.prendaBottom)),
            Pair(binding.shoesButton, getString(R.string.prendaShoes)),
        )
        buttonMapKeyPairs.forEach { (button, selectedChip) ->
            setupButtonWithActivityResult(button, selectedChip, selectedChip)
        }
        setupButtonWithActivityResult(binding.accesory1Button, getString(R.string.prendaAccessories),
            "${getString(R.string.prendaAccessories)}1")
        setupButtonWithActivityResult(binding.accesory2Button, getString(R.string.prendaAccessories),
            "${getString(R.string.prendaAccessories)}2")
    }

    private fun setupButtonWithActivityResult(button: ImageButton, selectedChip: String, mapKey: String) {
        val startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.extras?.let { extras ->
                        val clothesValues =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                                extras.getSerializable("clothesValues", HashMap<String, Any>().javaClass)
                            else extras.getSerializable("clothesValues")
                        clothesValues?.let { clothesDataMap[mapKey] = it as HashMap<*, *> }
                        Log.d("CreateOutfit", "Added clothes: $clothesValues")
                        extras.getString("image")?.let {
                            clothesUriMap[mapKey] = it
                            button.setImageURI(it.toUri())
                        } ?: run {
                            clothesUriMap[mapKey] = ""
                            button.setImageResource(R.drawable.tshirt)
                        }
                    }
                } else {
                    Log.d("CreateOutfit", "Cancelled")
                    clothesDataMap.remove(mapKey)
                    clothesUriMap.remove(mapKey)
                    button.setImageResource(R.drawable.baseline_add_24)
                }
            }

        button.setOnClickListener {
            intent = Intent(this, SelectItem::class.java)
            Log.d("CreateOutfit", "Add $selectedChip")
            intent.putExtra("selectedChip", selectedChip)
            startForResult.launch(intent)
        }
    }

    private fun uploadNewOutfit() {
        if (firebaseAuth.currentUser != null) {
            if (!this::outfitId.isInitialized) outfitId = UUID.randomUUID().toString()
            val uploadTask = database.child("outfits").child(firebaseAuth.currentUser!!.uid)
                .child(outfitId).setValue(clothesDataMap)
            // Register observers to listen for when the upload is done or if it fails
            uploadTask.addOnFailureListener {
                // Handle unsuccessful uploads
                Log.d("DatabaseUpdate", "Upload Failed: ${it.stackTrace}")
            }.addOnSuccessListener {
                // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
                Log.d("DatabaseUpdate", "Upload Success: $clothesDataMap")
            }
        }
    }
}
