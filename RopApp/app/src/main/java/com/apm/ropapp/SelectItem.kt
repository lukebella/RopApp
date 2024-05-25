package com.apm.ropapp

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apm.ropapp.databinding.SelectItemBinding
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class SelectItem : AppCompatActivity() {

    private lateinit var binding: SelectItemBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var storage: StorageReference
    private val dataList = mutableListOf<HashMap<String, Any>>()
    private val imageList = hashMapOf<String, Uri>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = SelectItemBinding.inflate(layoutInflater)
        val root: View = binding.root
        setContentView(root)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance(getString(R.string.database_url)).reference
        storage = FirebaseStorage.getInstance(getString(R.string.storage_url)).reference
        val userUid = firebaseAuth.currentUser?.uid

        val relatedCategories = mapOf(
            getString(R.string.prendaTop) to listOf(getString(R.string.prendaDress)),
            getString(R.string.prendaOuterwear) to listOf(),
            getString(R.string.prendaBottom) to listOf(),
            getString(R.string.prendaShoe) to listOf(),
            getString(R.string.prendaAccessories) to listOf(
                getString(R.string.prendaBag),
                getString(R.string.prendaHat),
                getString(R.string.prendaSocks),
                getString(R.string.prendaBelts),
                getString(R.string.prendaScarf),
                getString(R.string.prendaGloves),
                getString(R.string.prendaGlasses),
                getString(R.string.prendaWatch)
            ),
        )

        binding.floatingBackButton.setOnClickListener {
            Log.d("SelectItem", "Back to Create Outfit")
            finish()
        }

        suspend fun getImageUri(fileName: String, photos: StorageReference): Uri {
            val dir = File("${root.context.getExternalFilesDir(null)}/clothes")
            if (!dir.exists()) dir.mkdirs()
            val imageFile = File(dir, fileName)

            if (!imageFile.exists()) {
                withContext(Dispatchers.IO) {
                    imageFile.createNewFile()
                    photos.child(fileName).getFile(imageFile)
                }
            }
            return FileProvider.getUriForFile(
                root.context, "com.apm.ropapp.FileProvider", imageFile
            )
        }

        database.child("clothes/$userUid").addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val data = snapshot.getValue<HashMap<String, HashMap<String, Any>>>()
                //{aa3722c9-f5c9-4d6c-b5dc-f04bce8d29b3={seasons=[Verano, Invierno], photo=IMG_67929f11-76ac-452e-bcc3-101fa466ed2a.png,
                // details={size=L, price=20 €, state=Prestado, brand=M&M}, style=[Vintage, Glamorous], category=[Scarf]}}
                if (data != null) {
                    val photos = storage.child("clothes")
                    val asyncData = data.map { (key, value) ->
                        lifecycleScope.async {
                            value["id"] = key
                            dataList.add(value)
                            if (value["photo"] != null) {
                                val photoKey = value["photo"].toString()
                                val photoUri = getImageUri(photoKey, photos)
                                Pair(photoKey, photoUri)
                            }
                            else null
                        }
                    }
                    lifecycleScope.launch {
                        val uris = asyncData.awaitAll()
                        uris.filterNotNull().forEach { (photoKey, photoUri) ->
                            imageList[photoKey] = photoUri
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("SelectItem", "Failed to read value.", error.toException())
            }
        })

        val recyclerView: RecyclerView = root.findViewById(R.id.recycler_view)
        recyclerView.adapter = SelectAdapter(mutableListOf(), hashMapOf(), this)
        recyclerView.autoFitColumns(150)

        binding.chipGroup.setOnCheckedStateChangeListener { group, checkedId ->
            val chip = group.findViewById<Chip>(checkedId[0])
            val selectedCategory = chip?.text.toString()

            // Get the list of related categories
            val categories = relatedCategories[selectedCategory]?.toMutableList() ?: mutableListOf()
            categories.add(selectedCategory) // Add the selected category to the list

            // Filter the data based on the selected category and its related categories
            if (dataList.isNotEmpty()) {
                val filteredData = dataList.filter { data ->
                    val itemCategories = data["category"] as? List<*>
                    itemCategories?.any { it in categories } ?: false
                }
                recyclerView.adapter = SelectAdapter(filteredData, imageList, this)
            }
        }

    }

    private fun RecyclerView.autoFitColumns(columnWidth: Int) {
        val displayMetrics = this.context.resources.displayMetrics
        val noOfColumns =
            ((displayMetrics.widthPixels / displayMetrics.density) / columnWidth).toInt()
        this.layoutManager = GridLayoutManager(this.context, noOfColumns)
    }
}