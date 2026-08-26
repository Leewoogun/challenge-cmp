package com.lwg.challenge.remote.api.di

import com.lwg.challenge.remote.api.ActiveChallengeApi
import com.lwg.challenge.remote.api.ChallengeApi
import com.lwg.challenge.remote.api.FriendsApi
import com.lwg.challenge.remote.api.LoginApi
import com.lwg.challenge.remote.api.RankingApi
import com.lwg.challenge.remote.api.RecordApi
import com.lwg.challenge.remote.api.UserApi
import com.lwg.challenge.remote.api.VerificationApi
import com.lwg.challenge.remote.api.createActiveChallengeApi
import com.lwg.challenge.remote.api.createChallengeApi
import com.lwg.challenge.remote.api.createFriendsApi
import com.lwg.challenge.remote.api.createLoginApi
import com.lwg.challenge.remote.api.createRankingApi
import com.lwg.challenge.remote.api.createRecordApi
import com.lwg.challenge.remote.api.createUserApi
import com.lwg.challenge.remote.api.createVerificationApi
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

    @Single
    fun provideChallengeApi(ktorfit: Ktorfit): ChallengeApi = ktorfit.createChallengeApi()

    @Single
    fun provideFriendsApi(ktorfit: Ktorfit): FriendsApi = ktorfit.createFriendsApi()

    @Single
    fun provideUserApi(ktorfit: Ktorfit): UserApi = ktorfit.createUserApi()

    @Single
    fun provideVerificationApi(ktorfit: Ktorfit): VerificationApi = ktorfit.createVerificationApi()

    @Single
    fun provideRankingApi(ktorfit: Ktorfit): RankingApi = ktorfit.createRankingApi()
}
