package com.apm.ropapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.children
import com.apm.ropapp.databinding.AddclothesBinding
import com.apm.ropapp.utils.CLOTHES
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.util.UUID
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set


class AddClothes : AppCompatActivity() {

    private lateinit var binding: AddclothesBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var storage: StorageReference
    private lateinit var clothesId: String
    private lateinit var imageUri: Uri
    private lateinit var fileNameUpload: String
    private val uploadData = HashMap<String, Any>()

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = AddclothesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance(getString(R.string.database_url)).reference
        storage = FirebaseStorage.getInstance(getString(R.string.storage_url)).reference

        val clothesData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            intent.extras?.getSerializable("clothesValues", HashMap<String, Any>().javaClass)
        else intent.extras?.getSerializable("clothesValues")

        if (clothesData != null && clothesData is HashMap<*, *>) {
            Log.d("EditClothes", clothesData.toString())
            clothesId = clothesData["id"].toString()
            clothesData.remove("id")
            clothesData.forEach { (key, value) -> uploadData[key.toString()] = value }

            binding.seasonsChipGroup.children.forEach {
                val chip = binding.seasonsChipGroup.findViewById<Chip>(it.id)
                val seasonsList = clothesData["seasons"] as ArrayList<*>
                if (seasonsList.contains(chip.text)) chip.toggle()
            }

            binding.categoryTextView.text = updateTextViewCheckbox("category")
            binding.styleTextView.text = updateTextViewCheckbox("style")
            binding.detailsTextView.text = updateTextViewDetails()

            val imageString = intent.extras?.getString("image")
            if (imageString != null) {
                imageUri = Uri.parse(imageString)
                binding.imageView.setImageURI(imageUri)
                fileNameUpload = clothesData["photo"].toString()
            }
        }

        binding.backButton.setOnClickListener {
            Log.d("AddClothes", "Back to Main Activity")
            finish()
        }

        binding.guardar.setOnClickListener {

            if (binding.seasonsChipGroup.checkedChipIds.isNotEmpty()) {
                val seasonsList = arrayListOf<String>()
                binding.seasonsChipGroup.checkedChipIds.forEach {
                    val chip = binding.seasonsChipGroup.findViewById<Chip>(it)
                    seasonsList.add(chip.text.toString())
                }
                uploadData["seasons"] = seasonsList
            }
            if (this::fileNameUpload.isInitialized) {
                uploadData["photo"] = fileNameUpload
                uploadPhoto()
            }
            uploadNewClothes(uploadData)

            val intent = Intent()
            setResult(RESULT_OK, intent)
            Log.d("AddClothes", "Dress added: $uploadData")
            finish()
        }

        binding.camara.setOnClickListener {
            // Check if the Camera permission is already available.
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                // If Camera permission is not available, request for it.
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_REQUEST_CODE
                )
            } else {
                // Launch the camera if the permission exists
                launchCamera()
            }
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
                    val extras = result.data?.extras
                    if (extras?.getStringArrayList("category") != null) {
                        uploadData["category"] = extras.getStringArrayList("category")!!
                        binding.categoryTextView.text = updateTextViewCheckbox("category")
                        Log.d("AddCategories", uploadData["category"].toString())
                    }
                    if (extras?.getStringArrayList("style") != null) {
                        uploadData["style"] = extras.getStringArrayList("style")!!
                        binding.styleTextView.text = updateTextViewCheckbox("style")
                        Log.d("AddStyle", uploadData["style"].toString())
                    }

                    val detailsData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                        extras?.getSerializable("details", HashMap<String, Any>().javaClass)
                    else extras?.getSerializable("details")
                    if (detailsData != null && detailsData is HashMap<*, *>) {
                        uploadData["details"] = detailsData
                        binding.detailsTextView.text = updateTextViewDetails()
                        Log.d("AddDetails", uploadData["details"].toString())
                    }
                }
            }

        binding.categoryLayout.setOnClickListener {
            Log.d("AddClothes", "Add Category")
            val intent = Intent(this, AddCategories::class.java)
            if (uploadData["category"] != null)
                intent.putExtra("checked", uploadData["category"] as ArrayList<*>)
            startForResult.launch(intent)
        }

        binding.styleLayout.setOnClickListener {
            Log.d("AddClothes", "Add Style")
            val intent = Intent(this, AddStyle::class.java)
            if (uploadData["style"] != null)
                intent.putExtra("checked", uploadData["style"] as ArrayList<*>)
            startForResult.launch(intent)
        }

        binding.detailsLayout.setOnClickListener {
            Log.d("AddClothes", "Add Details")
            val intent = Intent(this, AddDetails::class.java)
            if (uploadData["details"] != null)
                intent.putExtra("details", uploadData["details"] as HashMap<*, *>)
            startForResult.launch(intent)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    launchCamera()
                } else {
                    Toast.makeText(
                        this,
                        "Es necesario otorgar permisos para utilizar la cámara",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private val resultLauncher =
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

    private fun launchCamera() {
        imageUri = createImageUri()
        intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        resultLauncher.launch(intent)
    }

    private fun updateTextViewCheckbox(data: String): String {
        if (uploadData[data] != null) {
            val dataString = uploadData[data].toString()
            return dataString.substring(1, dataString.length - 1)
        }
        return ""
    }

    private fun updateTextViewDetails(): String {
        val detailsData = uploadData["details"] as? HashMap<*, *>
        return detailsData?.values?.joinToString(", ") ?: ""
    }

    private fun generateFileName(): String {
        return "IMG_" + UUID.randomUUID() + ".png"
    }

    private fun createImageUri(): Uri {
        //or filesDir -> carpeta interna
        //or externalMediaDirs -> carpeta media
        //or getExternalFilesDir(null) -> carpeta data/files
        //or getExternalFilesDir(Environment.DIRECTORY_PICTURES)} -> carpeta data/files/Pictures
        val dir = File("${getExternalFilesDir(null)}/$CLOTHES")
        if (!dir.exists()) dir.mkdirs()
        val imageFile = File(dir, generateFileName())
        return FileProvider.getUriForFile(this, "com.apm.ropapp.FileProvider", imageFile)
    }

    private fun uploadPhoto() {
        val uploadTask = storage.child("$CLOTHES/$fileNameUpload").putFile(imageUri)
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
            if (!this::clothesId.isInitialized) clothesId = UUID.randomUUID().toString()
            val uploadTask = database.child(CLOTHES).child(firebaseAuth.currentUser!!.uid)
                .child(clothesId).setValue(data)
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