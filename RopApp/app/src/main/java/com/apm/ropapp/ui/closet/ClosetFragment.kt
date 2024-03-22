package com.apm.ropapp.ui.closet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.apm.ropapp.databinding.FragmentClosetBinding

class ClosetFragment : Fragment() {

    private var _binding: FragmentClosetBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var buttonSelected: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentClosetBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        val closetViewModel = ViewModelProvider(this)[ClosetViewModel::class.java]
//        val textView: TextView = binding.textView1
//        closetViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }

        buttonSelected = binding.button1
        buttonSelected.isSelected = true

        binding.button1.setOnClickListener {
            selectButton(binding.button1)
        }

        binding.button2.setOnClickListener {
            selectButton(binding.button2)
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
}