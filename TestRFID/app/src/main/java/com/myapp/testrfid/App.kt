package com.myapp.testrfid

import android.app.Application
import android.content.Context

class App : Application() {

    init{
        instance = this
    }

    override fun onCreate() {
        super.onCreate()

    }
    companion object {
        var instance: App? = null
        fun context() : Context {
            return instance!!.applicationContext
        }
    }



}