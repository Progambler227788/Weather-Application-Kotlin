package com.example.weatherapplication

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.weatherapplication.constants.Constants
import com.example.weatherapplication.databinding.ActivityMainBinding
import com.example.weatherapplication.models.WeatherModel
import com.example.weatherapplication.utilities.ApiInterface
import com.example.weatherapplication.utilities.ApiUtilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.RoundingMode

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val weatherApi = ApiUtilities.getInstance().create(ApiInterface::class.java)
        val city = if (binding.enterCity.text.toString()!="") binding.enterCity.text.toString() else "Lahore"
        lifecycleScope.launch(Dispatchers.IO) {
            val result = weatherApi.getCityWeather(city,Constants.apiKey).body()

            runOnUiThread {
                setUi(result)
                Toast.makeText(this@MainActivity, result!!.weather[0].main,Toast.LENGTH_SHORT).show()
            }


        }

    }
    private fun k2c(t:Double):Double{

        var intTemp=t

        intTemp=intTemp.minus(273)

        return intTemp.toBigDecimal().setScale(1, RoundingMode.UP).toDouble()
    }

    @SuppressLint("SetTextI18n")
    fun setUi(body : WeatherModel?){
        binding.apply {
         currentTemperature.text = "${k2c(body?.main!!.temp)}°"
         minTemp.text = "Minimum :${k2c(body.main.temp_min)}°"
         maxTemp.text = "Minimum :${k2c(body.main.temp_max)}°"
        }

    }

}