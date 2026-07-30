package com.lwg.challenge.domain.usecase

import com.lwg.challenge.domain.model.HomeData
import com.lwg.challenge.domain.model.user.CacheStrategy
import com.lwg.challenge.domain.model.user.UserInfo
import com.lwg.challenge.domain.repository.ActiveChallengeRepository
import com.lwg.challenge.domain.repository.ChallengeRepository
import com.lwg.challenge.domain.repository.RecordRepository
import com.lwg.challenge.domain.repository.UserInfoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEmpty

class GetHomeDataUseCase(
    private val recordRepository: RecordRepository,
    private val activeChallengeRepository: ActiveChallengeRepository,
    private val userInfoRepository: UserInfoRepository,
    private val challengeRepository: ChallengeRepository,
) {
    operator fun invoke(onError: (String) -> Unit): Flow<HomeData> = combine(
        recordRepository.getMyRecord(onError),
        activeChallengeRepository.getActiveChallenges(onError),
        userInfoRepository.getUserInfo(onError, CacheStrategy.CACHE_FIRST)
            .map<UserInfo, UserInfo?> { it }
            .onEmpty { emit(null) },
        challengeRepository.getReceivedChallenges(onError)
            // combine 은 모든 소스가 1회 이상 emit 해야 결과를 낸다. 받은 도전장 조회 실패가
            // 홈 전체를 Loading 에 가두지 않도록 빈 목록을 흘린다 (섹션만 미노출된다).
            // 전적 / 진행 중 챌린지 / 유저 정보는 홈의 필수 데이터라 의도적으로 이 처리를 하지 않는다.
            .onEmpty { emit(emptyList()) },
    ) { record, challenges, userInfo, receivedChallenges ->
        HomeData(
            record = record,
            challenges = challenges,
            userInfo = userInfo,
            receivedChallenges = receivedChallenges,
        )
    }
}
