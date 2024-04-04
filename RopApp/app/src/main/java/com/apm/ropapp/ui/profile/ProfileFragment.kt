package com.apm.ropapp.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.apm.ropapp.EditProfile
import com.apm.ropapp.databinding.FragmentProfileBinding

class ProfileFragment : Fragment(){

    private var _binding: FragmentProfileBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.configButton.setOnClickListener {
            val intent = Intent(requireContext(), EditProfile::class.java)
            Log.d("Profile", "Editing profile")
            startActivity(intent)
        }
        binding.infoButton.setOnClickListener {
            Log.d("Profile", "Info button clicked")
        }

        binding.statButton.setOnClickListener {
            Log.d("Profile", "Stats button clicked")
        }

        return root

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}