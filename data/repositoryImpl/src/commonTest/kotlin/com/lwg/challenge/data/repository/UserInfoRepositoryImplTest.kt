package com.lwg.challenge.data.repository

import com.lwg.challenge.domain.model.user.CacheStrategy
import com.lwg.challenge.domain.model.user.UserInfo
import com.lwg.challenge.local.datastore.datasource.UserInfoLocalDataSource
import com.lwg.challenge.remote.api.UserApi
import com.lwg.challenge.remote.model.user.UserInfoData
import com.lwg.challenge.remote.model.user.UserInfoResponse
import com.lwg.challenge.remote.network.util.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UserInfoRepositoryImplTest {

    private val apiUserInfo = UserInfo(
        id = 12L,
        kakaoId = 99L,
        nickname = "서버유저",
        profileImageUrl = "https://img/me.png",
    )

    private val apiResponse = ApiResult.Success(
        UserInfoResponse(
            data = UserInfoData(
                id = 12L,
                kakaoId = 99L,
                nickname = "서버유저",
                profileImageUrl = "https://img/me.png",
            ),
        ),
    )

    private val cachedUserInfo = UserInfo(
        id = 7L,
        kakaoId = 70L,
        nickname = "캐시유저",
        profileImageUrl = null,
    )

    @Test
    fun `CACHE_FIRST + 캐시 있음 - 캐시값 emit, API 호출 0건`() = runTest {
        val api = FakeUserApi(result = apiResponse)
        val local = FakeUserInfoLocalDataSource(initial = cachedUserInfo)
        val repository = UserInfoRepositoryImpl(api, local)

        val emitted = repository.getUserInfo(onError = {}, cacheStrategy = CacheStrategy.CACHE_FIRST).toList()

        assertEquals(listOf(cachedUserInfo), emitted)
        assertEquals(0, api.getMeCallCount)
        assertEquals(0, local.saveCallCount)
    }

    @Test
    fun `CACHE_FIRST + 캐시 없음 - API 1회 호출, save 1회, API 매핑값 emit`() = runTest {
        val api = FakeUserApi(result = apiResponse)
        val local = FakeUserInfoLocalDataSource(initial = null)
        val repository = UserInfoRepositoryImpl(api, local)

        val emitted = repository.getUserInfo(onError = {}, cacheStrategy = CacheStrategy.CACHE_FIRST).toList()

        assertEquals(listOf(apiUserInfo), emitted)
        assertEquals(1, api.getMeCallCount)
        assertEquals(1, local.saveCallCount)
        assertEquals(apiUserInfo, local.getUserInfo())
    }

    @Test
    fun `NETWORK_ONLY - 캐시 있어도 API 1회 호출, save 1회, API값 emit`() = runTest {
        val api = FakeUserApi(result = apiResponse)
        val local = FakeUserInfoLocalDataSource(initial = cachedUserInfo)
        val repository = UserInfoRepositoryImpl(api, local)

        val emitted = repository.getUserInfo(onError = {}, cacheStrategy = CacheStrategy.NETWORK_ONLY).toList()

        assertEquals(listOf(apiUserInfo), emitted)
        assertEquals(1, api.getMeCallCount)
        assertEquals(1, local.saveCallCount)
    }

    @Test
    fun `API 실패 - onError 1회 호출, emit 0건, save 0건`() = runTest {
        val api = FakeUserApi(
            result = ApiResult.Failure.HttpError(code = 401, message = "Unauthorized", body = ""),
        )
        val local = FakeUserInfoLocalDataSource(initial = null)
        val repository = UserInfoRepositoryImpl(api, local)

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
        val api = FakeUserApi(result = apiResponse)
        val local = FakeUserInfoLocalDataSource(initial = cachedUserInfo)
        val repository = UserInfoRepositoryImpl(api, local)

        repository.clearUserInfoCache()

        assertEquals(1, local.clearCallCount)
        assertNull(local.getUserInfo())
    }
}

private class FakeUserApi(
    private val result: ApiResult<UserInfoResponse>,
) : UserApi {
    var getMeCallCount = 0
        private set

    override suspend fun getMe(): ApiResult<UserInfoResponse> {
        getMeCallCount++
        return result
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
