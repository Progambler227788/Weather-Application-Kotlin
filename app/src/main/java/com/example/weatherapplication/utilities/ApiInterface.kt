package com.example.weatherapplication.utilities

import com.example.weatherapplication.models.WeatherModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {
    @GET("weather")
    suspend fun getCurrentWeatherData(
        @Query("lat") lat:String,
        @Query("lon") lon:String,
        @Query("APPID") appid:String
    ): Response<WeatherModel>  // Call<>
 @GET("weather")
 suspend fun getCityWeather(  @Query("q") q:String,
                              @Query("APPID") appid:String
 ) : Response<WeatherModel>
}