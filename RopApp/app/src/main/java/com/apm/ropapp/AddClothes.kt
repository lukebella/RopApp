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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.util.UUID

class AddClothes : AppCompatActivity() {

    private lateinit var binding: AddclothesBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var storage: StorageReference
    private lateinit var imageUri: Uri
    private lateinit var fileNameUpload: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = AddclothesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance(getString(R.string.database_url)).reference
        storage = FirebaseStorage.getInstance(getString(R.string.storage_url)).reference

        binding.backButton.setOnClickListener {
            intent = Intent(this, MainActivity::class.java)
            Log.d("AddClothes", "Back to Main Activity")
            startActivity(intent)
        }

        binding.guardar.setOnClickListener {
            binding.guardar.isSelected = true

            val uploadData = HashMap<String, Any>()
            val details = HashMap<String, Any>()
            uploadData["category"] = "Top"
            uploadData["style"] = "Vintage"
            details["brand"] = "M&M"
            details["state"] = "Prestado"
            details["price"] = "20 €"
            details["size"] = "L"
            uploadData["details"] = details
            uploadData["season"] = listOf("Spring", "Summer")

            if (this::fileNameUpload.isInitialized) {
                uploadData["photo"] = fileNameUpload
                uploadPhoto()
            }
            uploadNewClothes(uploadData)

            intent = Intent(this, MainActivity::class.java)
            Log.d("AddClothes", "Dress Added")
            startActivity(intent)
        }

        val resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // There are no request codes
                    Log.d("Camara", "Captured URI: $imageUri")
                    binding.imageView.setImageURI(imageUri)
                    fileNameUpload = imageUri.toString().substring(
                        imageUri.toString().lastIndexOf("IMG")
                    )
                } else Log.d("Camara", "No media captured")
            }

        binding.camara.setOnClickListener {
            imageUri = createImageUri()
            intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            resultLauncher.launch(intent)
        }

        val pickMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                // Callback is invoked after the user selects a media item or closes the photo picker.
                if (uri != null) {
                    imageUri = uri
                    Log.d("PhotoPicker", "Selected URI: $imageUri")
                    binding.imageView.setImageURI(imageUri)
                    fileNameUpload = generateFileName()
                } else Log.d("PhotoPicker", "No media selected")
            }

        binding.galeria.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        val startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val intent = result.data
                    Log.d("AddClothes", intent?.extras?.getStringArrayList("result").toString())
                }
            }

        binding.categoria.setOnClickListener {
            Log.d("AddClothes", "Add category")
            startForResult.launch(Intent(this, AddCategories::class.java))

        }
        binding.estilo.setOnClickListener {
            intent = Intent(this, AddStyle::class.java)
            Log.d("AddClothes", "Add Style")
            startActivity(intent)
        }
        binding.detalles.setOnClickListener {
            intent = Intent(this, AddDetails::class.java)
            Log.d("AddClothes", "Add Details")
            startActivity(intent)
        }
    }

    private fun generateFileName(): String {
        return "IMG_" + UUID.randomUUID() + ".png"
    }

    private fun createImageUri(): Uri {
        //or filesDir -> carpeta interna
        //or externalMediaDirs -> carpeta media
        //or getExternalFilesDir(null) -> carpeta data/files
        //or getExternalFilesDir(Environment.DIRECTORY_PICTURES)} -> carpeta data/files/Pictures
        val dir = File("${getExternalFilesDir(null)}/Photos")
        if (!dir.exists()) dir.mkdirs()
        val imageFile = File(dir, generateFileName())
        return FileProvider.getUriForFile(this, "com.apm.ropapp.FileProvider", imageFile)
    }

    private fun uploadPhoto() {
        val uploadTask = storage.child("Photos/$fileNameUpload").putFile(imageUri)
        // Register observers to listen for when the upload is done or if it fails
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
            Log.d("PhotoUpdate", "Upload Failed: ${it.stackTrace}")
        }.addOnSuccessListener {
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            Log.d("PhotoUpdate", "Upload Success: $fileNameUpload")
        }
    }

    private fun uploadNewClothes(data: HashMap<String, Any>) {
        if (firebaseAuth.currentUser != null) {
            val uploadTask = database.child("clothes").child(firebaseAuth.currentUser!!.uid)
                .child(UUID.randomUUID().toString()).setValue(data)
            // Register observers to listen for when the upload is done or if it fails
            uploadTask.addOnFailureListener {
                // Handle unsuccessful uploads
                Log.d("DatabaseUpdate", "Upload Failed: ${it.stackTrace}")
            }.addOnSuccessListener {
                // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
                Log.d("DatabaseUpdate", "Upload Success: $data")
            }
        }
    }
}