package com.lwg.challenge.domain.usecase

import com.lwg.challenge.domain.model.UserRecord
import com.lwg.challenge.domain.model.user.CacheStrategy
import com.lwg.challenge.domain.model.user.UserInfo
import com.lwg.challenge.domain.repository.RecordRepository
import com.lwg.challenge.domain.repository.UserInfoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEmpty

class GetMyPageDataUseCase(
    private val recordRepository: RecordRepository,
    private val userInfoRepository: UserInfoRepository,
) {
    // 한쪽이 실패해도 Loading 에 갇히지 않게 null 을 흘린다. 전적은 Empty 가 아니라 null 이어야 실패로 읽힌다.
    operator fun invoke(onError: (String) -> Unit): Flow<MyPageData> = combine(
        recordRepository.getMyRecord(onError)
            .map<UserRecord, UserRecord?> { it }
            .onEmpty { emit(null) },
        userInfoRepository.getUserInfo(onError, CacheStrategy.CACHE_FIRST)
            .map<UserInfo, UserInfo?> { it }
            .onEmpty { emit(null) },
    ) { record, userInfo ->
        MyPageData(
            record = record,
            userInfo = userInfo,
        )
    }
}

/**
 * 마이페이지 화면에 필요한 데이터 묶음 — 전적 + 내 정보.
 */
data class MyPageData(
    val record: UserRecord?,
    val userInfo: UserInfo? = null,
)
