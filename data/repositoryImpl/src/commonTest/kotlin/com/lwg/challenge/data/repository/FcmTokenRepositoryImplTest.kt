package com.lwg.challenge.data.repository

import com.lwg.challenge.data.datasource.remote.FcmTokenRemoteDataSource
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FcmTokenRepositoryImplTest {

    @Test
    fun `성공 - onSuccess 1회 호출, 원격 1회 호출, 토큰 그대로 전달`() = runTest {
        val remote = FakeFcmTokenRemoteDataSource(succeeds = true)
        val repository = FcmTokenRepositoryImpl(remote)

        var successCount = 0
        val errors = mutableListOf<String>()
        repository.updateFcmToken(
            fcmToken = "fcm-token-abc",
            onSuccess = { successCount++ },
            onError = { errors.add(it) },
        )

        assertEquals(1, successCount)
        assertEquals(emptyList(), errors)
        assertEquals(1, remote.callCount)
        assertEquals(listOf("fcm-token-abc"), remote.receivedTokens)
    }

    @Test
    fun `실패 - onError 1회 호출, onSuccess 0회 호출`() = runTest {
        val remote = FakeFcmTokenRemoteDataSource(succeeds = false, errorMessage = "Unauthorized")
        val repository = FcmTokenRepositoryImpl(remote)

        var successCount = 0
        val errors = mutableListOf<String>()
        repository.updateFcmToken(
            fcmToken = "fcm-token-abc",
            onSuccess = { successCount++ },
            onError = { errors.add(it) },
        )

        assertEquals(0, successCount)
        assertEquals(listOf("Unauthorized"), errors)
        assertEquals(1, remote.callCount)
    }

    /** 계약 §1 - 로그인 직후와 onNewToken 양쪽에서 부르므로 같은 토큰 중복 호출이 정상 경로다. */
    @Test
    fun `같은 토큰 2회 호출 - 매번 onSuccess, 원격 2회 호출`() = runTest {
        val remote = FakeFcmTokenRemoteDataSource(succeeds = true)
        val repository = FcmTokenRepositoryImpl(remote)

        var successCount = 0
        repeat(2) {
            repository.updateFcmToken(
                fcmToken = "same-token",
                onSuccess = { successCount++ },
                onError = {},
            )
        }

        assertEquals(2, successCount)
        assertEquals(2, remote.callCount)
        assertEquals(listOf("same-token", "same-token"), remote.receivedTokens)
    }
}

/** [succeeds] 가 false 면 [errorMessage] 로 onError 만 부른다 (실제 구현과 동일). */
private class FakeFcmTokenRemoteDataSource(
    private val succeeds: Boolean,
    private val errorMessage: String = "",
) : FcmTokenRemoteDataSource {

    var callCount = 0
        private set
    val receivedTokens = mutableListOf<String>()

    override suspend fun updateFcmToken(
        fcmToken: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        callCount++
        receivedTokens.add(fcmToken)
        if (succeeds) onSuccess() else onError(errorMessage)
    }
}
