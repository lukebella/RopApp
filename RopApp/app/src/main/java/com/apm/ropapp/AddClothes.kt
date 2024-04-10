package com.apm.ropapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.apm.ropapp.databinding.AddclothesBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class AddClothes : AppCompatActivity() {

    private lateinit var binding: AddclothesBinding
    private lateinit var imageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = AddclothesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            intent = Intent(this, MainActivity::class.java)
            Log.d("AddClothes","Back to Main Activity")
            startActivity(intent)
        }

        binding.guardar.setOnClickListener {
            intent = Intent(this, MainActivity::class.java)
            Log.d("AddClothes","Dress Added")
            startActivity(intent)
        }

        imageUri = createImageUri()
        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                Log.d("Camara", imageUri.toString())
            }
        }

        binding.camara.setOnClickListener {
            intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            resultLauncher.launch(intent)
        }

        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: $uri")
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

        binding.galeria.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.categoria.setOnClickListener {
            intent = Intent(this, AddCategories::class.java)
            Log.d("AddClothes","Add category")
            startActivity(intent)
        }
        binding.estilo.setOnClickListener {
            intent = Intent(this, AddStyle::class.java)
            Log.d("AddClothes","Add Style")
            startActivity(intent)
        }
        binding.detalles.setOnClickListener {
            intent = Intent(this, AddDetails::class.java)
            Log.d("AddClothes","Add Details")
            startActivity(intent)
        }
        binding.temporada.setOnClickListener {
            intent = Intent(this, AddSeason::class.java)
            Log.d("AddClothes","Add Season")
            startActivity(intent)
        }
    }

    private fun createImageUri() : Uri {
        val dir = File("${externalMediaDirs.first()}/Photos")
        if (!dir.exists()) dir.mkdirs()
        val image = File(dir, //or filesDir
            SimpleDateFormat("yyyy-MM-dd-HH-mm", Locale.US)
                .format(System.currentTimeMillis()) + ".png")
        return FileProvider.getUriForFile(this, "com.apm.ropapp.FileProvider", image)
    }
}