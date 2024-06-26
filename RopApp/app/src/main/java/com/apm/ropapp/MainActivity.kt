package com.apm.ropapp

import CustomDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.apm.ropapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Log.d("Main Activity", "No user logged")
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        } else {
            Log.d("Main Activity", "User logged")
            Toast.makeText(this, "Identificado como ${user.email}", Toast.LENGTH_SHORT).show()
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController
        binding.navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.navigation_calendar || destination.id == R.id.navigation_user) {
                binding.floatingAddButton.hide()
            } else {
                binding.floatingAddButton.show()
            }
        }

        binding.floatingAddButton.setOnClickListener {
            Log.d("Main Activity", "Se hizo clic en el botón flotante")
            showCustomDialog()
        }
    }

    private fun showCustomDialog() {
        // Crear e instanciar el Custom Dialog
        val customDialog = CustomDialog(this)
        // Mostrar el Custom Dialog
        customDialog.show()
    }
}