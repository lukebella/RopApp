package com.apm.ropapp.ui.closet

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apm.ropapp.R
import com.apm.ropapp.databinding.FragmentClosetBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File


class ClosetFragment : Fragment() {

    private var _binding: FragmentClosetBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var storage: StorageReference
    private lateinit var buttonSelected: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentClosetBinding.inflate(inflater, container, false)
        val root: View = binding.root
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance(getString(R.string.database_url)).reference
        storage = FirebaseStorage.getInstance(getString(R.string.storage_url)).reference

        buttonSelected = binding.button1
        buttonSelected.isSelected = true
        val userUid = firebaseAuth.currentUser?.uid

        val recyclerView: RecyclerView = binding.root.findViewById(R.id.recycler_view)
        recyclerView.adapter = CustomAdapter(mutableListOf(), mutableListOf())
        recyclerView.autoFitColumns(150)

        fun getImageUri(fileName: String, photos: StorageReference): Uri {
            //or filesDir -> carpeta interna
            //or externalMediaDirs -> carpeta media
            //or getExternalFilesDir(null) -> carpeta data/files
            //or getExternalFilesDir(Environment.DIRECTORY_PICTURES)} -> carpeta data/files/Pictures
            val dir = File("${root.context.getExternalFilesDir(null)}/clothes")
            if (!dir.exists()) dir.mkdirs()
            val imageFile = File(dir, fileName)
            if (!imageFile.exists()) {
                imageFile.createNewFile()
                photos.child(fileName).getFile(imageFile)
            }
            return FileProvider.getUriForFile(
                root.context,
                "com.apm.ropapp.FileProvider",
                imageFile
            )
        }

        fun getDatabaseValues(folderName: String) {
            database.child("$folderName/$userUid")
                .addValueEventListener(object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        val data = snapshot.getValue<HashMap<String, HashMap<String, Any>>>()
                        //{aa3722c9-f5c9-4d6c-b5dc-f04bce8d29b3={seasons=[Verano, Invierno], photo=IMG_67929f11-76ac-452e-bcc3-101fa466ed2a.png,
                        // details={size=L, price=20 €, state=Prestado, brand=M&M}, style=[Vintage, Glamorous], category=[Scarf]}}
                        if (data != null) {
                            val nameList = mutableListOf<String>()
                            val imageList = mutableListOf<Uri>()
                            val photos = storage.child(folderName)

                            data.values.forEach { value ->
                                nameList.add(value["category"].toString())
                                if (value["photo"] == null) imageList.add(Uri.EMPTY)
                                else imageList.add(getImageUri(value["photo"].toString(), photos))
                            }

                            Log.d("TAG", "Value is: $imageList")
                            recyclerView.adapter = CustomAdapter(nameList, imageList)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.w("TAG", "Failed to read value.", error.toException())
                    }
                })
        }

        getDatabaseValues("clothes")

        binding.button1.setOnClickListener {
            selectButton(binding.button1)
            getDatabaseValues("clothes")
            //recyclerView.adapter = CustomAdapter(stringList, imageList)
        }
        binding.button2.setOnClickListener {
            selectButton(binding.button2)
            getDatabaseValues("outfits")
            //recyclerView.adapter = CustomAdapter(stringList2, imageList2)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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