package com.lwg.challenge.feature.friends

import com.lwg.challenge.domain.model.user.CacheStrategy
import com.lwg.challenge.domain.model.user.UserInfo
import com.lwg.challenge.domain.repository.UserInfoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

/**
 * feature:friends 테스트용 [UserInfoRepository] 페이크 (feature:home 것과 별개 모듈).
 *
 * [getUserInfo] 는 주입 가능한 [cacheFlow] 스냅샷을 방출한다. [seed] 로 캐시 값 설정.
 */
class FakeUserInfoRepository : UserInfoRepository {

    val cacheFlow = MutableStateFlow<UserInfo?>(null)

    fun seed(userInfo: UserInfo?) {
        cacheFlow.value = userInfo
    }

    override fun getUserInfo(
        onError: (String) -> Unit,
        cacheStrategy: CacheStrategy,
    ): Flow<UserInfo> = flow {
        cacheFlow.value?.let { emit(it) }
    }

    override suspend fun clearUserInfoCache() {
        cacheFlow.value = null
    }
}
