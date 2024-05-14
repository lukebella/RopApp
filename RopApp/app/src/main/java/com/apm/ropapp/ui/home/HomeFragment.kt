package com.apm.ropapp.ui.home

//import com.google.android.gms.maps.model.LatLng
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.location.Geocoder
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.apm.ropapp.R
import com.apm.ropapp.databinding.FragmentHomeBinding
import com.apm.ropapp.ui.closet.ClosetFragment
import com.apm.ropapp.ui.closet.CustomAdapter
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
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var pressedNo: Boolean = false
    var pressedSi: Boolean = false

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var storage: StorageReference

    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH) + 1 // Month is zero-based
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale("es", "ES"))
    val recommendationId = dateFormat.format(calendar.time)
    var toRecommend = HashMap<String, Any> ()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("HOME", "init")
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val locationTextView = binding.aCoruna
        val sharedPreferences =
            requireContext().getSharedPreferences("MySharedPreferences", MODE_PRIVATE)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance(getString(R.string.database_url)).reference
        storage = FirebaseStorage.getInstance(getString(R.string.storage_url)).reference

        val noMeGustaButton = binding.noMeGusta
        val meGustaButton = binding.meGusta
        val userUid = firebaseAuth.currentUser?.uid


        //metodo para recomendar
        //TODO: comparar data sacada con la odierna para reinizializar buttons y recomendacion

        loadData(sharedPreferences)

        fun getImageUri(fileName: String, photos: StorageReference): Uri {
            val dir = File("${root.context.getExternalFilesDir(null)}/clothes")
            if (!dir.exists()) dir.mkdirs()
            val imageFile = File(dir, fileName)

            if (!imageFile.exists()) {
                imageFile.createNewFile()
                photos.child(fileName).getFile(imageFile)
            }
            return FileProvider.getUriForFile(
                root.context, "com.apm.ropapp.FileProvider", imageFile
            )
        }

        fun getDatabaseValues(folderName: String) : HashMap<String, Any> {

            val toRecommend = HashMap<String, Any> ()

            database.child("$folderName/$userUid")
                .addValueEventListener(object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        val data = snapshot.getValue<HashMap<String, HashMap<String, Any>>>()
                        //{aa3722c9-f5c9-4d6c-b5dc-f04bce8d29b3={seasons=[Verano, Invierno], photo=IMG_67929f11-76ac-452e-bcc3-101fa466ed2a.png,
                        // details={size=L, price=20 €, state=Prestado, brand=M&M}, style=[Vintage, Glamorous], category=[Scarf]}}
                        if (data != null) {
                            val dataList = mutableListOf<HashMap<String, Any>>()
                            val imageList = mutableListOf<Uri>()
                            val photos = storage.child(folderName)

                            data.forEach { (key, value) ->
                                value["id"] = key
                                Log.d("Key", value.toString())
                                dataList.add(value)
                                if (value["photo"] == null) imageList.add(Uri.EMPTY)
                                else imageList.add(getImageUri(value["photo"].toString(), photos))
                            }

                            fun updatePrendaRec(str: String, uri: Uri, img: ImageView) {
                                sharedPreferences.edit().putString(str, uri.toString()).apply()
                                Glide.with(requireContext()).load(uri).into(img)
                            }

                            //TODO: I put the first 4 clothes as recommendations, do the random process with the categories etc.

                            updatePrendaRec("prenda1", imageList[0], binding.prenda1)
                            updatePrendaRec("prenda2", imageList[1], binding.prenda2)
                            updatePrendaRec("prenda3", imageList[2], binding.prenda3)
                            updatePrendaRec("prenda4", imageList[3], binding.prenda4)

                            toRecommend[recommendationId] = imageList.take(4)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.w("TAG", "Failed to read value.", error.toException())
                    }
                })

            return toRecommend
        }

        toRecommend = getDatabaseValues("clothes")
        uploadNewRecommendation(toRecommend)

        var tmsUpdate = sharedPreferences.getLong("LONG_KEY", 0)
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
        noMeGustaButton.setOnClickListener {
            manageNoMeGusta(noMeGustaButton, meGustaButton, sharedPreferences)

            /*
            - Remove from the database the 4 clothes with the images (key is the date) if existent
             */

        }
        meGustaButton.setOnClickListener {
            manageMeGusta(noMeGustaButton, meGustaButton, sharedPreferences)
            /*
            - Write in the database the 4 clothes with the images (key is the date)
             */
            uploadNewRecommendation(toRecommend)

        }

        return root
    }

    private fun weatherForecast(lat: Double, longit: Double, key: String) {
        val url = "https://api.openweathermap.org/data/2.5/forecast?lat=$lat&lon=$longit&lang=es&appid=$key"
        FetchWeatherTask().execute(url)
    }

   override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
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
                            sharedPreferences.edit().putLong("LONG_KEY", System.currentTimeMillis()).apply()
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

            var dateToday = "$year-0$month-$dayString"
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

    private fun convertToURI(img: ImageView, str: String, sharedPreferences: SharedPreferences) {
        val uriString = sharedPreferences.getString(str, null)
        val uri = if (uriString != null) Uri.parse(uriString) else Uri.EMPTY
        Glide.with(requireContext()).load(uri).into(img)
    }

    private fun loadData(sharedPreferences: SharedPreferences) {

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


    }

    //TODO: upload recommendation
    private fun uploadNewRecommendation(data: HashMap<String, Any>) {
        if (firebaseAuth.currentUser != null) {
            val uploadTask = database.child("recommendations").child(firebaseAuth.currentUser!!.uid)
                .child(recommendationId).setValue(data)
            // Register observers to listen for when the upload is done or if it fails
            uploadTask.addOnFailureListener {
                // Handle unsuccessful uploads
                Log.d("DatabaseUpdate", "Upload Failed: ${it.stackTrace}")
            }.addOnSuccessListener {
                // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
                Log.d("DatabaseUpdate", "Upload Success: $data")
            }
        }
    }

    private fun manageNoMeGusta(noMeGustaButton: ImageButton, meGustaButton: ImageButton, sharedPreferences: SharedPreferences) {
        // Change color when pressed
        if(!pressedNo) {
            noMeGustaButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.md_theme_error))
            meGustaButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.md_theme_background))

            sharedPreferences.edit().putInt("nomegusta", R.color.md_theme_error).apply()
            sharedPreferences.edit().putInt("megusta", R.color.md_theme_background).apply()
            pressedNo = true
            pressedSi = false
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

            pressedNo = false
            pressedSi = true
        }
        else {
            meGustaButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.md_theme_background))
            sharedPreferences.edit().putInt("megusta", R.color.md_theme_background).apply()
            pressedSi = false
        }


    }


    data class WeatherData(val weat: String, val tempMin: Int, val tempMax: Int, val data: String)

}

