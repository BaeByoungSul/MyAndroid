package com.myapp.myframedagger.common.retrofit_myapi

import com.myapp.myframedagger.common.utils.Constants.BASE_URL
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitObject {
//    companion object {
//        //통신할 서버 url
//        private const val baseUrl = "http://12.345.678.910"
//
//        //Retrofit 객체 초기화
//        val retrofit: Retrofit = Retrofit.Builder()
//            .baseUrl(this.baseUrl)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//
//        val test  = retrofit.create(ApiService::class.java)
//    }
    companion object {
        private val retrofit by lazy {
            val logger = HttpLoggingInterceptor()
            //to be able to read the response body
            logger.setLevel(HttpLoggingInterceptor.Level.BODY)
            val client = OkHttpClient.Builder()
                .protocols(listOf(Protocol.HTTP_1_1))
                .addInterceptor(logger)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build()

            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
        }

        val api: ApiService by lazy {
            retrofit.create(ApiService::class.java)
        }
    }
}