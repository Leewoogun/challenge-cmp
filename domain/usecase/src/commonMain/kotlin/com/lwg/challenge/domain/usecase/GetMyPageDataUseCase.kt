package com.lwg.challenge.domain.usecase

import com.lwg.challenge.domain.model.MyPageData
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
    // combine 은 모든 소스가 1회 이상 emit 해야 결과를 낸다. 어느 한쪽이 실패해도 화면이 Loading 에
    // 갇히지 않도록 양쪽 다 null 을 흘린다.
    // 전적에도 이 처리를 하는 이유: 전적을 못 받아도 카드는 그린다 — 닉네임·아바타는 출처가 다른
    // 성공한 데이터이고, 승/패/무 행만 빼고 캡션으로 실패를 말한다 (design.md §1.7.3).
    // 폴백을 UserRecord.Empty 가 아니라 null 로 두는 것이 그 판정의 핵심이다.
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
