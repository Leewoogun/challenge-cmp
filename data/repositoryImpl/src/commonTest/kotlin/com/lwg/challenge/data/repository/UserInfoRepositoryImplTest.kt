package com.lwg.challenge.data.repository

import com.lwg.challenge.data.datasource.local.UserInfoLocalDataSource
import com.lwg.challenge.data.datasource.remote.UserInfoRemoteDataSource
import com.lwg.challenge.domain.model.user.CacheStrategy
import com.lwg.challenge.domain.model.user.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UserInfoRepositoryImplTest {

    private val remoteUserInfo = UserInfo(
        id = 12L,
        kakaoId = 99L,
        nickname = "서버유저",
        profileImageUrl = "https://img/me.png",
    )

    private val cachedUserInfo = UserInfo(
        id = 7L,
        kakaoId = 70L,
        nickname = "캐시유저",
        profileImageUrl = null,
    )

    @Test
    fun `CACHE_FIRST + 캐시 있음 - 캐시값 emit, 원격 호출 0건`() = runTest {
        val remote = FakeUserInfoRemoteDataSource(result = remoteUserInfo)
        val local = FakeUserInfoLocalDataSource(initial = cachedUserInfo)
        val repository = UserInfoRepositoryImpl(remote, local)

        val emitted = repository.getUserInfo(onError = {}, cacheStrategy = CacheStrategy.CACHE_FIRST).toList()

        assertEquals(listOf(cachedUserInfo), emitted)
        assertEquals(0, remote.callCount)
        assertEquals(0, local.saveCallCount)
    }

    @Test
    fun `CACHE_FIRST + 캐시 없음 - 원격 1회 호출, save 1회, 원격값 emit`() = runTest {
        val remote = FakeUserInfoRemoteDataSource(result = remoteUserInfo)
        val local = FakeUserInfoLocalDataSource(initial = null)
        val repository = UserInfoRepositoryImpl(remote, local)

        val emitted = repository.getUserInfo(onError = {}, cacheStrategy = CacheStrategy.CACHE_FIRST).toList()

        assertEquals(listOf(remoteUserInfo), emitted)
        assertEquals(1, remote.callCount)
        assertEquals(1, local.saveCallCount)
        assertEquals(remoteUserInfo, local.getUserInfo())
    }

    @Test
    fun `NETWORK_ONLY - 캐시 있어도 원격 1회 호출, save 1회, 원격값 emit`() = runTest {
        val remote = FakeUserInfoRemoteDataSource(result = remoteUserInfo)
        val local = FakeUserInfoLocalDataSource(initial = cachedUserInfo)
        val repository = UserInfoRepositoryImpl(remote, local)

        val emitted = repository.getUserInfo(onError = {}, cacheStrategy = CacheStrategy.NETWORK_ONLY).toList()

        assertEquals(listOf(remoteUserInfo), emitted)
        assertEquals(1, remote.callCount)
        assertEquals(1, local.saveCallCount)
    }

    @Test
    fun `원격 실패 - onError 1회 호출, emit 0건, save 0건`() = runTest {
        val remote = FakeUserInfoRemoteDataSource(result = null, errorMessage = "Unauthorized")
        val local = FakeUserInfoLocalDataSource(initial = null)
        val repository = UserInfoRepositoryImpl(remote, local)

        val errors = mutableListOf<String>()
        val emitted = repository.getUserInfo(
            onError = { errors.add(it) },
            cacheStrategy = CacheStrategy.NETWORK_ONLY,
        ).toList()

        assertEquals(emptyList(), emitted)
        assertEquals(listOf("Unauthorized"), errors)
        assertEquals(0, local.saveCallCount)
    }

    @Test
    fun `clearUserInfoCache - local clear 호출, 캐시 null`() = runTest {
        val remote = FakeUserInfoRemoteDataSource(result = remoteUserInfo)
        val local = FakeUserInfoLocalDataSource(initial = cachedUserInfo)
        val repository = UserInfoRepositoryImpl(remote, local)

        repository.clearUserInfoCache()

        assertEquals(1, local.clearCallCount)
        assertNull(local.getUserInfo())
    }
}

/** [result] 가 null 이면 실패로 간주해 [errorMessage] 로 onError 만 부르고 emit 없이 끝난다 (실제 구현과 동일). */
private class FakeUserInfoRemoteDataSource(
    private val result: UserInfo?,
    private val errorMessage: String = "",
) : UserInfoRemoteDataSource {

    var callCount = 0
        private set

    override fun getUserInfo(onError: (String) -> Unit): Flow<UserInfo> = flow {
        callCount++
        if (result == null) onError(errorMessage) else emit(result)
    }
}

private class FakeUserInfoLocalDataSource(
    initial: UserInfo?,
) : UserInfoLocalDataSource {
    private val state = MutableStateFlow(initial)

    var saveCallCount = 0
        private set
    var clearCallCount = 0
        private set

    override suspend fun getUserInfo(): UserInfo? = state.first()

    override suspend fun saveUserInfo(userInfo: UserInfo) {
        saveCallCount++
        state.value = userInfo
    }

    override suspend fun clear() {
        clearCallCount++
        state.value = null
    }
}
