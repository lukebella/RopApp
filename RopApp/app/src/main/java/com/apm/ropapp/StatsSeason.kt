package com.apm.ropapp

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.apm.ropapp.databinding.StatsSeasonBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate

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
//        setContentView(binding.root)

        // Initializing variable for bar chart.
        barChart = binding.seasonChart// Fixed ID reference

        // Calling method to get bar entries.
        getBarEntries()

        // Creating a new bar data set.
        barDataSet = BarDataSet(barEntriesArrayList, "Geeks for Geeks")

        // Creating a new bar data and
        // passing our bar data set.
        barData = BarData(barDataSet)

        // Below line is to set data
        // to our bar chart.
        barChart.data = barData

        // Adding color to our bar data set.
        barDataSet.colors = ColorTemplate.COLORFUL_COLORS.asList()

        // Setting text color.
        barDataSet.valueTextColor = Color.BLACK

        // Setting text size
        barDataSet.valueTextSize = 16f
        barChart.description.isEnabled = true

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun getBarEntries() {
        // Creating a new array list
        barEntriesArrayList = ArrayList()

        // Adding new entry to our array list with bar
        // entry and passing x and y axis value to it.
        barEntriesArrayList.add(BarEntry(1f, 4f))
        barEntriesArrayList.add(BarEntry(2f, 6f))
        barEntriesArrayList.add(BarEntry(3f, 8f))
        barEntriesArrayList.add(BarEntry(4f, 2f))
        barEntriesArrayList.add(BarEntry(5f, 4f))
        barEntriesArrayList.add(BarEntry(6f, 1f))
    }
}