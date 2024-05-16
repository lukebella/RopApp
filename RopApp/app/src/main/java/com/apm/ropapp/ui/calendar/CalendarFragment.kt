package com.apm.ropapp.ui.calendar

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.apm.ropapp.R
import com.apm.ropapp.databinding.FragmentCalendarBinding
import com.bumptech.glide.Glide
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarFragment : Fragment() {
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var storage: StorageReference
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance(getString(R.string.database_url)).reference
        storage = FirebaseStorage.getInstance(getString(R.string.storage_url)).reference
        val userUid = firebaseAuth.currentUser?.uid


        val calendarView = binding.simpleCalendarView

        calendarView.setOnDateChangedListener{ view, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val formattedDate = dateFormat.format(selectedDate.time)
            Log.d("SEL DATE", formattedDate.toString())
        }

        /*fun getImageUri(fileName: String, photos: StorageReference): Uri {
            val dir = File("${binding.root.context.getExternalFilesDir(null)}/clothes")
            if (!dir.exists()) dir.mkdirs()
            val imageFile = File(dir, fileName)

            if (!imageFile.exists()) {
                imageFile.createNewFile()
                photos.child(fileName).getFile(imageFile)
            }
            return FileProvider.getUriForFile(
                binding.root.context, "com.apm.ropapp.FileProvider", imageFile
            )
        }

        fun getDatabaseValues(folderName: String) : HashMap<String, Any> {

            val toRecommend = HashMap<String, Any> ()
            database.child("$folderName/$userUid")
                .addValueEventListener(object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        val data = snapshot.getValue<HashMap<String, HashMap<String, Any>>>()

                        if (data != null) {
                            val dataList = mutableListOf<HashMap<String, Any>>()
                            val imageList = mutableListOf<Uri>()
                            val photos = storage.child(folderName)

                            data.forEach { (key, value) ->
                                value["id"] = key
                                Log.d("Key", value.toString())
                                dataList.add(value)
                                if (value["photo"] == null) imageList.add(Uri.EMPTY)
                                else imageList.add(getImageUri(value["photo"].toString(), photos))
                            }

                            /*fun updatePrendaRec(str: String, uri: Uri, img: ImageView) {
                                sharedPreferences.edit().putString(str, uri.toString()).apply()
                                Glide.with(requireContext()).load(uri).into(img)
                            }*/

                            //TODO: I put the first 4 clothes as recommendations, do the random process with the categories etc.

                            /*updatePrendaRec("prenda1", imageList[0], binding.prenda1)
                            updatePrendaRec("prenda2", imageList[1], binding.prenda2)
                            updatePrendaRec("prenda3", imageList[2], binding.prenda3)
                            updatePrendaRec("prenda4", imageList[3], binding.prenda4)*/
                            Log.d("CALENDAR", imageList[0].toString())
                            Glide.with(requireContext()).load(imageList[0]).into(binding.imageView1)
                            Glide.with(requireContext()).load(imageList[1]).into(binding.imageView2)
                            Glide.with(requireContext()).load(imageList[2]).into(binding.imageView3)
                            Glide.with(requireContext()).load(imageList[3]).into(binding.imageView4)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.w("TAG", "Failed to read value.", error.toException())
                    }
                })

            return toRecommend
        }

        getDatabaseValues("recommendations")*/


        return binding.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}