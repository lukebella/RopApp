package com.apm.ropapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.apm.ropapp.databinding.EditprofileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class EditProfile : AppCompatActivity() {
    private lateinit var binding: EditprofileBinding
    private lateinit var databaseReference: DatabaseReference
    private var selectedGender: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EditprofileBinding.inflate(layoutInflater)
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
        binding.saveButton2.setOnClickListener {
            Log.d("EditProfile", "Edit Profile")

            // Get the updated username from the EditText
            val updatedUsername = binding.editUsername.text.toString()
            val updatedBirthdate = binding.editBirthdate.text.toString()

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
}
