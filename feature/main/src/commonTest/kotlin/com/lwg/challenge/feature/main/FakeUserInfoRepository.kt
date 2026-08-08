package com.lwg.challenge.feature.main

import com.lwg.challenge.domain.model.user.CacheStrategy
import com.lwg.challenge.domain.model.user.UserInfo
import com.lwg.challenge.domain.repository.UserInfoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/** LogoutUseCase 조립용 최소 Fake. */
class FakeUserInfoRepository : UserInfoRepository {

    var clearUserInfoCacheCalled = false
        private set

    override fun getUserInfo(
        onError: (String) -> Unit,
        cacheStrategy: CacheStrategy,
    ): Flow<UserInfo> = emptyFlow()

    override suspend fun clearUserInfoCache() {
        clearUserInfoCacheCalled = true
    }
}
