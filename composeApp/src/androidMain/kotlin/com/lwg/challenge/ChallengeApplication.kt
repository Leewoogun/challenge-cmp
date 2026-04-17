package com.lwg.challenge

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ChallengeApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin(
            cookingAppDeclaration {
                androidContext(this@ChallengeApplication)
            },
        )
    }
}