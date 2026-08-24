package com.lwg.challenge.feature.challenge.verify

import com.lwg.challenge.domain.model.user.CacheStrategy
import com.lwg.challenge.domain.model.user.UserInfo
import com.lwg.challenge.domain.repository.UserInfoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * 테스트용 [UserInfoRepository] 페이크.
 *
 * 실패는 실제 구현과 같은 모양으로 흉내 낸다 — [onError] 만 부르고 **아무것도 emit 하지 않는다.**
 * userId 미상 fallback 이 걸리는 경로가 바로 이 "값 없이 끝나는 Flow" 다.
 */
class FakeUserInfoRepository : UserInfoRepository {

    var userInfo: UserInfo? = null

    /** 조회 실패 메시지. null 이면 [userInfo] 로 성공. */
    var error: String? = null

    var callCount: Int = 0

    override fun getUserInfo(
        onError: (String) -> Unit,
        cacheStrategy: CacheStrategy,
    ): Flow<UserInfo> = flow {
        callCount++
        val currentError = error
        if (currentError != null) {
            onError(currentError)
            return@flow
        }
        userInfo?.let { emit(it) }
    }

    override suspend fun clearUserInfoCache() = Unit
}
