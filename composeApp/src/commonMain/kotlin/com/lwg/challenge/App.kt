package com.lwg.challenge

import com.lwg.challenge.di.AppModule
import com.lwg.challenge.feature.ex1.di.Ex1Module
import com.lwg.challenge.feature.ex2.di.Ex2Module
import com.lwg.challenge.feature.ex3.di.Ex3Module
import com.lwg.challenge.feature.home.di.HomeModule
import org.koin.core.KoinApplication
import org.koin.dsl.KoinAppDeclaration
import org.koin.ksp.generated.module

internal fun cookingAppDeclaration(
    additionalDeclaration: KoinApplication.() -> Unit = {},
): KoinAppDeclaration = {
    modules(
        AppModule().module,
        HomeModule().module,
        Ex1Module().module,
        Ex2Module().module,
        Ex3Module().module,
    )
    additionalDeclaration()
}
