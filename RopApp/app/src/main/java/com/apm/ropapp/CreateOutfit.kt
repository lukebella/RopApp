package com.apm.ropapp

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.apm.ropapp.databinding.CreateoutfitBinding
import com.apm.ropapp.utils.CLOTHES
import com.apm.ropapp.utils.ImageUtils
import com.apm.ropapp.utils.OUTFITS
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.launch
import java.util.UUID

class CreateOutfit : AppCompatActivity() {

    private lateinit var binding: CreateoutfitBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var storage: StorageReference
    private lateinit var outfitId: String
    private val clothesDataMap = HashMap<String, Any>()
    private val clothesUriMap = HashMap<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = CreateoutfitBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance(getString(R.string.database_url)).reference
        storage = FirebaseStorage.getInstance(getString(R.string.storage_url)).reference

        val outfitData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            intent.extras?.getSerializable("outfitValues", HashMap<String, Any>().javaClass)
        else intent.extras?.getSerializable("outfitValues")

        if (outfitData != null && outfitData is HashMap<*, *>) {
            Log.d("EditOutfit", outfitData.toString())
            outfitId = outfitData["id"].toString()
            outfitData.remove("id")
            outfitData.forEach { (key, value) -> clothesDataMap[key.toString()] = value }
            binding.outfitName.setText(clothesDataMap["outfitName"].toString())

            clothesDataMap.forEach { (key, value) ->
                if (value is HashMap<*, *> && value["photo"] != null) {
                    lifecycleScope.launch {
                        val image = ImageUtils.getImageUri(value["photo"].toString(), CLOTHES,
                            storage.child(CLOTHES), binding.root.context).toString()
                        clothesUriMap[key] = image
                        when (key) {
                            getString(R.string.prendaTop) ->
                                binding.topButton.setImageURI(image.toUri())
                            getString(R.string.prendaOuterwear) ->
                                binding.outerwearButton.setImageURI(image.toUri())
                            getString(R.string.prendaBottom) ->
                                binding.bottomButton.setImageURI(image.toUri())
                            getString(R.string.prendaShoes) ->
                                binding.shoesButton.setImageURI(image.toUri())
                            "${getString(R.string.prendaAccessories)}1" ->
                                binding.accesory1Button.setImageURI(image.toUri())
                            "${getString(R.string.prendaAccessories)}2" ->
                                binding.accesory2Button.setImageURI(image.toUri())
                        }
                    }
                }
            }
        }

        binding.backButton.setOnClickListener {
            Log.d("CreateOutfit", "Back to Main Activity")
            finish()
        }

        val startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    Log.d("CreateOutfit", "Completed")
                    finish()
                }
                else Log.d("CreateOutfit", "Editing Outfit")
            }

        binding.saveButton.setOnClickListener {
            val outfitName = binding.outfitName.text.toString()
            clothesDataMap["outfitName"] = outfitName
            uploadNewOutfit()
            Log.d("CreateOutfit", "Outfit Added: $clothesUriMap")

            intent = Intent(this, ShareOutfit::class.java)
            intent.putExtra("outfitName", outfitName)
            intent.putExtra("clothesUriBundle", clothesUriMap)
            //intent.putExtra("outfitImage", clothesDataMap["outfitImage"])
            startForResult.launch(intent)
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
        val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
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
                }
                Activity.RESULT_FIRST_USER -> {
                    Log.d("CreateOutfit", "Removed Clothes")
                    clothesDataMap.remove(mapKey)
                    clothesUriMap.remove(mapKey)
                    button.setImageResource(R.drawable.baseline_add_24)
                }
                else -> Log.d("CreateOutfit", "Cancelled")
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
            val uploadTask = database.child(OUTFITS).child(firebaseAuth.currentUser!!.uid)
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