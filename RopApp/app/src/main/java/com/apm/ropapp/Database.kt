package com.apm.ropapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.apm.ropapp.databinding.DatabaseBinding
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.initialize
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException
import java.util.UUID


class Database : AppCompatActivity() {
    lateinit var binding: DatabaseBinding
    private lateinit var database: DatabaseReference
    private lateinit var storage: StorageReference

    private var imageView: ImageView? = null
    // Uri indicates, where the image will be picked from
    private var filePath: Uri? = null
    // request code
    private val PICK_IMAGE_REQUEST = 22

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DatabaseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpView()
    }


    private fun setUpView() {
        // Inicializa Firebase
        Firebase.initialize(context = this)
        Firebase.appCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance(),
        )
        database = FirebaseDatabase.getInstance("https://ropapp-743fd-default-rtdb.europe-west1.firebasedatabase.app").reference
        storage = FirebaseStorage.getInstance("gs://ropapp-743fd.appspot.com").reference
        imageView = binding.imgView
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
        launchGallery()
        uploadImage()

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

    private fun launchGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if(data == null || data.data == null){
                return
            }

            filePath = data.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                imageView?.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun uploadImage(){
        if(filePath != null){
            val ref = storage.child("myImages/" + UUID.randomUUID().toString())
            val uploadTask = ref.putFile(filePath!!)

        }else{
            Toast.makeText(this, "Please Upload an Image", Toast.LENGTH_SHORT).show()
        }
    }

}