package com.lwg.challenge.remote.api.di

import com.lwg.challenge.remote.api.LoginApi
import com.lwg.challenge.remote.api.MovieApi
import com.lwg.challenge.remote.api.createLoginApi
import com.lwg.challenge.remote.api.createMovieApi
import de.jensklingenberg.ktorfit.Ktorfit
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class ApiModule {

    @Single
    fun provideMovieApi(ktorfit: Ktorfit): MovieApi = ktorfit.createMovieApi()

    @Single
    fun provideLoginApi(ktorfit: Ktorfit): LoginApi = ktorfit.createLoginApi()
}
