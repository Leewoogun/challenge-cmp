package com.lwg.challenge.remote.datasource

import com.lwg.challenge.data.datasource.remote.FcmTokenRemoteDataSource
import com.lwg.challenge.remote.api.UserApi
import com.lwg.challenge.remote.model.user.FcmTokenRequest
import com.lwg.challenge.remote.network.util.suspendOnFailureWithErrorHandling
import com.lwg.challenge.remote.network.util.suspendOnSuccess
import org.koin.core.annotation.Single

@Single(binds = [FcmTokenRemoteDataSource::class])
class FcmTokenRemoteDataSourceImpl(
    private val userApi: UserApi,
) : FcmTokenRemoteDataSource {

    override suspend fun updateFcmToken(
        fcmToken: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        userApi.updateFcmToken(FcmTokenRequest(fcmToken = fcmToken))
            .suspendOnSuccess { onSuccess() }
            .suspendOnFailureWithErrorHandling(onError)
    }
}
