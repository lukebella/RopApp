package com.apm.ropapp

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.apm.ropapp.databinding.EditprofileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage


class EditProfile : AppCompatActivity() {
    private lateinit var binding: EditprofileBinding
    private lateinit var databaseReference: DatabaseReference
    private var selectedGender: String? = null
    private var imageUri: Uri? = null
    private var userId = FirebaseAuth.getInstance().currentUser?.uid


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EditprofileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener una referencia a la base de datos de Firebase para los datos del user actual
        databaseReference = userId?.let {
            FirebaseDatabase.getInstance().reference.child("users").child(it)
        }!!

        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.providerData?.forEach { userInfo ->
            if (userInfo.providerId == getString(R.string.facebookProvider) || userInfo.providerId == getString(R.string.googleProvider)) {
                binding.passwordChange.visibility = View.GONE
                Log.d("EditProfile", "Not allowed to change password")
                Toast.makeText(
                    applicationContext,
                    "No es posible modificar la contraseña, usuario registrado con google",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


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
                    val imageUrl = dataSnapshot.child("image").getValue(String::class.java)
                    if (!imageUrl.isNullOrEmpty()) {
                        val imageRef =
                            FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
                        imageRef.getBytes(10 * 1024 * 1024).addOnSuccessListener { bytes ->
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            binding.editImage.setImageBitmap(bitmap)
                        }.addOnFailureListener {
                            // Handle any errors
                            Toast.makeText(
                                applicationContext,
                                "Image upload failed: ${it.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
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
                view: View?,
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

        val pickImageLauncher =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                // Callback is invoked after the user selects a media item or closes the photo picker.
                if (uri != null) {
                    imageUri = uri
                    Log.d("PhotoPicker", "Selected URI: $imageUri")
                    binding.editImage.setImageURI(imageUri)
                } else Log.d("PhotoPicker", "No media selected")
            }

        binding.editImage.setOnClickListener {
            pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
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
            finish()
        }


        binding.passwordChange.setOnClickListener {

            intent = Intent(this, EditPassword::class.java)
            Log.d("EditProfile", "Change Password")
            startActivity(intent)
        }

        // Similarly, initialize other views and set their behavior
    }

    private fun uploadImageToFirebase(databaseReference: DatabaseReference = this.databaseReference) {
        imageUri?.let { uri ->
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
            val imageRef = storageRef.child("user_images/${userId}.jpg")

            val uploadTask = imageRef.putFile(uri)
            uploadTask.addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    // Get the download URL and store it in the database
                    Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                    databaseReference.child("image").setValue(downloadUrl.toString())
                }.addOnFailureListener {
                    // Handle any errors
                    Toast.makeText(this, "Failed to get download URL: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                // Handle any errors
                Toast.makeText(this, "Image upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        } ?: Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
    }

}

