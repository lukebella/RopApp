package com.apm.ropapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Geocoder.GeocodeListener
import android.location.Location
import android.os.Build
import android.util.Log
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.apm.ropapp.R
import com.apm.ropapp.databinding.FragmentHomeBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.Locale
//import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
class HomeFragment : Fragment() {

  private var _binding: FragmentHomeBinding? = null

  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!
  private lateinit var fusedLocationClient: FusedLocationProviderClient

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    Log.d("HOME", "init")

    _binding = FragmentHomeBinding.inflate(inflater, container, false)
    val root: View = binding.root
    val locationTextView = binding.aCoruna

    fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

    Log.d("Home", "after fusedLocationClient instance")
    if (ActivityCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.ACCESS_COARSE_LOCATION
      ) != PackageManager.PERMISSION_GRANTED
    ) {
      // Request coarse location permission if not granted
      Log.d("Home", "No permission")
      ActivityCompat.requestPermissions(
        requireActivity(),
        arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
        LOCATION_PERMISSION_REQUEST_CODE
      )
    }
    Log.d("Home", "Permission")
    // Start receiving location updates
    fusedLocationClient.lastLocation.addOnSuccessListener { location ->

      Log.d("HOME", "check location")
      // Got last known location. In some rare situations this can be null.
      if (location != null) {
        // Update UI with location information
        //fetchAddressFromFirebase(location.latitude, location.longitude, locationTextView)
        Log.d("HOME", "location!=null")
        val geocoder = Geocoder(requireContext(), Locale.getDefault())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          geocoder.getFromLocation(location.latitude, location.longitude, 1, Geocoder.GeocodeListener {
              addresses ->
            if (addresses != null && addresses.isNotEmpty()) {
              Log.d("HOME", "address")
              val cityName = addresses[0].locality
              locationTextView.text = cityName
            } else {
              Log.d("HOME", "no address")
              locationTextView.text = "City not found"
            }

          })
        }
        else {
          val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
          if (addresses != null && addresses.isNotEmpty()) {
            Log.d("HOME", "address")
            val cityName = addresses[0].locality
            locationTextView.text = cityName
          } else {
            Log.d("HOME", "no address")
            locationTextView.text = "City not found"
          }

        }
      }
      else Log.d("HOME", "location=null")
    }

    val noMeGusta: Button = binding.noMeGusta
    noMeGusta.setOnClickListener {
      //findNavController().navigate(R.id.navigation_profile)
      // crea una doble barra de navegación
      //parentFragmentManager.popBackStack()
    }
    val meGusta: Button = binding.meGusta
    meGusta.setOnClickListener {
      findNavController().navigate(R.id.navigation_calendar)
    }
    return root
  }


  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  companion object {
    private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
  }

  private fun fetchAddressFromFirebase(latitude: Double, longitude: Double, locationTextView: TextView ) {
    val firestore = FirebaseFirestore.getInstance()
    val locationRef = firestore.collection("locations")

    locationRef.document("${latitude},${longitude}")
      .get()
      .addOnSuccessListener { document ->
        if (document != null && document.exists()) {
          val address = document.getString("address")
          Log.d("FirebaseGeocoding", "Address: $address")
          locationTextView.text = address
        } else {
          Log.d("FirebaseGeocoding", "No such document")
          locationTextView.text = "Location not found"
        }
      }
      .addOnFailureListener { exception ->
        Log.e("FirebaseGeocoding", "Error getting document: ", exception)
        locationTextView.text = "Error getting location"
      }
  }
}


