package com.apm.ropapp.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.Fragment
import com.apm.ropapp.databinding.CalendarBinding

class CalendarFragment : Fragment() {
    private var _binding: CalendarBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CalendarBinding.inflate(inflater, container, false)
        val root: View = binding.root


        binding.backButtonChoice.setOnClickListener {
                parentFragmentManager.popBackStack()

            }

        return root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}