package com.apm.ropapp

import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.apm.ropapp.databinding.EditprofileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import android.Manifest
import android.os.Build
import com.google.firebase.storage.FirebaseStorage


class EditProfile : AppCompatActivity() {
    private lateinit var binding: EditprofileBinding
    private lateinit var databaseReference: DatabaseReference
    private var selectedGender: String? = null
    private var imageUri: Uri? = null
    private var userId = FirebaseAuth.getInstance().currentUser?.uid

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                openGallery()
            } else {
                Toast.makeText(
                    this,
                    "Permission denied to read your External storage",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    // Register the activity result launcher for picking images from gallery
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                imageUri = it
                binding.editImage.setImageURI(it)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EditprofileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener una referencia a la base de datos de Firebase para los datos del user actual
        databaseReference = userId?.let {
            FirebaseDatabase.getInstance().reference.child("users").child(it)
        }!!

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Verificar si los datos existen en la base de datos
                if (dataSnapshot.exists()) {
                    // Obtener los datos del usuario actual
                    val username = dataSnapshot.child("username").getValue(String::class.java)
                    val birthdate = dataSnapshot.child("birthdate").getValue(String::class.java)
                    selectedGender = dataSnapshot.child("gender").getValue(String::class.java)

                    // Llenar los campos de EditText con la información del usuario actual
                    username?.let { binding.editUsername.setText(it) }
                    birthdate?.let { binding.editBirthdate.setText(it) }
                    selectedGender?.let {
                        val spinnerPosition = when (it) {
                            "Hombre" -> 0
                            "Mujer" -> 1
                            else -> 2
                        }
                        binding.editGenero.setSelection(spinnerPosition)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar la cancelación del evento de obtención de datos
            }
        })

        // Initialize your views here
        // For example, to set a click listener on the floating action button:
        binding.cancelButton.setOnClickListener {
            Log.d("EditProfile", "Back to Profile")
            finish()
        }
        binding.editGenero.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: android.view.View?,
                position: Int,
                id: Long
            ) {
                selectedGender = parent.getItemAtPosition(position).toString()
                Log.d("EditProfile", "Se seleccionó el género: $selectedGender")
//                Toast.makeText(applicationContext, "Selected gender: $selectedGender", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No hacer nada si no se selecciona ningún elemento
            }
        }

        binding.editImage.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) == PERMISSION_GRANTED
                ) {
                    openGallery()
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            } else {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PERMISSION_GRANTED
                ) {
                    openGallery()
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }






        binding.saveButton2.setOnClickListener {
            Log.d("EditProfile", "Edit Profile")

            // Get the updated username from the EditText
            val updatedUsername = binding.editUsername.text.toString()
            val updatedBirthdate = binding.editBirthdate.text.toString()
            uploadImageToFirebase()
            // Update the username in the Firebase database
            databaseReference.child("username").setValue(updatedUsername)
            databaseReference.child("birthdate").setValue(updatedBirthdate)
            databaseReference.child("gender").setValue(selectedGender)

            // Navigate back to MainActivity
            intent = Intent(this, MainActivity::class.java)
            intent.putExtra("fragment", R.id.navigation_user)
            startActivity(intent)
        }


        binding.passwordChange.setOnClickListener() {
            for (user in FirebaseAuth.getInstance().currentUser!!
                .providerData) {
                if (user.providerId == "facebook.com" || user.providerId == "google.com") {
                    Log.d("EditProfile", "Not allowed to change password")
                    Toast.makeText(
                        applicationContext,
                        "Not possible to change password, user signed with ${user.providerId}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            intent = Intent(this, EditPassword::class.java)
            Log.d("EditProfile", "Change Password")
            startActivity(intent)
        }

        // Similarly, initialize other views and set their behavior
    }

    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun uploadImageToFirebase(databaseReference: DatabaseReference = this.databaseReference) {
        imageUri?.let { uri ->
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
            val imageRef = storageRef.child("images/${userId}.jpg")

            val uploadTask = imageRef.putFile(uri)
            uploadTask.addOnSuccessListener {
                Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                databaseReference.child("image").setValue("images/${userId}.jpg")
            }.addOnFailureListener {
                Toast.makeText(this, "Image upload failed: ${it.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
//            ?: Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
    }
}

