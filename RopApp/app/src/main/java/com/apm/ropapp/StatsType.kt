package com.apm.ropapp

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.apm.ropapp.databinding.StatsTypeBinding
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

class StatsType : AppCompatActivity() {

    private lateinit var binding: StatsTypeBinding
    private lateinit var barChart: BarChart
    private lateinit var barData: BarData
    private lateinit var barDataSet: BarDataSet
    private lateinit var barEntriesArrayList: ArrayList<BarEntry>
    private lateinit var typeLabels: ArrayList<String>
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = StatsTypeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        barChart = binding.typesChart

        getDatabaseData()

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun getBarEntries(clothesTypeDataMap: Map<String, Int>) {
        barEntriesArrayList = ArrayList()
        typeLabels = ArrayList()

        var index = 0f
        for ((type, count) in clothesTypeDataMap) {
            barEntriesArrayList.add(BarEntry(index, count.toFloat()))
            typeLabels.add(type)
            index += 1f
        }

        updateChart()
    }

    private fun updateChart() {
        barDataSet = BarDataSet(barEntriesArrayList, "Clothes Type Data")
        barData = BarData(barDataSet)
        barChart.data = barData
        barDataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()
        barDataSet.valueTextColor = Color.BLACK
        barDataSet.valueTextSize = 16f
        barChart.description.isEnabled = false

        // Set the labels on the X-axis
        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(typeLabels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.labelCount = typeLabels.size

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
                    val TypeDataMap = mutableMapOf<String, Int>()

                    for (clothSnapshot in dataSnapshot.children) {
                        val typesSnapshot = clothSnapshot.child("style")
                        for (typesSnapshot in typesSnapshot.children) {
                            val season = typesSnapshot.getValue(String::class.java)
                            if (season != null) {
                                TypeDataMap[season] = TypeDataMap.getOrDefault(season, 0) + 1
                            }
                        }
                    }

                    getBarEntries(TypeDataMap)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d("StatsType", "Failed to get clothes type data.", databaseError.toException())
                }
            })
        } else {
            Log.d("StatsType", "User ID is null.")
        }
    }
}
