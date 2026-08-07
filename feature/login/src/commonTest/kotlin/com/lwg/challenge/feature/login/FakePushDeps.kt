package com.lwg.challenge.feature.login

import com.lwg.challenge.domain.repository.FcmTokenRepository
import com.lwg.challenge.push.FcmTokenProvider

class FakeFcmTokenProvider(var token: String? = "fcm-token-1") : FcmTokenProvider {

    override suspend fun getToken(): String? = token
}

class FakeFcmTokenRepository(
    private val errorMessage: String? = null,
) : FcmTokenRepository {

    var updateCallCount: Int = 0
        private set

    var lastFcmToken: String? = null
        private set

    override suspend fun updateFcmToken(
        fcmToken: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        updateCallCount += 1
        lastFcmToken = fcmToken
        if (errorMessage == null) onSuccess() else onError(errorMessage)
    }
}
