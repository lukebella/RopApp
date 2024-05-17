package com.apm.ropapp.ui.home

import android.Manifest
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.apm.ropapp.R
import com.apm.ropapp.databinding.FragmentHomeBinding
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.roundToInt

class prova : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var pressedNo = false
    private var pressedSi = false

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var storage: StorageReference

    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale("es", "ES"))
    private val recommendationId = dateFormat.format(calendar.time)
    private var toRecommend = HashMap<String, Any>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("HOME", "init")
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        initializeFirebase()
        loadDataFromPreferences()
        resetDataIfNeeded()

        getDatabaseValues("clothes") { data ->
            toRecommend = checkURI(data)
        }

        updateWeatherIfNeeded(binding.aCoruna)
        setupButtonListeners()

        return root
    }

    private fun initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance(getString(R.string.database_url)).reference
        storage = FirebaseStorage.getInstance(getString(R.string.storage_url)).reference
    }

    private fun loadDataFromPreferences() {
        val sharedPreferences = requireContext().getSharedPreferences("MySharedPreferences", MODE_PRIVATE)
        binding.aCoruna.text = sharedPreferences.getString("LOC", binding.aCoruna.text.toString())
        binding.weatherDate.text = sharedPreferences.getString("DATE", binding.weatherDate.text.toString())
        binding.temperature.text = sharedPreferences.getString("TEMP", binding.temperature.text.toString())
        binding.weatImg.setImageResource(sharedPreferences.getInt("IMG", R.drawable.w01d_2x))
        updateButtonColors(sharedPreferences)
        updateImageViews(sharedPreferences)
    }

    private fun updateButtonColors(sharedPreferences: SharedPreferences) {
        binding.noMeGusta.setBackgroundColor(
            ContextCompat.getColor(requireContext(), sharedPreferences.getInt("nomegusta", R.color.md_theme_background))
        )
        binding.meGusta.setBackgroundColor(
            ContextCompat.getColor(requireContext(), sharedPreferences.getInt("megusta", R.color.md_theme_background))
        )
    }

    private fun updateImageViews(sharedPreferences: SharedPreferences) {
        convertToURI(binding.prenda1, "prenda1", sharedPreferences)
        convertToURI(binding.prenda2, "prenda2", sharedPreferences)
        convertToURI(binding.prenda3, "prenda3", sharedPreferences)
        convertToURI(binding.prenda4, "prenda4", sharedPreferences)
    }

    private fun resetDataIfNeeded() {
        val sharedPreferences = requireContext().getSharedPreferences("MySharedPreferences", MODE_PRIVATE)
        val memorizedDate = sharedPreferences.getString("FORMATTED_DATE", "01-01-24").toString()
        if (memorizedDate != recommendationId) {
            sharedPreferences.edit().putString("FORMATTED_DATE", recommendationId).apply()
            sharedPreferences.edit().putInt("nomegusta", R.color.md_theme_background).apply()
            sharedPreferences.edit().putInt("megusta", R.color.md_theme_background).apply()
        }
    }

    private fun updateWeatherIfNeeded(locationTextView: TextView) {
        val sharedPreferences = requireContext().getSharedPreferences("MySharedPreferences", MODE_PRIVATE)
        val lastUpdate = sharedPreferences.getLong("LONG_KEY", 0)
        val updateInterval = 2 * 60 * 60 * 1000

        if (System.currentTimeMillis() >= lastUpdate + updateInterval) {
            retrieveCity(locationTextView) { lat, long ->
                fetchWeatherForecast(lat, long)
            }
        }
    }

    private fun setupButtonListeners() {
        val noMeGustaButton = binding.noMeGusta
        val meGustaButton = binding.meGusta

        noMeGustaButton.setOnClickListener {
            manageNoMeGusta()
        }
        meGustaButton.setOnClickListener {
            manageMeGusta()
        }
    }

    private fun fetchWeatherForecast(lat: Double, lon: Double) {
        val url = "https://api.openweathermap.org/data/2.5/forecast?lat=$lat&lon=$lon&appid=${getString(R.string.weather_key)}"
        FetchWeatherTask().execute(url)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun retrieveCity(locationTextView: TextView, callback: (Double, Double) -> Unit) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(it.latitude, it.longitude, 1)
                } else {
                    geocoder.getFromLocation(it.latitude, it.longitude, 1)
                }
                if (!addresses.isNullOrEmpty()) {
                    val city = addresses[0].locality ?: "City not found"
                    locationTextView.text = city
                    saveCityToPreferences(city)
                    callback(it.latitude, it.longitude)
                } else {
                    locationTextView.text = "City not found"
                }
            }
        }
    }

    private fun saveCityToPreferences(city: String) {
        val sharedPreferences = requireContext().getSharedPreferences("MySharedPreferences", MODE_PRIVATE)
        sharedPreferences.edit().putString("LOC", city).apply()
    }

    private fun convertToURI(img: ImageView, str: String, sharedPreferences: SharedPreferences) {
        val uriString = sharedPreferences.getString(str, null)
        val uri = if (uriString != null) Uri.parse(uriString) else Uri.EMPTY
        Glide.with(requireContext()).load(uri).into(img)
    }

    private fun checkURI(data: HashMap<String, Any>): HashMap<String, Any> {
        val recommendationData = data.mapValues { entry ->
            if (entry.value is List<*>) {
                (entry.value as List<*>).map {
                    if (it is Uri) it.toString() else it
                }
            } else {
                entry.value
            }
        }
        return HashMap(recommendationData)
    }

    private fun manageNoMeGusta() {
        val sharedPreferences = requireContext().getSharedPreferences("MySharedPreferences", MODE_PRIVATE)
        val noMeGustaButton = binding.noMeGusta
        val meGustaButton = binding.meGusta

        if (!pressedNo) {
            noMeGustaButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.md_theme_error))
            meGustaButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.md_theme_background))
            sharedPreferences.edit().putInt("nomegusta", R.color.md_theme_error).apply()
            sharedPreferences.edit().putInt("megusta", R.color.md_theme_background).apply()
            pressedNo = true
            pressedSi = false
            deleteRecommendation()
        } else {
            noMeGustaButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.md_theme_background))
            sharedPreferences.edit().putInt("nomegusta", R.color.md_theme_background).apply()
            pressedNo = false
        }
    }

    private fun manageMeGusta() {
        val sharedPreferences = requireContext().getSharedPreferences("MySharedPreferences", MODE_PRIVATE)
        val noMeGustaButton = binding.noMeGusta
        val meGustaButton = binding.meGusta

        if (!pressedSi) {
            meGustaButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.md_theme_confirm))
            noMeGustaButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.md_theme_background))
            sharedPreferences.edit().putInt("megusta", R.color.md_theme_confirm).apply()
            sharedPreferences.edit().putInt("nomegusta", R.color.md_theme_background).apply()
            pressedSi = true
            pressedNo = false
            saveRecommendation()
        } else {
            meGustaButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.md_theme_background))
            sharedPreferences.edit().putInt("megusta", R.color.md_theme_background).apply()
            pressedSi = false
        }
    }

    private fun saveRecommendation() {
        val currentUser = firebaseAuth.currentUser
        val recommendationPath = "recommendations/${currentUser?.uid}/$recommendationId"
        database.child(recommendationPath).setValue(toRecommend)
    }

    private fun deleteRecommendation() {
        val currentUser = firebaseAuth.currentUser
        val recommendationPath = "recommendations/${currentUser?.uid}/$recommendationId"
        database.child(recommendationPath).removeValue()
    }

    private fun getDatabaseValues(path: String, callback: (HashMap<String, Any>) -> Unit) {
        database.child(path).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.value as? HashMap<String, Any> ?: HashMap()
                callback(data)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.CUPCAKE)
    private inner class FetchWeatherTask : AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg urls: String?): String {
            val urlConnection = URL(urls[0]).openConnection() as HttpURLConnection
            return try {
                val inputStream: InputStream = urlConnection.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream))
                reader.use { it.readText() }
            } finally {
                urlConnection.disconnect()
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            result?.let { parseWeatherData(it) }
        }
    }

    private fun parseWeatherData(weatherData: String) {
        val jsonObject = JSONObject(weatherData)
        val listArray = jsonObject.getJSONArray("list")

        if (listArray.length() > 0) {
            val firstObject = listArray.getJSONObject(0)
            val mainObject = firstObject.getJSONObject("main")
            val weatherArray = firstObject.getJSONArray("weather")
            val weatherObject = weatherArray.getJSONObject(0)
            val temp = mainObject.getDouble("temp") - 273.15
            val tempRounded = temp.roundToInt()
            val icon = weatherObject.getString("icon")
            val updatedIcon = updateWeatherIcon(icon)

            updateWeatherUI(tempRounded.toString(), updatedIcon)
            saveWeatherToPreferences(tempRounded.toString(), updatedIcon)
        }
    }

    private fun updateWeatherIcon(icon: String): Int {
        val iconMap = mapOf(
            "01d" to R.drawable.w01d_2x,
            "01n" to R.drawable.w01n_2x,
            "02d" to R.drawable.w02d_2x,
            "02n" to R.drawable.w02n_2x,
            "03d" to R.drawable.w03d_2x,
            "03n" to R.drawable.w03n_2x,
            "04d" to R.drawable.w04d_2x,
            "04n" to R.drawable.w04n_2x,
            "09d" to R.drawable.w09d_2x,
            "09n" to R.drawable.w09n_2x,
            "10d" to R.drawable.w10d_2x,
            "10n" to R.drawable.w10n_2x,
            "11d" to R.drawable.w11d_2x,
            "11n" to R.drawable.w11n_2x,
            "13d" to R.drawable.w13d_2x,
            "13n" to R.drawable.w13n_2x
        )
        return iconMap[icon] ?: R.drawable.w01d_2x
    }

    private fun updateWeatherUI(temp: String, icon: Int) {
        binding.weatherDate.text = dateFormat.format(calendar.time)
        binding.temperature.text = temp
        binding.weatImg.setImageResource(icon)
    }

    private fun saveWeatherToPreferences(temp: String, icon: Int) {
        val sharedPreferences = requireContext().getSharedPreferences("MySharedPreferences", MODE_PRIVATE)
        sharedPreferences.edit().putLong("LONG_KEY", System.currentTimeMillis()).apply()
        sharedPreferences.edit().putString("DATE", binding.weatherDate.text.toString()).apply()
        sharedPreferences.edit().putString("TEMP", temp).apply()
        sharedPreferences.edit().putInt("IMG", icon).apply()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}
