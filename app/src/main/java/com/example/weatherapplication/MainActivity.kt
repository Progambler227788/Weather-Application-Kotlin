package com.example.weatherapplication

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import java.text.SimpleDateFormat
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.weatherapplication.adapter.AdapterWeather
import com.example.weatherapplication.constants.Constants
import com.example.weatherapplication.databinding.ActivityMainBinding
import com.example.weatherapplication.models.Specs
import com.example.weatherapplication.models.WeatherModel
import com.example.weatherapplication.utilities.ApiInterface
import com.example.weatherapplication.utilities.ApiUtilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.RoundingMode
import java.util.*
import kotlin.collections.ArrayList
import  android.Manifest
import android.location.*
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import com.example.weatherapplication.models.City
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest
import com.google.gson.Gson
import java.io.IOException
import java.io.InputStream

@Suppress("DEPRECATION", "SameParameterValue")
class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private lateinit var adapter : AdapterWeather
    private lateinit var data : ArrayList<Specs>
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
  //  private late init var locationManager : LocationManager
    private val requestCode= 1001
    private var city =  "Lahore"
    private var result : WeatherModel? = null
    private var latitude = ""
    private var longitude = ""
    private lateinit var citiesList: List<String> // list of cities
    private lateinit var adapterCities: ArrayAdapter<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create().apply {
            interval = 5000 // Update location every 5 seconds
            fastestInterval = 2000 // Fastest update interval 2 seconds
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        checkLocationPermission()


        binding.currentLocation.setOnClickListener {
            checkLocationPermission()
        }
       // Read the JSON file and extract city names
                try {
                    val jsonCities = loadJSONFromAsset("cities.json")
                    val cityNames = Gson().fromJson(jsonCities, Array<City>::class.java).toList()

                    // Now, the "citiesList" contains the list of cities with their names.
                    // You can access the names as follows:
                    citiesList = cityNames.map { it.name }
                    // Use cityNames array as needed.
                } catch (e: IOException) {
                    e.printStackTrace()
                }

        binding.enterCity.addTextChangedListener(cityTextWatcher)

        // Create ArrayAdapter with your cities list
        adapterCities = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, citiesList)
        // Set the ArrayAdapter as the adapter for AutoCompleteTextView
        binding.enterCity.setAdapter(adapterCities)

        binding.enterCity.setOnEditorActionListener { _, i, _ ->
            if(i==EditorInfo.IME_ACTION_SEARCH){
                check()
                val view=this.currentFocus

                if (view!=null){

                    val imm:InputMethodManager=getSystemService(INPUT_METHOD_SERVICE)
                            as InputMethodManager

                    imm.hideSoftInputFromWindow(view.windowToken,0)

                    binding.enterCity.clearFocus()


                }
                return@setOnEditorActionListener true

            }
            else

                return@setOnEditorActionListener false
        }
    }

    private val cityTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            val input = s?.toString()?.trim() ?: ""
            val filteredCities = citiesList.filter { it.startsWith(input, ignoreCase = true) }

            // Display the filtered suggestions in a dropdown list
            val adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_dropdown_item_1line, filteredCities)
            binding.enterCity.setAdapter(adapter)
            binding.enterCity.showDropDown()
        }
    }
    private fun loadJSONFromAsset(fileName: String): String {
        val json: String
        try {
            val inputStream: InputStream = assets.open(fileName)
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            json = String(buffer, Charsets.UTF_8)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return ""
        }
        return json
    }
    private fun checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, request it from the user
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                this.requestCode
            )
        }
        else {
            startLocationUpdates()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode ==this.requestCode && grantResults.isNotEmpty() && grantResults[0]== PackageManager.PERMISSION_GRANTED){
            startLocationUpdates()
        }
        else{
            callApi(city,1)
            Toast.makeText(this, "Location permission denied. So, Lahore will be used.", Toast.LENGTH_SHORT).show()
        }
    }
