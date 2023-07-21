package com.example.weatherapplication

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import java.text.SimpleDateFormat
import android.widget.Toast
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

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private lateinit var adapter : AdapterWeather
    private lateinit var data : ArrayList<Specs>
    private var city =  "Lahore"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        callApi(city)
        binding.currentLocation.setOnClickListener {
            check()
        }

        binding.enterCity.setOnEditorActionListener { _, i, _ ->
            if(i==EditorInfo.IME_ACTION_SEARCH){
                check()
                return@setOnEditorActionListener true

            }
            else

                return@setOnEditorActionListener false
        }
    }
    private fun check(){
        city = binding.enterCity.text.toString()
        if(city!="Lahore" && city!=""  ){
            callApi(city)
        }
    }
    private fun callApi(city : String){
        val weatherApi = ApiUtilities.getInstance().create(ApiInterface::class.java)

        lifecycleScope.launch(Dispatchers.IO) {
            val result = weatherApi.getCityWeather(city,Constants.apiKey).body()
            runOnUiThread {
                if (result != null) {

                    setUi(result)
                    Toast.makeText(this@MainActivity, result.weather[0].main, Toast.LENGTH_SHORT)
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
            else -> SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
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
        data.add(Specs(R.drawable.city,body.name,"City",back))
        data.add(Specs(R.drawable.pressure,body.main.pressure.toString()+" hPa","Pressure",back))
        data.add(Specs(R.drawable.wind,body.wind.speed.toString()+"m/s","Wind",back))
        data.add(Specs(R.drawable.humidity,body.main.humidity.toString()+"%","Humidity",back))
        data.add(Specs(R.drawable.sunrise,convertDate(body.sys.sunrise.toLong(),1),"Sunrise",back))
        data.add(Specs(R.drawable.sunset,convertDate(body.sys.sunset.toLong(),1),"Sunset",back))
        data.add(Specs(R.drawable.visibility,body.visibility.toString(),"Visibility",back))


        adapter = AdapterWeather(data,this)

        binding.apply {
         currentTemperature.text = "${k2c(body.main.temp)}°"
         minTemp.text = "Minimum :${k2c(body.main.temp_min)}°"
         maxTemp.text = "Maximum :${k2c(body.main.temp_max)}°"
            dateTime.text = convertDate(body.dt.toLong(),2)
            recyclerView.layoutManager = GridLayoutManager(this@MainActivity,3)
           //
            recyclerView.adapter = adapter

        }

    }

    override fun onResume() {
        super.onResume()
        callApi(city)
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
        return  -1



    }

}