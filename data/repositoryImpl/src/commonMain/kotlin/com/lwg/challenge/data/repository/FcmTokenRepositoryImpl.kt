package com.lwg.challenge.data.repository

import com.lwg.challenge.data.datasource.remote.FcmTokenRemoteDataSource
import com.lwg.challenge.domain.repository.FcmTokenRepository
import org.koin.core.annotation.Single

@Single(binds = [FcmTokenRepository::class])
class FcmTokenRepositoryImpl(
    private val fcmTokenRemoteDataSource: FcmTokenRemoteDataSource,
) : FcmTokenRepository {

    override suspend fun updateFcmToken(
        fcmToken: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        fcmTokenRemoteDataSource.updateFcmToken(fcmToken, onSuccess, onError)
    }
}
