package com.apm.ropapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.apm.ropapp.databinding.GooglecompleteregisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CompleteData : AppCompatActivity() {

    private lateinit var binding: GooglecompleteregisterBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private var selectedGender: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = GooglecompleteregisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance(getString(R.string.database_url)).reference

        // Crear un ArrayAdapter utilizando el array de opciones y un diseño predefinido para los elementos de lista
        ArrayAdapter.createFromResource(
            this,
            R.array.genero_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Especificar el diseño a utilizar cuando aparece la lista de opciones
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            // Aplicar el adaptador al Spinner
            binding.spinnerGenero.adapter = adapter
        }

        // Manejar la selección del Spinner
        binding.spinnerGenero.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                selectedGender = parent.getItemAtPosition(position).toString()
                Log.d("SignUp", "Se seleccionó el género: $selectedGender")
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No hacer nada si no se selecciona ningún elemento
            }
        }

        // Configurar el clic del botón para navegar a MainActivity
        binding.continueButton.setOnClickListener {
            Log.d("SignUp", "Se seleccionó el botón Crear Cuenta")


            val birthdate = binding.editTextDate.text.toString()

            if(birthdate.isNotEmpty()){

                firebaseAuth.currentUser?.uid?.let { it1 -> writeGoogleUser(it1,birthdate, selectedGender!!) }
                finish()

            }else{
                Toast.makeText(this, "Debe rellenar todos los campos",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun writeGoogleUser(userId: String, birthdate: String, selectedGender: String) {
        val userData = HashMap<String, Any>()
        userData["email"] = firebaseAuth.currentUser?.email.toString()
        userData["username"] = firebaseAuth.currentUser?.displayName.toString()
        userData["birthdate"] = birthdate
        userData["gender"] = selectedGender

        database.child("users").child(userId).setValue(userData)
    }

}
