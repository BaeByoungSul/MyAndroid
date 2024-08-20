package com.myapp.myframedagger.common.di_simple

import dagger.Module
import dagger.Provides

@Module
class CoffeeMakerModule {
    @Provides
    fun provideHeater() = Heater()
    @Provides
    fun providePump() = Pump()
}
