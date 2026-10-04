package com.lwg.challenge.domain.usecase

import com.lwg.challenge.domain.model.HomeData
import com.lwg.challenge.domain.model.user.CacheStrategy
import com.lwg.challenge.domain.model.user.UserInfo
import com.lwg.challenge.domain.repository.ActiveChallengeRepository
import com.lwg.challenge.domain.repository.ChallengeRepository
import com.lwg.challenge.domain.repository.NotificationRepository
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
    private val notificationRepository: NotificationRepository,
) {
    // combine 오버로드는 5개까지다. 소스가 하나 더 필요해지면 다른 형태로 바꿔야 한다.
    operator fun invoke(onError: (String) -> Unit): Flow<HomeData> = combine(
        recordRepository.getMyRecord(onError),
        activeChallengeRepository.getActiveChallenges(onError),
        userInfoRepository.getUserInfo(onError, CacheStrategy.CACHE_FIRST)
            .map<UserInfo, UserInfo?> { it }
            .onEmpty { emit(null) },
        challengeRepository.getReceivedChallenges(onError)
            .onEmpty { emit(emptyList()) },
        notificationRepository.getUnreadCount(onError)
            .onEmpty { emit(0) },
    ) { record, challenges, userInfo, receivedChallenges, unreadCount ->
        HomeData(
            record = record,
            challenges = challenges,
            userInfo = userInfo,
            receivedChallenges = receivedChallenges,
            unreadCount = unreadCount,
        )
    }
}
