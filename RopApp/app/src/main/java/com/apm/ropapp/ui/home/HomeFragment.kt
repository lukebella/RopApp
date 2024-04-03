package com.apm.ropapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.apm.ropapp.R
import com.apm.ropapp.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

private var _binding: FragmentHomeBinding? = null
  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

    _binding = FragmentHomeBinding.inflate(inflater, container, false)
    val root: View = binding.root

      /*val textView: TextView = binding.textHome
    homeViewModel.text.observe(viewLifecycleOwner) {
      textView.text = it
    }*/

    val noMeGusta: Button =  binding.noMeGusta
    noMeGusta.setOnClickListener{
        //findNavController().navigate(R.id.navigation_profile)
        // crea una doble barra de navegación
    }
      val meGusta: Button =  binding.meGusta
      meGusta.setOnClickListener{
          findNavController().navigate(R.id.navigation_calendar)
      }
    return root
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

}