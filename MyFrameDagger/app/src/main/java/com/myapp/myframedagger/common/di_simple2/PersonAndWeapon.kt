package com.myapp.myframedagger.common.di_simple2

import android.util.Log
import javax.inject.Inject

interface Person {
    fun name(): String
    fun skill(): String
}

interface Weapon {
    fun type(): String
}

//class Hero @Inject constructor(private val person: Person, private val weapon: Weapon) {
//    fun info() {
//        println( "name: ${person.name()} skill: ${person.skill()} | weapon:${weapon.type()}")
//        //Log.d("doo", "name: ${person.name()} skill: ${person.skill()} | weapon:${weapon.type()}")
//    }
//}
class Hero {

    @Inject
    lateinit var person: Person
    @Inject
    lateinit var weapon: Weapon

    fun info() {
        println( "name: ${person.name()} skill: ${person.skill()} | weapon:${weapon.type()}")
    }
}
