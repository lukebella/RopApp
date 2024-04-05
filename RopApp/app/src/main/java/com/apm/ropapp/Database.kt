package com.apm.ropapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.apm.ropapp.databinding.DatabaseBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Database : AppCompatActivity() {
    lateinit var binding: DatabaseBinding
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DatabaseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpView()
    }


    private fun setUpView() {
        // Inicializa Firebase
        database = FirebaseDatabase.getInstance("https://ropapp-743fd-default-rtdb.europe-west1.firebasedatabase.app").reference
        setUpActions()
        database.child("ruta/data").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val dato = snapshot.getValue(String::class.java)
                // Manejar el dato leído
                binding.textId.text = dato
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar errores de lectura
                Log.d("TAG",error.message)
            }
        })
    }

    private fun setUpActions() {
        binding.uploadButtonId.setOnClickListener {
            uploadData()
        }
        binding.downloadButtonId.setOnClickListener {
            downloadData()
        }
        binding.updateButtonId.setOnClickListener {
            updateData("ACTUALIZACIÓN DE RDB")
        }
        binding.removeButtonId.setOnClickListener {
            removeData()
        }
    }

    // Escribe datos en la base de datos
    private fun uploadData() {
        val data = "¡HOLA RDB!"
        database.child("ruta/data").setValue(data)
    }

    // Lee datos de la base de datos
    private fun downloadData() {

    }

    // Actualiza datos en la base de datos
    private fun updateData(newData: String) {
        val actualizacion = HashMap<String, Any>()
        actualizacion["data"] = newData
        database.child("ruta").updateChildren(actualizacion)
    }

    // Elimina datos de la base de datos
    private fun removeData() {
        val data = "RDB ELIMINADA"
        database.child("ruta/data").removeValue()
        database.child("ruta/data").setValue(data)
    }
}