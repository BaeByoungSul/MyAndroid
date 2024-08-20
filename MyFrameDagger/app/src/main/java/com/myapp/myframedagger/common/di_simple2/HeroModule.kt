package com.myapp.myframedagger.common.di_simple2

import dagger.Module
import dagger.Provides

// module은 객체를 생성해서 공급해주는 역할을 합니다.
@Module
class HeroModule {
    @Provides
    fun providePerson(): Person = IronMan()

    @Provides
    fun provideWeapon(): Weapon = Suit()

//    @Provides
//    fun provideHero(person: Person, weapon: Weapon) =  Hero(person, weapon)
}