package com.apm.ropapp.ui.calendar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarFragment : Fragment() {
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private var lastFetchedDate: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance(getString(R.string.database_url)).reference

        val calendarView = binding.simpleCalendarView

        // Get today's date
        val selectedDate = Calendar.getInstance()
        var formattedDate = formatDate(selectedDate)

        // Fetch and display the images for today's date
        fetchAndDisplayImages(formattedDate)

        calendarView.setOnDateChangedListener { _, year, month, dayOfMonth ->
            selectedDate.set(year, month, dayOfMonth)
            formattedDate = formatDate(selectedDate)
            Log.d("SEL DATE", formattedDate)

            // Fetch and display the images for the selected date
            fetchAndDisplayImages(formattedDate)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        // Get today's date
        val today = Calendar.getInstance()
        val calendarView = binding.simpleCalendarView
        calendarView.updateDate(
            today.get(Calendar.YEAR), today.get(Calendar.MONTH),
            today.get(Calendar.DAY_OF_MONTH)
        )

        // Format today's date and fetch and display the images
        val formattedDate = formatDate(today)
        fetchAndDisplayImages(formattedDate)
    }
    private fun formatDate(date: Calendar): String {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        return dateFormat.format(date.time)
    }

    private fun fetchAndDisplayImages(selectedDate: String) {
        // If the images for the selected date have already been fetched, no need to fetch them again
        if (selectedDate == lastFetchedDate) return

        val userUid = firebaseAuth.currentUser?.uid ?: return
        val recommendationRef = database.child("recommendations").child(userUid).child(selectedDate)
        recommendationRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val images = mutableListOf<String>()
                if (snapshot.exists()) {
                    for (imageSnapshot in snapshot.children) {
                        Log.d("LLAVE", imageSnapshot.key.toString())
                        val imageUrl = imageSnapshot.getValue(String::class.java)
                        imageUrl?.let { images.add(it) }
                    }
                    binding.textViewCalendar.text = getString(R.string.valorConjuntoCalendario)
                    displayImages(images)
                } else {
                    Log.d("Firebase", "No data found for the selected date")
                    binding.textViewCalendar.text = getString(R.string.valorSinRecomendacion)
                    binding.imageView5.setImageDrawable(null)
                    binding.imageView6.setImageDrawable(null)
                    displayImages(images)
                }
                lastFetchedDate = selectedDate
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