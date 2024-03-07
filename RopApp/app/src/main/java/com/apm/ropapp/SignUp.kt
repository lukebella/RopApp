package com.apm.ropapp

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SignUp : AppCompatActivity() {

    private lateinit var spinnerGenero: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up)

        // Obtener referencia al Spinner desde el layout
        spinnerGenero = findViewById(R.id.spinner_genero)

        // Crear un ArrayAdapter utilizando el array de opciones y un diseño predefinido para los elementos de lista
        ArrayAdapter.createFromResource(
            this,
            R.array.genero_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Especificar el diseño a utilizar cuando aparece la lista de opciones
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            // Aplicar el adaptador al Spinner
            spinnerGenero.adapter = adapter
        }

        // Manejar la selección del Spinner
        spinnerGenero.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                val selectedGender = parent.getItemAtPosition(position).toString()
                Toast.makeText(applicationContext, "Selected gender: $selectedGender", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No hacer nada si no se selecciona ningún elemento
            }
        }
    }
}
