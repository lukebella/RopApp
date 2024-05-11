package com.apm.ropapp

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.apm.ropapp.databinding.StatsTypeBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

data class ClothesTypeData(
    val type: String,
    val count: Float
)

class StatsType : AppCompatActivity() {

    private lateinit var binding: StatsTypeBinding
    private lateinit var barChart: BarChart
    private lateinit var barData: BarData
    private lateinit var barDataSet: BarDataSet
    private lateinit var barEntriesArrayList: ArrayList<BarEntry>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = StatsTypeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        barChart = binding.typesChart

        get_database_data()

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun getBarEntries(clothesTypeDataList: List<ClothesTypeData>) {
        barEntriesArrayList = ArrayList()

        for ((index, clothesTypeData) in clothesTypeDataList.withIndex()) {
            barEntriesArrayList.add(BarEntry(index.toFloat(), clothesTypeData.count))
        }

        updateChart()
    }

    private fun updateChart() {
        barDataSet = BarDataSet(barEntriesArrayList, "Clothes Type Data")
        barData = BarData(barDataSet)
        barChart.data = barData
        barDataSet.colors = ColorTemplate.COLORFUL_COLORS.asList()
        barDataSet.valueTextColor = Color.BLACK
        barDataSet.valueTextSize = 16f
        barChart.description.isEnabled = true
        barChart.invalidate()
    }

    private fun get_database_data() {
        val databaseReference = FirebaseDatabase.getInstance().reference.child("path_to_your_data")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val clothesTypeDataList = mutableListOf<ClothesTypeData>()
                for (typeSnapshot in dataSnapshot.children) {
                    val clothesTypeData = typeSnapshot.getValue(ClothesTypeData::class.java)
                    if (clothesTypeData != null) {
                        clothesTypeDataList.add(clothesTypeData)
                    }
                }
                getBarEntries(clothesTypeDataList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("ClothesTypeChart", "Failed to get clothes type data.", databaseError.toException())
            }
        })
    }
}