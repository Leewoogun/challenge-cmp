package com.lwg.challenge.remote.api.di

import com.lwg.challenge.remote.api.ActiveChallengeApi
import com.lwg.challenge.remote.api.LoginApi
import com.lwg.challenge.remote.api.RecordApi
import com.lwg.challenge.remote.api.createActiveChallengeApi
import com.lwg.challenge.remote.api.createLoginApi
import com.lwg.challenge.remote.api.createRecordApi
import de.jensklingenberg.ktorfit.Ktorfit
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class ApiModule {

    @Single
    fun provideLoginApi(ktorfit: Ktorfit): LoginApi = ktorfit.createLoginApi()

    @Single
    fun provideRecordApi(ktorfit: Ktorfit): RecordApi = ktorfit.createRecordApi()

    @Single
    fun provideActiveChallengeApi(ktorfit: Ktorfit): ActiveChallengeApi = ktorfit.createActiveChallengeApi()
}
