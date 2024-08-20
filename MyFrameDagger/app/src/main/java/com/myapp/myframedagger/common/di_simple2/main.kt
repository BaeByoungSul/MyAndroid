package com.myapp.myframedagger.common.di_simple2

class IronMan: Person {
    override fun name() = "토니 스타크"
    override fun skill() = "수트 변형"
}

class Suit : Weapon {
    override fun type() = "수트"
}

fun  main() {
    val hero = Hero()
    //hero.info()

    DaggerHeroComponent.create().inject(hero)
    hero.info()

    //val hero= DaggerHeroComponent.create().callHero()
    //hero.info()

//    val person = IronMan()
//    val weapon = Suit()
//    val hero = Hero(person, weapon)
//
//    hero.info()
//
}