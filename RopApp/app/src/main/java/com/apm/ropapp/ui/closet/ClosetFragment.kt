package com.apm.ropapp.ui.closet

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apm.ropapp.R
import com.apm.ropapp.databinding.FragmentClosetBinding
import com.apm.ropapp.utils.CLOTHES
import com.apm.ropapp.utils.ImageUtils
import com.apm.ropapp.utils.OUTFITS
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch


class ClosetFragment : Fragment() {

    private var _binding: FragmentClosetBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var storage: StorageReference
    private lateinit var buttonSelected: Button
    private var currentCloset: String? = null
    private var valueEventListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentClosetBinding.inflate(inflater, container, false)
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance(getString(R.string.database_url)).reference
        storage = FirebaseStorage.getInstance(getString(R.string.storage_url)).reference

        buttonSelected = binding.button1
        buttonSelected.isSelected = true
        val userUid = firebaseAuth.currentUser?.uid

        val startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    Log.d("EditClothes", "Updated")
                } else Log.d("EditClothes", "Cancelled")
            }

        val recyclerView: RecyclerView = binding.recyclerView
        recyclerView.adapter = ClosetAdapter(mutableListOf(), mutableListOf(), startForResult, CLOTHES)
        recyclerView.autoFitColumns(150)

        fun getDatabaseValues(folderName: String) {
            val newCloset = "$folderName/$userUid"
            if (newCloset == currentCloset) return
            // If there's a previous listener, remove it from the previous path
            if (valueEventListener != null && currentCloset != null)
                database.child(currentCloset!!).removeEventListener(valueEventListener!!)
            currentCloset = newCloset

            valueEventListener =
                database.child(newCloset).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        val data = snapshot.getValue<HashMap<String, HashMap<String, Any>>>()
                        //{aa3722c9-f5c9-4d6c-b5dc-f04bce8d29b3={seasons=[Verano, Invierno], photo=IMG_67929f11-76ac-452e-bcc3-101fa466ed2a.png,
                        // details={size=L, price=20 €, state=Prestado, brand=M&M}, style=[Vintage, Glamorous], category=[Scarf]}}
                        if (data != null) {
                            val dataList = mutableListOf<HashMap<String, Any>>()
                            val imageList = mutableListOf<Uri>()
                            val photos = storage.child(folderName)

                            val asyncData = data.map { (key, value) ->
                                lifecycleScope.async {
                                    value["id"] = key
                                    dataList.add(value)
                                    if (value["photo"] == null) Uri.EMPTY
                                    else ImageUtils.getImageUri(value["photo"].toString(), folderName,
                                        photos, requireContext())
                                }
                            }
                            lifecycleScope.launch {
                                val uris = asyncData.awaitAll()
                                imageList.addAll(uris)
                                Log.d("Closet", "URIs are: $imageList")
                                recyclerView.adapter =
                                    ClosetAdapter(dataList, imageList, startForResult, folderName)
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Log.w("Closet", "Failed to read value.", error.toException())
                    }
                })
        }

        getDatabaseValues(CLOTHES)

        binding.button1.setOnClickListener {
            selectButton(binding.button1)
            getDatabaseValues(CLOTHES)
        }
        binding.button2.setOnClickListener {
            selectButton(binding.button2)
            getDatabaseValues(OUTFITS)
        }

        return binding.root
    }

    private fun selectButton(newButton: Button) {
        buttonSelected.isSelected = false
        newButton.isSelected = true
        buttonSelected = newButton
    }

    private fun RecyclerView.autoFitColumns(columnWidth: Int) {
        val displayMetrics = this.context.resources.displayMetrics
        val noOfColumns =
            ((displayMetrics.widthPixels / displayMetrics.density) / columnWidth).toInt()
        this.layoutManager = GridLayoutManager(this.context, noOfColumns)
    }
}