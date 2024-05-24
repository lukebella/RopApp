package com.apm.ropapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.apm.ropapp.databinding.EditpasswordBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth


class EditPassword : AppCompatActivity() {
    private lateinit var binding: EditpasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EditpasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var newPassword = ""
        var oldPassword = ""

        // Initialize your views here
        // For example, to set a click listener on the floating action button:
        binding.cancelButton3.setOnClickListener {
            Log.d("EditPassword", "Back to EditProfile")
            finish()
        }
        binding.saveButton3.setOnClickListener {
            Log.d("EditPassword", "Change Password")

            val user = FirebaseAuth.getInstance().currentUser
            val email: String? = user?.email
            val credential = email?.let { it1 -> EmailAuthProvider.getCredential(it1, oldPassword) }

            user!!.reauthenticate(credential!!)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        user.updatePassword(newPassword)
                            .addOnCompleteListener {
                                if (!it.isSuccessful) {
                                    Toast.makeText(this, "Ha ocurrido un error. Vuelva a " +
                                            "intentarlo más tarde", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this, "Contraseña modificada con éxito", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(this, "Autenticación fallida", Toast.LENGTH_SHORT).show()
                    }
                }
            intent = Intent(this, MainActivity::class.java) // Maybe editOutfit?
            intent.putExtra("fragment", R.id.navigation_user)
            startActivity(intent)
        }

        binding.newPassword.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                 newPassword = binding.newPassword.text.toString()
                Log.d("EditPassword", "New Password: $newPassword")
                // You can now use the newPassword variable
            }
        }

        binding.oldPassword.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                oldPassword = binding.oldPassword.text.toString()
                Log.d("EditPassword", "Old Password: $oldPassword")
                // You can now use the newPassword variable
            }
        }

    }
}