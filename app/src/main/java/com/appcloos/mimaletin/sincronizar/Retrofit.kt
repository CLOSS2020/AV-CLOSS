package com.appcloos.mimaletin.sincronizar

import com.appcloos.mimaletin.Constantes
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object Retrofit {
    private val gson = GsonBuilder().setLenient().create()

    private val okHttpClient: OkHttpClient =
        OkHttpClient.Builder().readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS).build()

    fun getRetrofit1(): Retrofit {
        return Retrofit.Builder().baseUrl(Constantes.URL1)
            .addConverterFactory(GsonConverterFactory.create(gson)).client(okHttpClient).build()
    }

    fun getRetrofit2(): Retrofit {
        return Retrofit.Builder().baseUrl(Constantes.URL2)
            .addConverterFactory(GsonConverterFactory.create(gson)).client(okHttpClient).build()
    }

    fun getApi(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}