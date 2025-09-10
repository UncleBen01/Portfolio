package com.example.tp1

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import org.kickmyb.CustomGson
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object UtilitaireRetrofit {
    private var instance: Service? = null

    fun get(): Service {
        if (instance == null) { //  ca sera le cas au tout premier appel
            val retrofit = Retrofit.Builder()
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(CustomGson.getIt()))
                .client(client())
                .baseUrl("http://10.0.2.2:8080/")
                .build()
            instance = retrofit.create<Service?>(Service::class.java)
            return instance!!
        } else {
            return instance!!
        }
    }

    private fun client(): OkHttpClient {
        return OkHttpClient.Builder()
            .cookieJar(SessionCookieJar)
            .build()
    }
}