package com.lwg.challenge.data.datasource.remote

interface FcmTokenRemoteDataSource {

    suspend fun updateFcmToken(
        fcmToken: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    )
}
