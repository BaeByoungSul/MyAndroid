package com.myapp.myframedagger

import android.app.Application
import android.content.Context
import com.myapp.myframedagger.common.di.DaggerRetrofitComponent
import com.myapp.myframedagger.common.di.RetrofitComponent

class App : Application() {
    lateinit var retrofitComponent: RetrofitComponent
    init{
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        retrofitComponent = DaggerRetrofitComponent.builder().build()
    }
    companion object {
        var instance: App? = null
        fun context() : Context {
            return instance!!.applicationContext
        }
    }



}