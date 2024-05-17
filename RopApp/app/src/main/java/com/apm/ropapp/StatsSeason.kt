package com.apm.ropapp

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.apm.ropapp.databinding.StatsSeasonBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

data class SeasonData(
    val season: String,
    val clothesCount: Float,
    val outfitCount: Float
)

class StatsSeason : AppCompatActivity() {

    private lateinit var binding: StatsSeasonBinding
    // Variable for our bar chart
    private lateinit var barChart: BarChart

    // Variable for our bar data.
    private lateinit var barData: BarData

    // Variable for our bar data set.
    private lateinit var barDataSet: BarDataSet

    // ArrayList for storing entries.
    private lateinit var barEntriesArrayList: ArrayList<BarEntry>

    override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = StatsSeasonBinding.inflate(layoutInflater)
    setContentView(binding.root)

    // Initializing variable for bar chart.
    barChart = binding.seasonChart

    // Call the function to get data from the database
    get_database_data()

    binding.backButton.setOnClickListener {
        finish()
    }
}

    private fun getBarEntries(seasonDataList: List<SeasonData>) {
        barEntriesArrayList = ArrayList()

        for ((index, seasonData) in seasonDataList.withIndex()) {
            // Use the index as the x value and the clothesCount as the y value
            barEntriesArrayList.add(BarEntry(index.toFloat(), seasonData.clothesCount))
        }

        // After getting the entries, update the chart
        updateChart()
    }

    private fun updateChart() {
        barDataSet = BarDataSet(barEntriesArrayList, "Season Data")
        barData = BarData(barDataSet)
        barChart.data = barData
        barDataSet.colors = ColorTemplate.COLORFUL_COLORS.asList()
        barDataSet.valueTextColor = Color.BLACK
        barDataSet.valueTextSize = 16f
        barChart.description.isEnabled = true
        barChart.invalidate() // refresh the chart
    }

    private fun get_database_data() {
        val databaseReference = FirebaseDatabase.getInstance().reference.child("path_to_your_data")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val seasonDataList = mutableListOf<SeasonData>()
                for (seasonSnapshot in dataSnapshot.children) {
                    val seasonData = seasonSnapshot.getValue(SeasonData::class.java)
                    if (seasonData != null) {
                        seasonDataList.add(seasonData)
                    }
                }
                getBarEntries(seasonDataList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("StatsSeason", "Failed to get season data.", databaseError.toException())
            }
        })
    }
}