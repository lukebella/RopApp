package com.apm.ropapp.ui.calendar

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.apm.ropapp.R
import com.apm.ropapp.databinding.FragmentCalendarBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarFragment : Fragment() {
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance(getString(R.string.database_url)).reference

        // Get today's date
        val today = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(today.time)

        // Fetch and display the images for today's date
        fetchAndDisplayImages(formattedDate)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance(getString(R.string.database_url)).reference

        val calendarView = binding.simpleCalendarView

        calendarView.setOnDateChangedListener { view, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val formattedDate = dateFormat.format(selectedDate.time)
            Log.d("SEL DATE", formattedDate)

            // Fetch and display the images for the selected date
            fetchAndDisplayImages(formattedDate)
        }

        return binding.root
    }

    private fun fetchAndDisplayImages(selectedDate: String) {
        val userUid = firebaseAuth.currentUser?.uid ?: return
        val recommendationRef = database.child("recommendations").child(userUid).child(selectedDate)
        recommendationRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val images = mutableListOf<String>()
                if (snapshot.exists()) {
                    for (imageSnapshot in snapshot.children) {
                        val imageUrl = imageSnapshot.getValue(String::class.java)
                        imageUrl?.let { images.add(it) }
                    }
                    binding.prendaMasUtilizada.text = "Conjunto del día:"
                    displayImages(images)
                } else {
                    Log.d("Firebase", "No data found for the selected date")
                    binding.prendaMasUtilizada.text = "No recomendaciónes"
                    displayImages(images)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error fetching data: ${error.message}")
            }
        })
    }


    private fun displayImages(images: List<String>) {
        val imageViews = listOf(
            binding.imageView1,
            binding.imageView2,
            binding.imageView3,
            binding.imageView4
        )
        Log.d("images", images.toString())
        for (i in imageViews.indices) {
            if (i < images.size) {
                Glide.with(this).load(images[i]).into(imageViews[i])
            } else {
                imageViews[i].setImageDrawable(null)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
