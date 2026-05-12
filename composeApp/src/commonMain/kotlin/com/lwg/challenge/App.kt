package com.lwg.challenge

import com.lwg.challenge.di.AppModule
import com.lwg.challenge.feature.friends.di.FriendsModule
import com.lwg.challenge.feature.home.di.HomeModule
import com.lwg.challenge.feature.login.di.LoginModule
import com.lwg.challenge.feature.login.di.platformLoginModule
import com.lwg.challenge.feature.mypage.di.MyPageModule
import com.lwg.challenge.feature.ranking.di.RankingModule
import com.lwg.challenge.feature.splash.di.SplashModule
import org.koin.core.KoinApplication
import org.koin.dsl.KoinAppDeclaration
import org.koin.ksp.generated.module

internal fun cookingAppDeclaration(
    additionalDeclaration: KoinApplication.() -> Unit = {},
): KoinAppDeclaration = {
    modules(
        AppModule().module,
        HomeModule().module,
        LoginModule().module,
        platformLoginModule,
        SplashModule().module,
        FriendsModule().module,
        RankingModule().module,
        MyPageModule().module,
    )
    additionalDeclaration()
}
