package com.apm.ropapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
    private lateinit var recyclerView: RecyclerView
    private lateinit var selectedChip: Chip
    private lateinit var relatedCategories: Map<String, List<String>>
    private val dataList = mutableListOf<HashMap<String, Any>>()
    private val imageList = mutableMapOf<String, Uri>()
    private val imageUriCache = mutableMapOf<String, Uri>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SelectItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance(getString(R.string.database_url)).reference
        storage = FirebaseStorage.getInstance(getString(R.string.storage_url)).reference

        recyclerView = binding.recyclerView
        recyclerView.adapter = SelectAdapter(mutableListOf(), hashMapOf(), this)
        recyclerView.autoFitColumns(150)

        relatedCategories = mapOf(
            getString(R.string.prendaTop) to listOf(getString(R.string.prendaDress)),
            getString(R.string.prendaOuterwear) to listOf(),
            getString(R.string.prendaBottom) to listOf(),
            getString(R.string.prendaShoes) to listOf(),
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

        val chipId = when (intent.getStringExtra("selectedChip")) {
            getString(R.string.prendaTop) -> R.id.chipTop
            getString(R.string.prendaOuterwear) -> R.id.chipOuterwear
            getString(R.string.prendaBottom) -> R.id.chipBottom
            getString(R.string.prendaShoes) -> R.id.chipShoes
            getString(R.string.prendaAccessories) -> R.id.chipAccessories
            else -> R.id.chipTop
        }
        selectedChip = binding.chipGroup.findViewById(chipId)
        selectedChip.isChecked = true
        binding.textView.text = getString(R.string.elegirPrenda, selectedChip.text)

        firebaseAuth.currentUser?.uid?.let { userUid ->
            database.child("clothes/$userUid").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue<HashMap<String, HashMap<String, Any>>>()?.let { data ->
                        val photos = storage.child("clothes")
                        val asyncData = data.map { (key, value) ->
                            lifecycleScope.async {
                                value["id"] = key
                                dataList.add(value)
                                value["photo"]?.toString()?.let { photoKey ->
                                    Pair(photoKey, getImageUri(photoKey, photos))
                                }
                            }
                        }
                        lifecycleScope.launch {
                            asyncData.awaitAll().filterNotNull().forEach { (photoKey, photoUri) ->
                                imageList[photoKey] = photoUri
                            }
                            updateRecyclerView()
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.w("SelectItem", "Failed to read value.", error.toException())
                }
            })
        }

        binding.chipGroup.setOnCheckedStateChangeListener { group, checkedId ->
            if (checkedId.isNotEmpty()) {
                selectedChip = group.findViewById(checkedId[0])
                updateRecyclerView()
            }
        }

        binding.floatingBackButton.setOnClickListener {
            finish()
        }

        binding.buttonRemove.setOnClickListener {
            val intent = Intent()
            setResult(RESULT_FIRST_USER, intent)
            finish()
        }

    }

    private fun RecyclerView.autoFitColumns(columnWidth: Int) {
        val displayMetrics = this.context.resources.displayMetrics
        val noOfColumns =
            ((displayMetrics.widthPixels / displayMetrics.density) / columnWidth).toInt()
        this.layoutManager = GridLayoutManager(this.context, noOfColumns)
    }

    private suspend fun getImageUri(fileName: String, photos: StorageReference): Uri {
        // Check if the URI is in the cache
        if (imageUriCache.containsKey(fileName)) return imageUriCache[fileName]!!

        val dir = File("${binding.root.context.getExternalFilesDir(null)}/clothes")
        if (!dir.exists()) dir.mkdirs()
        val imageFile = File(dir, fileName)

        if (!imageFile.exists()) {
            withContext(Dispatchers.IO) {
                imageFile.createNewFile()
                photos.child(fileName).getFile(imageFile)
            }
        }
        val uri = FileProvider.getUriForFile(
            binding.root.context, "com.apm.ropapp.FileProvider", imageFile
        )
        imageUriCache[fileName] = uri
        return uri
    }

    private fun updateRecyclerView() {
        val selectedCategory = selectedChip.text.toString()

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