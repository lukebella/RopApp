package com.apm.ropapp.ui.home

//import com.google.android.gms.maps.model.LatLng
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.apm.ropapp.MainActivity
import com.apm.ropapp.R
import com.apm.ropapp.databinding.FragmentHomeBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

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
    val weatherDate = binding.weatherDate

    //get city
    retrieveCity(locationTextView, weatherDate) { lat, long ->
      //get weather
      weatherForecast(lat, long, R.string.weather_key.toString())
    }


    val noMeGusta: Button = binding.noMeGusta
    noMeGusta.setOnClickListener {
      //findNavController().navigate(R.id.navigation_profile)
      // crea una doble barra de navegación
      //parentFragmentManager.popBackStack()
    }
    val meGusta: Button = binding.meGusta
    meGusta.setOnClickListener {
      Log.d("HomeFragment", "Continue to Fragment Calendar")
      val intent = Intent(it.context, MainActivity::class.java)
      intent.putExtra("fragment", R.id.navigation_calendar)
      startActivity(intent)
    }
    return root
  }

  private fun weatherForecast(lat: Double, longit: Double, key: String) {
      val url ="https://api.openweathermap.org/data/2.5/forecast?lat=$lat&lon=$longit&appid=$key"
      FetchWeatherTask().execute(url)
  }


  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  companion object {
    private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
  }

  private fun retrieveCity(locationTextView: TextView, dateTextView: TextView, callback: (Double, Double) -> Unit) {
    fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    var city: String

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
            1
          ) { addresses ->
            if (addresses.isNotEmpty()) {
              Log.d("HOME", "address")
              city = addresses[0].locality
              locationTextView.text = city
              callback(location.latitude, location.longitude)
            } else {
              Log.d("HOME", "no address")
              locationTextView.text = "City not found"
            }

          }
        } else {
          val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
          if (!addresses.isNullOrEmpty()) {
            Log.d("HOME", "address")
            city = addresses[0].locality
            locationTextView.text = city
            callback(location.latitude, location.longitude)
          } else {
            Log.d("HOME", "no address")
            locationTextView.text = "City not found"
          }

        }
      } else Log.d("HOME", "location=null")
    }
  }

  inner class FetchWeatherTask : AsyncTask<String, Void, String>() {

    @Deprecated("Deprecated in Java")
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

    @RequiresApi(Build.VERSION_CODES.O)
    @Deprecated("Deprecated in Java")
    override fun onPostExecute(result: String?) {
      super.onPostExecute(result)
      if (!result.isNullOrEmpty()) {
        val weatherData = parseWeatherData(result)
        val temperature: String =
          weatherData.tempMax.toString() + "/" + weatherData.tempMin.toString() + "°C"
        binding.temperature.text = temperature
        binding.weatherDate.text = weatherData.data
        // Do something with the weather data
        Log.d("WeatherForecast", weatherData.toString())
      } else {
        Log.e("FetchWeatherTask", "Empty result")
      }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun parseWeatherData(jsonString: String): WeatherData {
      val calendar = Calendar.getInstance()
      val year = calendar.get(Calendar.YEAR)
      val month = calendar.get(Calendar.MONTH) + 1 // Month is zero-based
      val day = calendar.get(Calendar.DAY_OF_MONTH)

      var tempMin = 0
      var tempMax = 0
      var weat = ""

      var dateToday = "$year-0$month-$day"
      val jsonObject = JSONObject(jsonString)
      val listArray = jsonObject.getJSONArray("list")
      for (i in 0 until listArray.length()) {
        val forecast = listArray.getJSONObject(i)

        if (forecast.getString("dt_txt").startsWith(dateToday)) {
          tempMin = forecast.getJSONObject("main").getDouble("temp_min").roundToInt() - 273
          tempMax = forecast.getJSONObject("main").getDouble("temp_max").roundToInt() - 273
          weat = forecast.getJSONArray("weather").getJSONObject(0).getString("icon")
        }
      }
      val imgurl = "https://openweathermap.org/img/wn/$weat@2x.png"
      Log.d("URL", imgurl)
      downloadImage(imgurl, "RopApp/app/src/main/res/drawable/imageweather.png")
      val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
      val date = LocalDate.parse(dateToday, formatter)
      dateToday = date.dayOfWeek.toString().substring(0,3)+" "+date.dayOfMonth+" "+date.month.toString().substring(0,3)
      return WeatherData(weat, tempMin, tempMax, dateToday)

    }

    private fun downloadImage(imageUrl: String, destinationFile: String) {
      runBlocking {
        launch(Dispatchers.IO) {
          try {
            val url = URL(imageUrl)
            val connection = url.openConnection().getInputStream()
            val file = File(destinationFile)
            file.outputStream().use { output ->
              connection.copyTo(output)
            }
            println("Image downloaded successfully to $destinationFile")
          } catch (e: Exception) {
            println("Error downloading image: ${e.message}")
          }
        }
      }
    }
  }

  data class WeatherData(val weat: String, val tempMin: Int, val tempMax: Int, val data: String)

}

