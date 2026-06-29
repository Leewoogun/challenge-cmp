package com.lwg.challenge.domain.usecase

import com.lwg.challenge.domain.model.HomeData
import com.lwg.challenge.domain.model.user.CacheStrategy
import com.lwg.challenge.domain.model.user.UserInfo
import com.lwg.challenge.domain.repository.ActiveChallengeRepository
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
) {
    operator fun invoke(onError: (String) -> Unit): Flow<HomeData> = combine(
        recordRepository.getMyRecord(onError),
        activeChallengeRepository.getActiveChallenges(onError),
        userInfoRepository.getUserInfo(onError, CacheStrategy.CACHE_FIRST)
            .map<UserInfo, UserInfo?> { it }
            .onEmpty { emit(null) },
    ) { record, challenges, userInfo ->
        HomeData(record = record, challenges = challenges, userInfo = userInfo)
    }
}
