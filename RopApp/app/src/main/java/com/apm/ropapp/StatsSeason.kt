package com.apm.ropapp

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.apm.ropapp.databinding.StatsSeasonBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class StatsSeason : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var binding: StatsSeasonBinding
    private lateinit var barChart: BarChart
    private lateinit var barData: BarData
    private lateinit var barDataSet: BarDataSet
    private lateinit var barEntriesArrayList: ArrayList<BarEntry>
    private lateinit var seasonLabels: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = StatsSeasonBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initializing variable for bar chart.
        barChart = binding.seasonChart

        // Call the function to get data from the database
        getDatabaseData()

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun getBarEntries(seasonDataMap: Map<String, Int>) {
        barEntriesArrayList = ArrayList()
        seasonLabels = ArrayList()

        var index = 0f
        for ((season, count) in seasonDataMap) {
            barEntriesArrayList.add(BarEntry(index, count.toFloat()))
            seasonLabels.add(season)
            index += 1f
        }

        // After getting the entries, update the chart
        updateChart()
    }

    private fun updateChart() {
        barDataSet = BarDataSet(barEntriesArrayList, "Clothes per Season")
        barData = BarData(barDataSet)
        barChart.data = barData
        barDataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()
        barDataSet.valueTextColor = Color.BLACK
        barDataSet.valueTextSize = 16f
        barChart.description.isEnabled = false

        // Set the labels on the X-axis
        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(seasonLabels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.labelCount = seasonLabels.size

        barChart.invalidate() // refresh the chart
    }

    private fun getDatabaseData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            // Obtain a reference to the user's clothes data
            databaseReference = FirebaseDatabase.getInstance().reference
                .child("clothes").child(userId)

            databaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val seasonDataMap = mutableMapOf<String, Int>()

                    for (clothSnapshot in dataSnapshot.children) {
                        val seasonsSnapshot = clothSnapshot.child("seasons")
                        for (seasonSnapshot in seasonsSnapshot.children) {
                            val season = seasonSnapshot.getValue(String::class.java)
                            if (season != null) {
                                seasonDataMap[season] = seasonDataMap.getOrDefault(season, 0) + 1
                            }
                        }
                    }

                    getBarEntries(seasonDataMap)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d("StatsSeason", "Failed to get season data.", databaseError.toException())
                }
            })
        } else {
            Log.d("StatsSeason", "User ID is null.")
        }
    }
}
