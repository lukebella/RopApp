package com.apm.ropapp

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.apm.ropapp.databinding.UserinfoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class UserInfo : AppCompatActivity() {

    private lateinit var binding: UserinfoBinding
    private lateinit var databaseReference: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = UserinfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userId = FirebaseAuth.getInstance().currentUser?.uid

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
                    val selectedGender = dataSnapshot.child("gender").getValue(String::class.java)
                    val email = dataSnapshot.child("email").getValue(String::class.java)

                    // Llenar los campos de EditText con la información del usuario actual
                    username?.let { binding.username.setText(it) }
                    birthdate?.let { binding.userBirthdate.setText(it) }
                    selectedGender?.let { binding.userGenero.setText(it) }
                    email?.let { binding.userEmail.setText(it) }
                    val imageUrl = dataSnapshot.child("image").getValue(String::class.java)
                    if (!imageUrl.isNullOrEmpty()) {
                        val imageRef =
                            FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
                        imageRef.getBytes(10 * 1024 * 1024).addOnSuccessListener { bytes ->
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            binding.imageButton.setImageBitmap(bitmap)
                        }.addOnFailureListener {
                            // Handle any errors
                            Toast.makeText(
                                applicationContext,
                                "Image upload failed: ${it.message}",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar la cancelación del evento de obtención de datos
            }
        })

        binding.backButtonInfo.setOnClickListener {
            finish()
        }

    }
}