package com.lwg.challenge

import com.lwg.challenge.core.invite.di.platformInviteModule
import com.lwg.challenge.di.AppModule
import com.lwg.challenge.feature.friends.list.di.FriendsModule
import com.lwg.challenge.feature.friends.search.di.FriendsSearchModule
import com.lwg.challenge.feature.home.di.HomeModule
import com.lwg.challenge.feature.login.di.LoginModule
import com.lwg.challenge.feature.login.di.platformLoginModule
import com.lwg.challenge.feature.main.di.MainModule
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
        MainModule().module,
        HomeModule().module,
        LoginModule().module,
        platformLoginModule,
        platformInviteModule,
        SplashModule().module,
        FriendsModule().module,
        FriendsSearchModule().module,
        RankingModule().module,
        MyPageModule().module,
    )
    additionalDeclaration()
}
