package com.apm.ropapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.apm.ropapp.databinding.AddchoicedetailsBinding

class AddDetails : AppCompatActivity() {

    private lateinit var binding: AddchoicedetailsBinding
    private lateinit var selectedEstado: String
    private lateinit var selectedTamano: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddchoicedetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        spinnerAdapter(R.array.estado_options, binding.estadoSpinner) {
            selectedEstado = it
        }
        spinnerAdapter(R.array.tamano_options, binding.tamanoSpinner) {
            selectedTamano = it
        }

        // Retrieve the "details" HashMap from the intent extras
        val detailsData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            intent.extras?.getSerializable("details", HashMap<String, Any>().javaClass)
        else intent.extras?.getSerializable("details")

        // Set the values to the corresponding views
        if (detailsData != null && detailsData is HashMap<*, *>) {
            binding.marca.setText(detailsData["brand"]?.toString() ?: "")
            binding.precio.setText(detailsData["price"]?.toString() ?: "")
            binding.colores.setText(detailsData["colors"]?.toString() ?: "")
            selectedEstado = detailsData["state"]?.toString() ?: ""
            selectedTamano = detailsData["size"]?.toString() ?: ""

            // Set the spinner values from the details HashMap
            binding.estadoSpinner.setSelection(
                getSpinnerIndex(
                    binding.estadoSpinner,
                    selectedEstado
                )
            )
            binding.tamanoSpinner.setSelection(
                getSpinnerIndex(
                    binding.tamanoSpinner,
                    selectedTamano
                )
            )
        }

        binding.backButtonChoice.setOnClickListener {
            Log.d("AddDetails", "Back to Add Clothes")
            finish()
        }

        binding.confirm.setOnClickListener {
            val details = HashMap<String, Any>()

            // Agregar los valores de los EditText al mapa details
            binding.marca.text?.toString()?.let { details["brand"] = it }
            binding.precio.text?.toString()?.let { details["price"] = it }
            binding.colores.text?.toString()?.let { details["colors"] = it }

            // Agregar los valores seleccionados de los spinners al mapa details
            selectedEstado.let { details["state"] = it }
            selectedTamano.let { details["size"] = it }

            // Crear el intent y pasar el mapa details como extra
            val intent = Intent()
            intent.putExtra("details", details)
            setResult(RESULT_OK, intent)

            Log.d("AddDetails", "Details Added: $details")
            finish()
        }

    }

    private fun getSpinnerIndex(spinner: Spinner, value: String): Int {
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i).toString() == value) {
                return i
            }
        }
        return 0
    }

    private fun spinnerAdapter(options: Int, spinner: Spinner, onItemSelected: (String) -> Unit) {
        ArrayAdapter.createFromResource(
            this, options, android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Especificar el diseño a utilizar cuando aparece la lista de opciones
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            // Aplicar el adaptador al Spinner
            spinner.adapter = adapter
        }

        // Manejar la selección del Spinner
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long
            ) {
                val selectedValue = parent.getItemAtPosition(position).toString()
                onItemSelected(selectedValue)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No hacer nada si no se selecciona ningún elemento
            }
        }
    }

}
