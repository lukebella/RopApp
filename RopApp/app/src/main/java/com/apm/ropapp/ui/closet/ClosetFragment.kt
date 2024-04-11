package com.apm.ropapp.ui.closet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apm.ropapp.R
import com.apm.ropapp.databinding.FragmentClosetBinding


class ClosetFragment : Fragment() {

    private var _binding: FragmentClosetBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var buttonSelected: Button
    private val imageList = mutableListOf(R.drawable.tshirt, R.drawable.nike, R.drawable.recomendprenda, R.drawable.tshirt, R.drawable.nike, R.drawable.recomendprenda)
    private val stringList = mutableListOf("Armario Principal", "Armario Verano", "Armario Formal", "Armario Fiesta", "Chaquetas", "Pantalones")
    private val imageList2 = mutableListOf(R.drawable.nike, R.drawable.recomendprenda, R.drawable.tshirt, R.drawable.nike, R.drawable.recomendprenda, R.drawable.tshirt)
    private val stringList2 = mutableListOf("Conjunto 1", "Conjunto 2", "Conjunto 3", "Conjunto 4", "Conjunto 5", "Conjunto 6")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentClosetBinding.inflate(inflater, container, false)
        val root: View = binding.root

        buttonSelected = binding.button1
        buttonSelected.isSelected = true

        val recyclerView: RecyclerView = binding.root.findViewById(R.id.recycler_view)
        recyclerView.adapter = CustomAdapter(stringList, imageList)
        recyclerView.autoFitColumns(150)

        binding.button1.setOnClickListener {
            selectButton(binding.button1)
            recyclerView.adapter = CustomAdapter(stringList, imageList)
        }
        binding.button2.setOnClickListener {
            selectButton(binding.button2)
            recyclerView.adapter = CustomAdapter(stringList2, imageList2)
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
        val noOfColumns = ((displayMetrics.widthPixels / displayMetrics.density) / columnWidth).toInt()
        this.layoutManager = GridLayoutManager(this.context, noOfColumns)
    }
}