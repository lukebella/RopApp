package com.apm.ropapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EditprofileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Obtener una referencia a la base de datos de Firebase para los datos del user actual
        databaseReference = userId?.let {
            FirebaseDatabase.getInstance().reference.child("users").child(it) }!!

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Verificar si los datos existen en la base de datos
                if (dataSnapshot.exists()) {
                    // Obtener los datos del usuario actual
                    val username = dataSnapshot.child("username").getValue(String::class.java)

                    // Llenar los campos de EditText con la información del usuario actual
                    username?.let { binding.editUsername.setText(it) }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar la cancelación del evento de obtención de datos
            }
        })

        // Initialize your views here
        // For example, to set a click listener on the floating action button:
        binding.cancelButton2.setOnClickListener {
            Log.d("EditProfile", "Back to Profile")
            intent = Intent(this, MainActivity::class.java)
            intent.putExtra("fragment", R.id.navigation_user)
            startActivity(intent)
        }
        binding.saveButton2.setOnClickListener {
            Log.d("EditProfile", "Edit Profile")
            intent = Intent(this, MainActivity::class.java) // Maybe editOutfit?
            intent.putExtra("fragment", R.id.navigation_user)
            startActivity(intent)
        }

        binding.editAddress.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                val address = binding.editAddress.text.toString()
                Log.d("EditProfile", "Address: $address")
                // You can now use the address variable
            }
        }

        binding.editPhone.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                val phone = binding.editPhone.text.toString()
                Log.d("EditProfile", "Phone: $phone")
                // You can now use the phone variable
            }
        }

        binding.editGender.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                val email = binding.editGender.text.toString()
                Log.d("EditProfile", "Email: $email")
                // You can now use the email variable
            }
        }

        binding.passwordChange.setOnClickListener() {
            intent = Intent(this, EditPassword::class.java)
            Log.d("EditProfile", "Change Password")
            startActivity(intent)
        }

        // Similarly, initialize other views and set their behavior
    }
}