//    private fun getCityName(latitude: Double, longitude: Double): String {
//        val geocoder = Geocoder(this, Locale.getDefault())
//        try {
//            val addresses: MutableList<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
//            if (addresses!!.isNotEmpty()) {
//                val address: Address = addresses[0]
//                return address.locality ?: address.subAdminArea ?: address.adminArea ?: ""
//            }
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//        return "Unknown"
//    }
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                // When the user's location is available, this block will be executed
                latitude = location.latitude.toString()
                longitude = location.longitude.toString()

                callApi(city,2)
            } else {
                callApi(city,1)
                // If the location is null, handle the situation accordingly
                Toast.makeText(this, "Location not available. So, Lahore will be used. Please on your gps", Toast.LENGTH_SHORT).show()
            }

        }
    }


    private fun check(){
        city = binding.enterCity.text.toString()
            // Toast.makeText(this, city, Toast.LENGTH_SHORT).show()
        if(city!="Lahore" && city!=""  ){
         //   Toast.makeText(this, city, Toast.LENGTH_SHORT).show()
            callApi(city,1)
        }
    }


    private fun callApi(city : String, code : Int){
        val weatherApi = ApiUtilities.getInstance().create(ApiInterface::class.java)

        lifecycleScope.launch(Dispatchers.IO) {
            result = if(code == 1)
                weatherApi.getCityWeather(city,Constants.apiKey).body()
            else
                weatherApi.getCurrentWeatherData(latitude,longitude,Constants.apiKey).body()
            runOnUiThread {
                if (result != null) {

                    setUi(result)
                    binding.enterCity.setText(result!!.name)
                    Toast.makeText(this@MainActivity, result!!.weather[0].main, Toast.LENGTH_SHORT)
                        .show()

                } else {

                    Toast.makeText(this@MainActivity, "Enter Correct City !!", Toast.LENGTH_SHORT)
                        .show()
                }
            }


        }
    }


    private fun convertDate(unixTimestamp: Long, code: Int): String {
        // Note: Multiplying by 1000 is necessary because the provided timestamp is in seconds,
        // and Date() expects milliseconds.

        val date = Date(unixTimestamp * 1000)
        val format = when (code) {
            1 -> SimpleDateFormat("HH:mm", Locale.US)
            else -> SimpleDateFormat("EEEE, yyyy-MM-dd HH:mm", Locale.US)
        }
        return format.format(date)
    }


    private fun k2c(t:Double):Double{

        var intTemp=t

        intTemp=intTemp.minus(273)

        return intTemp.toBigDecimal().setScale(1, RoundingMode.UP).toDouble()
    }

    @SuppressLint("SetTextI18n")
    fun setUi(body : WeatherModel?){
        data = ArrayList()
        val back = updateUI(body!!.weather[0].id)
            //    val back = ContextCompat.getDrawable(this@MainActivity,  updateUI(body!!.weather[0].id))
        data.add(Specs(R.drawable.temperature,k2c(body.main.temp).toString(),"Temperature",back))
        data.add(Specs(R.drawable.country,body.sys.country,"Country",back))
        data.add(Specs(R.drawable.city,body.name,"Location",back))
        data.add(Specs(R.drawable.pressure,body.main.pressure.toString()+" hPa","Pressure",back))
        data.add(Specs(R.drawable.wind,body.wind.speed.toString()+"m/s","Wind",back))
        data.add(Specs(R.drawable.humidity,body.main.humidity.toString()+"%","Humidity",back))
        data.add(Specs(R.drawable.sunrise,convertDate(body.sys.sunrise.toLong(),1),"Sunrise",back))
        data.add(Specs(R.drawable.sunset,convertDate(body.sys.sunset.toLong(),1),"Sunset",back))
        data.add(Specs(R.drawable.visibility,body.visibility.toString(),"Visibility",back))


        adapter = AdapterWeather(data,this)

        binding.apply {
            descript.text ="Weather Type : " + body.weather[0].main
         currentTemperature.text = "${k2c(body.main.temp)}°"
         minTemp.text = "Minimum :${k2c(body.main.temp_min)}°"
         maxTemp.text = "Maximum :${k2c(body.main.temp_max)}°"
            dateTime.text = convertDate(body.dt.toLong(),2)
            recyclerView.layoutManager = GridLayoutManager(this@MainActivity,3)
           //
            recyclerView.adapter = adapter

        }

    }


    private fun applyImages(one : Int, two : Int) : Int{
        binding.apply {
            currentWeatherPic.setImageResource(one)

         //   mainLayout.background= ContextCompat
              //  .getDrawable(this@MainActivity, two)

         //   recyclerView.background= ContextCompat
             //   .getDrawable(this@MainActivity, two)
          //  linearLayoutOfTextViews.background= ContextCompat
             //   .getDrawable(this@MainActivity, two)
         //   cardViewLayout.background= ContextCompat
              //  .getDrawable(this@MainActivity, two)
//            adapter.updateItemBackgroundColor(ContextCompat
//                .getDrawable(this@MainActivity, two)!!)
        }
        return two

    }
    private fun updateUI(id: Int) : Int{

        binding.apply {


            when (id) {

                //Thunderstorm
                in 200..232 -> {

                   return applyImages(R.drawable.ic_storm_weather,R.drawable.thunderstrom_bg)


                }

                //Drizzle
                in 300..321 -> {
                    return   applyImages(R.drawable.ic_few_clouds,R.drawable.drizzle_bg)

                }

                //Rain
                in 500..531 -> {
                    return  applyImages(R.drawable.ic_rainy_weather,R.drawable.rain_bg)



                }

                //Snow
                in 600..622 -> {
                    return applyImages(R.drawable.ic_snow_weather,R.drawable.snow_bg)


                }

                //Atmosphere
                in 701..781 -> {
                    return  applyImages(R.drawable.ic_broken_clouds,R.drawable.atmosphere_bg)

                }

                //Clear
                800 -> {
                    return applyImages(R.drawable.ic_clear_day,R.drawable.clear_bg)

                }

                //Clouds
                in 801..804 -> {
                    return applyImages(R.drawable.ic_cloudy_weather,R.drawable.clouds_bg)

                }

                //unknown
                else->{
                    return  applyImages(R.drawable.ic_unknown,R.drawable.unknown_bg)

                }


            }






        }


    }

}