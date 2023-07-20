package com.example.weatherapplication

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Adapter
import android.widget.GridLayout
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.weatherapplication.adapter.AdapterWeather
import com.example.weatherapplication.constants.Constants
import com.example.weatherapplication.databinding.ActivityMainBinding
import com.example.weatherapplication.databinding.RvRecyclerviewBinding
import com.example.weatherapplication.models.Specs
import com.example.weatherapplication.models.WeatherModel
import com.example.weatherapplication.utilities.ApiInterface
import com.example.weatherapplication.utilities.ApiUtilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.RoundingMode

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private lateinit var adapter : AdapterWeather
    private lateinit var data : ArrayList<Specs>
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
                adapterSetting()
                Toast.makeText(this@MainActivity, result!!.weather[0].main,Toast.LENGTH_SHORT).show()
            }


        }

    }
    fun adapterSetting(){
        data = ArrayList()
        data.add(Specs(R.drawable.pressure,"32","Pressure"))
        data.add(Specs(R.drawable.wind,"32","Wind"))
        data.add(Specs(R.drawable.humidity,"32","Humidity"))
        data.add(Specs(R.drawable.sunrise,"32","Sunrise"))
        data.add(Specs(R.drawable.sunset,"33","Sunset"))
        data.add(Specs(R.drawable.temperature,"33","Temperature"))
        data.add(Specs(R.drawable.ground_level,"33","Ground Level"))
        data.add(Specs(R.drawable.country,"PK","Country"))
        data.add(Specs(R.drawable.water,"0","Sea Level"))
        adapter = AdapterWeather(data,this)
        binding.apply {
            recyclerView.layoutManager = GridLayoutManager(this@MainActivity,3)
            recyclerView.adapter = adapter
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
         maxTemp.text = "Maximum :${k2c(body.main.temp_max)}°"
        }

    }

}