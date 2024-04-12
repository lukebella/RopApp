package com.apm.ropapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.apm.ropapp.databinding.SignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUp : AppCompatActivity() {

    private lateinit var binding: SignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private var selectedGender: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SignUpBinding.inflate(layoutInflater)
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
                Toast.makeText(applicationContext, "Selected gender: $selectedGender", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No hacer nada si no se selecciona ningún elemento
            }
        }

        // Configurar el clic del botón para navegar a MainActivity
        binding.createAccountButton.setOnClickListener {
            Log.d("SignUp", "Se seleccionó el botón Crear Cuenta")

            val email = binding.registerEmail.text.toString()
            val password = binding.registerPassword.text.toString()
            val username = binding.registerUsername.text.toString()
            val birthdate = binding.editTextDate.text.toString()

            if(email.isNotEmpty() && password.isNotEmpty()){

                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener()
                {
                    if(it.isSuccessful){

                        val uid = firebaseAuth.currentUser?.uid
                        if (uid != null && selectedGender != null){
                            writeNewUser(uid,username,email,birthdate, selectedGender!!)
                        }else{
                            Toast.makeText(this, it.exception.toString(),Toast.LENGTH_SHORT).show()
                        }


                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    }else{
                        Toast.makeText(this, it.exception.toString(),Toast.LENGTH_SHORT).show()
                    }
                }
            }else{
                Toast.makeText(this, "Debe rellenar todos los campos",Toast.LENGTH_SHORT).show()
            }


        }


    }

    private fun writeNewUser(userId: String, username: String, email: String, birthdate: String, selectedGender: String) {
        val userData = HashMap<String, Any>()
        userData["username"] = username
        userData["email"] = email
        userData["birthdate"] = birthdate
        userData["gender"] = selectedGender

        database.child("users").child(userId).setValue(userData)
    }

}
