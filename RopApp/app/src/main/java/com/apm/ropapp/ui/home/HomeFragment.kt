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
import android.os.AsyncTask
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
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.roundToInt

class HomeFragment : Fragment() {

  private var _binding: FragmentHomeBinding? = null

  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!
  private lateinit var fusedLocationClient: FusedLocationProviderClient

  val weatherKey = "704f6e60299aa4605accec614d63a67b"

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    Log.d("HOME", "init")

    _binding = FragmentHomeBinding.inflate(inflater, container, false)
    val root: View = binding.root
    val locationTextView = binding.aCoruna

    //get city
    retrieveCity(locationTextView) { city ->
      Log.d("CITY", city)
      //get weather
      weatherForecast(city, weatherKey)
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

  private fun weatherForecast(city: String, key: String) {
      val url = "https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$key&units=metric"
      FetchWeatherTask().execute(url)
  }


  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  companion object {
    private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
  }

  private fun retrieveCity(locationTextView: TextView, callback: (String) -> Unit) {
    fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    var city: String = ""

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
        Log.d("HOME", "location!=null")
        val geocoder = Geocoder(requireContext(), Locale.getDefault())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          geocoder.getFromLocation(
            location.latitude,
            location.longitude,
            1,
            Geocoder.GeocodeListener { addresses ->
              if (addresses.isNotEmpty()) {
                Log.d("HOME", "address")
                city = addresses[0].locality
                locationTextView.text = city
                callback(city)
              } else {
                Log.d("HOME", "no address")
                locationTextView.text = "City not found"
              }

            })
        } else {
          val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
          if (addresses != null && addresses.isNotEmpty()) {
            Log.d("HOME", "address")
            city = addresses[0].locality
            locationTextView.text = city
            callback(city)
          } else {
            Log.d("HOME", "no address")
            locationTextView.text = "City not found"
          }

        }
      } else Log.d("HOME", "location=null")
    }
  }

  inner class FetchWeatherTask : AsyncTask<String, Void, String>() {

    override fun doInBackground(vararg params: String?): String {
      var result = ""
      var urlConnection: HttpURLConnection? = null
      try {
        val url = URL(params[0])
        urlConnection = url.openConnection() as HttpURLConnection
        val inputStream: InputStream = urlConnection.inputStream
        val reader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
          stringBuilder.append(line).append("\n")
        }
        result = stringBuilder.toString()
      } catch (e: Exception) {
        Log.e("FetchWeatherTask", "Error", e)
      } finally {
        urlConnection?.disconnect()
      }
      return result
    }

    override fun onPostExecute(result: String?) {
      super.onPostExecute(result)
      if (!result.isNullOrEmpty()) {
        val weatherData = parseWeatherData(result)
        val temperature: String = weatherData.tempMax.toString()+"/"+weatherData.tempMin.toString()+"°C"
        binding.temperature.text =temperature
        // Do something with the weather data
        Log.d("WeatherForecast", weatherData.toString())
      } else {
        Log.e("FetchWeatherTask", "Empty result")
      }
    }

    private fun parseWeatherData(jsonString: String): WeatherData {
      val jsonObject = JSONObject(jsonString)
      val weat = jsonObject.getJSONArray("weather").getJSONObject(0).getString("main")
      val tempMin = jsonObject.getJSONObject("main").getDouble("temp_min").roundToInt()
      val tempMax = jsonObject.getJSONObject("main").getDouble("temp_max").roundToInt()
      return WeatherData(weat, tempMin, tempMax)
    }
  }

  data class WeatherData(val weat: String, val tempMin: Int, val tempMax: Int)

}

