package com.example.bazaar.api

import com.example.bazaar.utils.Constants.BASE_URL
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitInstance {

    private val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

    val retrofit = Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(BASE_URL)
            .build()

    val api: MarketApi by lazy {
        retrofit.create(MarketApi::class.java)
    }
}