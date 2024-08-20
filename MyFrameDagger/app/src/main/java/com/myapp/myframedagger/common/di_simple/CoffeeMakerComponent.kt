package com.myapp.myframedagger.common.di_simple

import dagger.Component

@Component(modules = [CoffeeMakerModule::class])
interface CoffeeMakerComponent {
    fun getCoffeeMaker() : CoffeeMaker
}