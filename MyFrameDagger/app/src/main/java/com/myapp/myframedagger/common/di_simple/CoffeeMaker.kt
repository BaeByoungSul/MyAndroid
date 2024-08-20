package com.myapp.myframedagger.common.di_simple

import javax.inject.Inject

class CoffeeMaker @Inject constructor (private val heater: Heater, private val pump: Pump) {
    fun makeCoffee() = "${heater.heatWater()} and ${pump.pumpWater()}"
}
