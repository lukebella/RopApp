package com.apm.ropapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.apm.ropapp.databinding.EditpasswordBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import org.checkerframework.checker.nullness.qual.NonNull


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
                            .addOnCompleteListener { task ->
                                if (!task.isSuccessful) {
                                    Toast.makeText(this, "Something went wrong. Please try again later", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this, "Password Successfully Modified", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(this, "Authentication Failed", Toast.LENGTH_SHORT).show()
                    }
                }
            intent = Intent(this, MainActivity::class.java) // Maybe editOutfit?
            intent.putExtra("fragment", R.id.navigation_user)
            startActivity(intent)
        }

        binding.newPassword.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                 newPassword = binding.newPassword.text.toString()
                Log.d("EditPassword", "New Password: $newPassword")
                // You can now use the newPassword variable
            }
        }

        binding.oldPassword.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                oldPassword = binding.oldPassword.text.toString()
                Log.d("EditPassword", "Old Password: $oldPassword")
                // You can now use the newPassword variable
            }
        }

    }
}