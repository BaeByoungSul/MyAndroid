package com.myapp.myframedagger.common.di_simple2

import dagger.Component


@Component(modules = [HeroModule::class])
interface HeroComponent {
    // 함수명은 상관없으며, 생성할 객체가 Hero이기 때문에 return값이 Hero인 abstract 함수를 하나 만들어 둡니다.
    //fun callHero():Hero

    fun inject(hero: Hero)
}