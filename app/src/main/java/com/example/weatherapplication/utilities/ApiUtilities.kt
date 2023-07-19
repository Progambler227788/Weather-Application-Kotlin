package com.example.weatherapplication.utilities

import com.example.weatherapplication.constants.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiUtilities {
    fun getInstance() : Retrofit {
        return Retrofit.Builder().baseUrl(Constants.baseUrl).
        addConverterFactory(GsonConverterFactory.create()).build()
    }
}