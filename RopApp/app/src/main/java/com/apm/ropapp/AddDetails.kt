package com.apm.ropapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.apm.ropapp.databinding.AddchoicedetailsBinding

class AddDetails : AppCompatActivity() {

    private lateinit var spinnerTamano: Spinner
    private lateinit var spinnerEstado: Spinner
    private lateinit var selectedEstado: String
    private lateinit var selectedTamano: String

    private lateinit var binding: AddchoicedetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddchoicedetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        spinnerEstado = binding.estadoSpinner
        spinnerTamano = binding.tamanoSpinner

        binding.backButtonChoice.setOnClickListener {
            navigateToAddClothes()
        }

        binding.confirm.setOnClickListener {
            confirmDetails()
        }

        spinnerAdapter(R.array.estado_options, spinnerEstado) { estado ->
            selectedEstado = estado
        }
        spinnerAdapter(R.array.tamano_options, spinnerTamano) { tamano ->
            selectedTamano = tamano
        }
    }

    private fun spinnerAdapter(options: Int, spinner: Spinner, onItemSelected: (String) -> Unit) {
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
                val selectedValue = parent.getItemAtPosition(position).toString()
                onItemSelected(selectedValue)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No hacer nada si no se selecciona ningún elemento
            }
        }
    }

    private fun navigateToAddClothes() {
        intent = Intent(this, AddClothes::class.java)
        Log.d("AddDetails","Back to Add Clothes")
        finish()
    }

    private fun confirmDetails() {
        val details = HashMap<String, Any>()

        // Agregar los valores de los EditText al mapa details
        binding.marca.text?.toString()?.let { details["brand"] = it }
        binding.precio.text?.toString()?.let { details["price"] = it }
        binding.colores.text?.toString()?.let { details["colors"] = it }

        // Agregar los valores seleccionados de los spinners al mapa details
        selectedEstado.let { details["state"] = it }
        selectedTamano.let { details["size"] = it }

        // Crear el intent y pasar el mapa details como extra
        val intent = Intent(this, AddClothes::class.java)
        intent.putExtra("details", details)
        setResult(RESULT_OK, intent)

        Log.d("AddDetails", "Details Added: $details")
        finish()
    }

}
