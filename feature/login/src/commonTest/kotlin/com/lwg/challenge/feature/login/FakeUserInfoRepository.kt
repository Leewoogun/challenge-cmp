package com.lwg.challenge.feature.login

import com.lwg.challenge.domain.model.user.CacheStrategy
import com.lwg.challenge.domain.model.user.UserInfo
import com.lwg.challenge.domain.repository.UserInfoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * LogoutUseCase 조립용 최소 Fake. 로그인 테스트에서는 [clearUserInfoCache] 호출 여부만 본다.
 */
class FakeUserInfoRepository : UserInfoRepository {

    var clearUserInfoCacheCallCount = 0
        private set

    override fun getUserInfo(
        onError: (String) -> Unit,
        cacheStrategy: CacheStrategy,
    ): Flow<UserInfo> = emptyFlow()

    override suspend fun clearUserInfoCache() {
        clearUserInfoCacheCallCount++
    }
}
