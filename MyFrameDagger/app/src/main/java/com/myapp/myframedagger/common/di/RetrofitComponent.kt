package com.myapp.myframedagger.common.di

import com.myapp.myframedagger.MainActivity
import com.myapp.myframedagger.view.RetrofitTestActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [RetrofitModule::class])
interface RetrofitComponent {
    fun inject( activity: MainActivity)
    fun inject( activity: RetrofitTestActivity)
}