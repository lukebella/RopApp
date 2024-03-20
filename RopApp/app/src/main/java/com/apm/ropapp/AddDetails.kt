package com.apm.ropapp

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.apm.ropapp.databinding.AddchoicedetailsBinding

class AddDetails : AppCompatActivity() {

    private lateinit var spinnerTamano: Spinner
    private lateinit var spinnerEstado: Spinner

    private lateinit var binding: AddchoicedetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddchoicedetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        spinnerEstado = binding.estadoSpinner
        spinnerTamano = binding.tamanoSpinner

        binding.backButtonChoice.setOnClickListener {
            intent = Intent(this, AddClothes::class.java)
            startActivity(intent)
        }

        binding.confirm.setOnClickListener {
            intent = Intent(this, AddClothes::class.java)
            startActivity(intent)
        }

        spinnerAdapter(R.array.estado_options, spinnerEstado, "Selecciona Estado")
        spinnerAdapter(R.array.tamano_options, spinnerTamano, "Selecciona Tamaño")

    }


    private fun spinnerAdapter(options: Int, spinner: Spinner, text: String) {
        ArrayAdapter.createFromResource(
            this,
            options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Especificar el diseño a utilizar cuando aparece la lista de opciones
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            // Aplicar el adaptador al Spinner
            spinner.adapter = adapter
        }

        // Manejar la selección del Spinner
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                val selectedGender = text+parent.getItemAtPosition(position).toString()
                Toast.makeText(applicationContext, selectedGender, Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No hacer nada si no se selecciona ningún elemento
            }
        }

    }
}
