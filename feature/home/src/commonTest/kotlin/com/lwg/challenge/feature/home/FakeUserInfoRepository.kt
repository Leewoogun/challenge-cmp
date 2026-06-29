package com.lwg.challenge.feature.home

import com.lwg.challenge.domain.model.user.CacheStrategy
import com.lwg.challenge.domain.model.user.UserInfo
import com.lwg.challenge.domain.repository.UserInfoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

class FakeUserInfoRepository : UserInfoRepository {

    private val cache = MutableStateFlow<UserInfo?>(null)

    // 캐시 없을 때 getUserInfo가 fetch해서 저장/emit할 값
    var fetchResult: UserInfo? = null
    var error: String? = null

    var getUserInfoCallCount: Int = 0
        private set

    fun seedCache(userInfo: UserInfo?) {
        cache.value = userInfo
    }

    override fun getUserInfo(
        onError: (String) -> Unit,
        cacheStrategy: CacheStrategy,
    ): Flow<UserInfo> = flow {
        getUserInfoCallCount += 1
        val currentError = error
        if (currentError != null) {
            onError(currentError)
            return@flow
        }
        val cached = cache.value
        if (cacheStrategy == CacheStrategy.CACHE_FIRST && cached != null) {
            emit(cached)
        } else {
            val fetched = fetchResult
            if (fetched != null) {
                cache.value = fetched
                emit(fetched)
            }
        }
    }

    override suspend fun clearUserInfoCache() {
        cache.value = null
    }
}
