package com.myapp.myframedagger.common.di_simple

fun main() {
    val coffeeMakerComponent = DaggerCoffeeMakerComponent.create()
    val coffeeMaker = coffeeMakerComponent.getCoffeeMaker()

    println(coffeeMaker.makeCoffee())
}