package com.apm.ropapp.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
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
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.apm.ropapp.R
import com.apm.ropapp.databinding.FragmentHomeBinding
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
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
    private var pressedNo: Boolean = false
    private var pressedSi: Boolean = false

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var storage: StorageReference

    private val calendar: Calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH) + 1 // Month is zero-based
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale("es", "ES"))
    val recommendationId: String = dateFormat.format(calendar.time)
    private var toRecommend = HashMap<String, Any> ()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("HOME", "init")
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val noMeGustaButton = binding.noMeGusta
        val meGustaButton = binding.meGusta
        val sharedPreferences =
            requireContext().getSharedPreferences("MySharedPreferences", MODE_PRIVATE)

        initializeFirebase()
        val userUid = firebaseAuth.currentUser?.uid
        loadData(sharedPreferences)
        resetData(sharedPreferences)

        updateWeather(sharedPreferences, binding.aCoruna)
        //metodo para recomendar

        if (sharedPreferences.getBoolean("new_rec", true)) {
            getDatabaseValues("clothes", userUid!!, sharedPreferences) { data ->
                toRecommend = checkURI(data)
            }
            sharedPreferences.edit().putBoolean("new_rec", false).apply()
        }

        noMeGustaButton.setOnClickListener {
            manageNoMeGusta(noMeGustaButton, meGustaButton, sharedPreferences)
        }
        meGustaButton.setOnClickListener {
            if (firebaseAuth.currentUser != null) {
                manageMeGusta(noMeGustaButton, meGustaButton, sharedPreferences)
                Log.d("DatabaseUpdate", "toRecommend: $toRecommend")
                uploadNewRecommendation(toRecommend[recommendationId]!!)
            }
        }

        return root
    }

    private fun initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance(getString(R.string.database_url)).reference
        storage = FirebaseStorage.getInstance(getString(R.string.storage_url)).reference
    }

    //LOCALIZATION AND WEATHER FUNCTIONS

    private fun updateWeather(sharedPreferences: SharedPreferences, locationTextView: TextView) {
        val tmsUpdate = sharedPreferences.getLong("LONG_KEY", 0)
        val delta = 2 * 60 * 60 * 1000
        Log.d("TMSU", tmsUpdate.toString())
        Log.d("TMS", System.currentTimeMillis().toString())
        Log.d("TMSD", (tmsUpdate + delta).toString())

        if (System.currentTimeMillis() >= tmsUpdate + delta) {
            Log.d("m", "Update")
            //get city
            retrieveCity(locationTextView, sharedPreferences) { lat, long ->
                //get weather
                weatherForecast(lat, long, getString(R.string.weather_key))

            }
        }
    }

    private fun weatherForecast(lat: Double, longit: Double, key: String) {
        val url = "https://api.openweathermap.org/data/2.5/forecast?lat=$lat&lon=$longit&appid=$key"
        FetchWeatherTask().execute(url)
    }

    private fun retrieveCity(
        locationTextView: TextView,
        sharedPreferences: SharedPreferences,
        callback: (Double, Double) -> Unit
    ) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        var city: String

        Log.d("Home", "after fusedLocationClient instance")
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            //Request coarse location permission if not granted
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
                            sharedPreferences.edit().putString("LOC", city).apply()
                            locationTextView.text = city
                            callback(location.latitude, location.longitude)
                        } else {
                            Log.d("HOME", "no address")
                            locationTextView.text = "City not found"
                        }

                    }
                } else {
                    val addresses =
                        geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        Log.d("HOME", "address")
                        city = addresses[0].locality
                        sharedPreferences.edit().putString("LOC", city).apply()
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
        private val sharedPreferences: SharedPreferences =
            requireContext().getSharedPreferences("MySharedPreferences", MODE_PRIVATE)

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

                sharedPreferences.edit().putString("DATE", weatherData.data).apply()
                sharedPreferences.edit().putString("TEMP", temperature).apply()
                sharedPreferences.edit().putLong("LONG_KEY", System.currentTimeMillis()).apply()

                Log.d("WeatherForecast", weatherData.toString())
            } else {
                Log.e("FetchWeatherTask", "Empty result")
            }
        }

        @SuppressLint("DiscouragedApi")
        @RequiresApi(Build.VERSION_CODES.O)
        private fun parseWeatherData(jsonString: String): WeatherData {

            val dayString = if (day < 10) {
                "0$day" // Adding leading zero if dayOfMonth is less than 10
            } else {
                day.toString()
            }

            var tempMin = 0
            var tempMax = 0
            var weat = ""

            val dateToday = "$year-0$month-$dayString"
            val jsonObject = JSONObject(jsonString)
            val listArray = jsonObject.getJSONArray("list")
            for (i in 0 until listArray.length()) {
                val forecast = listArray.getJSONObject(i)

                if (forecast.getString("dt_txt").startsWith(dateToday)) {
                    tempMin =
                        forecast.getJSONObject("main").getDouble("temp_min").roundToInt() - 273
                    tempMax =
                        forecast.getJSONObject("main").getDouble("temp_max").roundToInt() - 273
                    weat = forecast.getJSONArray("weather").getJSONObject(0).getString("icon")
                }
            }
            val resourceName = "w" + weat + "_2x" // Replace with your resource name
            val resourceId =
                resources.getIdentifier(resourceName, "drawable", requireContext().packageName)
            sharedPreferences.edit().putInt("IMG", resourceId).apply()
            //binding.weatImg.setImageResource(resourceId)
            Log.d("HOME", "Changed weather image")
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale("es", "ES"))
            val date = LocalDate.parse(dateToday, formatter)
            val outputFormatter = DateTimeFormatter.ofPattern("EEE dd MMM", Locale("es", "ES"))

            // Format the date to Spanish
            val formattedDate = date.format(outputFormatter).uppercase(Locale("es", "ES"))
            /*dateToday = date.dayOfWeek.toString().substring(0, 3) + " " +
                    date.dayOfMonth + " " + date.month.toString().substring(0, 3)*/
            return WeatherData(weat, tempMin, tempMax, formattedDate)

        }

    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
    data class WeatherData(val weat: String, val tempMin: Int, val tempMax: Int, val data: String)


    //------------------------------------------------------------------------------------
    //CONVERSION IMAGES --> URI

    private fun checkURI(data: HashMap<String, Any>) : HashMap<String, Any> {
        // Convert Uri to String if present
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

    fun getImageUri(fileName: String, photos: StorageReference): Uri {
        val dir = File("${requireContext().getExternalFilesDir(null)}/clothes")
        if (!dir.exists()) dir.mkdirs()
        val imageFile = File(dir, fileName)

        if (imageFile.isDirectory) {
            return getUriFromDrawable(requireContext(), R.drawable.unavailable, "default_image.png")
        }


        if (!imageFile.exists()) {
            imageFile.createNewFile()
            photos.child(fileName).getFile(imageFile)
        }


        return FileProvider.getUriForFile(
            requireContext(), "com.apm.ropapp.FileProvider", imageFile
        )


    }
    private fun getBitmapFromDrawable(context: Context, drawableId: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(context, drawableId)
        val bitmap = Bitmap.createBitmap(drawable!!.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    // Function to save a Bitmap to a file
    private fun saveBitmapToFile(context: Context, bitmap: Bitmap, fileName: String): File {
        val file = File(context.getExternalFilesDir(null), fileName)
        val out = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        out.flush()
        out.close()
        return file
    }

    private fun getUriFromDrawable(context: Context, drawableId: Int, fileName: String): Uri {
        val bitmap = getBitmapFromDrawable(context, drawableId)
        val file = saveBitmapToFile(context, bitmap, fileName)
        return getUriFromFile(context, file)
    }

    private fun getUriFromFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(context, "com.apm.ropapp.FileProvider", file)
    }

    private fun convertToURI(img: ImageView, str: String, sharedPreferences: SharedPreferences) {
        val uriString = sharedPreferences.getString(str, null)
        val uri = if (uriString != null) Uri.parse(uriString) else Uri.EMPTY
        Glide.with(requireContext()).load(uri).into(img)
    }

    //------------------------------------------------------------------------------------
    //DATABASE FUNCTIONS

    private fun getDatabaseValues(folderName: String, userUid:String, sharedPreferences: SharedPreferences, callback: (HashMap<String, Any>) -> Unit) {
        database.child("$folderName/$userUid")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val data = snapshot.getValue<HashMap<String, HashMap<String, Any>>>()
                    val toRecommend = HashMap<String, Any>()

                    if (data != null) {
                        val photoUrls = mutableListOf<String>()
                        val photos = storage.child(folderName)

                        data.forEach { (key, value) ->
                            value["id"] = key
                            Log.d("Key", value.toString())
                        }

                        //Recommendation Algorithm
                        val recommendedPhotos = recommendation(data)
                        Log.d("Recommend", recommendedPhotos.toString())
                        for ((i,ph) in recommendedPhotos.withIndex()) {
                            Log.d("photo", getImageUri(ph,photos).toString())
                            sharedPreferences.edit().putString("r$i", getImageUri(ph,photos).toString()).apply()
                            photoUrls.add(getImageUri(ph,photos).toString())
                        }
                        updateRecommendations(sharedPreferences, photoUrls)
                        Log.d("rp", recommendedPhotos.toString())
                        toRecommend[recommendationId] = photoUrls
                    }
                    else {
                        binding.textPregunta.text = "No hay ropa..."
                    }
                    callback(toRecommend)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("TAG", "Failed to read value.", error.toException())
                }
            })
    }


    //-----------------------------------------------------------------------------
    // SHAREDPREFERENCES FUNCTIONS

    private fun loadData(sharedPreferences: SharedPreferences) {

        sharedPreferences.getString("FORMATTED_DATE", "01-01-24").toString()
        sharedPreferences.getBoolean("new_rec", true)

        binding.aCoruna.text = sharedPreferences.getString("LOC", binding.aCoruna.text.toString())
        binding.weatherDate.text = sharedPreferences.getString("DATE", binding.weatherDate.text.toString())
        binding.temperature.text = sharedPreferences.getString("TEMP", binding.temperature.text.toString())

        binding.weatImg.setImageResource(sharedPreferences.getInt("IMG", R.drawable.w01d_2x))

        binding.noMeGusta.setBackgroundColor(ContextCompat.getColor(requireContext(),
            (sharedPreferences.getInt("nomegusta",R.color.md_theme_background))))
        binding.meGusta.setBackgroundColor(ContextCompat.getColor(requireContext(),
            (sharedPreferences.getInt("megusta",R.color.md_theme_background))))

        convertToURI(binding.prenda1, "prenda1", sharedPreferences)
        convertToURI(binding.prenda2, "prenda2", sharedPreferences)
        convertToURI(binding.prenda3, "prenda3", sharedPreferences)
        convertToURI(binding.prenda4, "prenda4", sharedPreferences)
        val uriList= mutableListOf<String>()
        for (i in 0..3) {
            uriList.add(sharedPreferences.getString("r$i", "")!!)
        }
        toRecommend[recommendationId] = uriList

    }


    private fun resetData(sharedPreferences: SharedPreferences) {
        val memorizedDate: String = sharedPreferences.getString("FORMATTED_DATE", "01-01-24").toString()

        if (memorizedDate != recommendationId) {
            sharedPreferences.edit().putString("FORMATTED_DATE", recommendationId).apply()
            sharedPreferences.edit().putBoolean("new_rec", true).apply()
            sharedPreferences.edit().putInt("nomegusta",R.color.md_theme_background).apply()
            sharedPreferences.edit().putInt("megusta",R.color.md_theme_background).apply()
        }
    }

    //-------------------------------------------------------------------------------------
    //RECOMMENDATION FUNCTIONS
    private fun uploadNewRecommendation(data: Any) {
        if (firebaseAuth.currentUser != null) {
            Log.d("USER", firebaseAuth.currentUser!!.uid)

            val uploadTask = database.child("recommendations")
                .child(firebaseAuth.currentUser!!.uid)
                .child(recommendationId)
                .setValue(data)

            // Register observers to listen for when the upload is done or if it fails
            uploadTask.addOnFailureListener {
                // Handle unsuccessful uploads
                Log.d("DatabaseUpdate", "Upload Failed: ${it.stackTrace}")
            }.addOnSuccessListener {
                Log.d("DatabaseUpdate", "Upload Success: $data")
            }
        }
    }

    private fun deleteRecommendation() {
        if (firebaseAuth.currentUser != null) {
            val userUid = firebaseAuth.currentUser!!.uid
            val deleteTask = database.child("recommendations")
                .child(userUid)
                .child(recommendationId)
                .removeValue()

            deleteTask.addOnFailureListener {
                // Handle unsuccessful deletions
                Log.d("DatabaseDelete", "Delete Failed: ${it.stackTrace}")
            }.addOnSuccessListener {
                Log.d("DatabaseDelete", "Delete Success")
            }
        }
    }

    private fun recommendation(
        data: HashMap<String, HashMap<String, Any>>
    ): List<String> {
        val season = fromMonthToSeason(month.toString())
        val categories = listOf(
            listOf("Top"),
            listOf("Bottom"),
            listOf("Shoe"),
            listOf("Accessories")
        )
        val filteredClothes = data.values.filter { item ->
            val seasons = item["seasons"] as? List<String>
            seasons?.contains(season) == true
        }
        val clothesByCategory = filteredClothes.groupBy { it["category"] }
        Log.d("cc", clothesByCategory.toString())
        val recommendedPhotos = mutableListOf<String>()
        for (category in categories) {
            Log.d("cat", category.toString())
            val clothes = clothesByCategory[category]
            Log.d("filcloth",clothes.toString())

            if (!clothes.isNullOrEmpty()) {
                val randomClothes = clothes.random()
                val photoUrl = randomClothes["photo"]?.toString() ?: ""
                Log.d("photourl", photoUrl)
                recommendedPhotos.add(photoUrl)
            } else {
                recommendedPhotos.add("")
            }
        }

        return recommendedPhotos
    }


    private fun updateRecommendations(sharedPreferences: SharedPreferences, photoUrls: List<String>) {
        val photos = listOf(binding.prenda1, binding.prenda2, binding.prenda3, binding.prenda4)
        photoUrls.forEachIndexed { index, url ->
            if (index < photos.size) {
                sharedPreferences.edit().putString("prenda${index + 1}", url).apply()
                Glide.with(requireContext()).load(url).into(photos[index])
            }
        }
    }


    private fun fromMonthToSeason(month: String): String {
        return when (month) {
            in listOf("3","4","5") -> "Primavera"
            in listOf("6","7","8") -> "Verano"
            in listOf("9","10","11") -> "Otoño"
            in listOf("12","1","2") -> "Invierno"
            else -> "Mes desconocido"
        }
    }

    //----------------------------------------------------------------------------------
    //MANAGE BUTTONS

    private fun manageNoMeGusta(noMeGustaButton: ImageButton, meGustaButton: ImageButton, sharedPreferences: SharedPreferences) {
        // Change color when pressed
        if(!pressedNo) {
            noMeGustaButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.md_theme_error))
            meGustaButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.md_theme_background))

            sharedPreferences.edit().putInt("nomegusta", R.color.md_theme_error).apply()
            sharedPreferences.edit().putInt("megusta", R.color.md_theme_background).apply()
            pressedNo = true
            pressedSi = false
            Toast.makeText(context, "Recomendación quitada del calendario", Toast.LENGTH_SHORT).show()
            //DELETE RECOMMENDATION
            deleteRecommendation()

        }
        else
        {
            noMeGustaButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.md_theme_background))
            sharedPreferences.edit().putInt("nomegusta", R.color.md_theme_background).apply()
            pressedNo = false
        }
    }

    private fun manageMeGusta(noMeGustaButton: ImageButton, meGustaButton: ImageButton, sharedPreferences: SharedPreferences) {
        if(!pressedSi) {
            meGustaButton.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.md_theme_confirm
                )
            )
            sharedPreferences.edit().putInt("megusta", R.color.md_theme_confirm).apply()

            noMeGustaButton.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.md_theme_background
                )
            )
            sharedPreferences.edit().putInt("nomegusta", R.color.md_theme_background).apply()
            Toast.makeText(context, "Recomendación añadida al calendario", Toast.LENGTH_SHORT).show()
            pressedNo = false
            pressedSi = true
        }
        else {
            meGustaButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.md_theme_background))
            sharedPreferences.edit().putInt("megusta", R.color.md_theme_background).apply()
            pressedSi = false
        }


    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}

