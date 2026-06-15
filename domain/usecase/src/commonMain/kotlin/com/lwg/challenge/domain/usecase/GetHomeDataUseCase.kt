package com.lwg.challenge.domain.usecase

import com.lwg.challenge.domain.model.HomeData
import com.lwg.challenge.domain.repository.ActiveChallengeRepository
import com.lwg.challenge.domain.repository.RecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetHomeDataUseCase(
    private val recordRepository: RecordRepository,
    private val activeChallengeRepository: ActiveChallengeRepository,
) {
    operator fun invoke(onError: (String) -> Unit): Flow<HomeData> = combine(
        recordRepository.getMyRecord(onError),
        activeChallengeRepository.getActiveChallenges(onError),
    ) { record, challenges ->
        HomeData(record = record, challenges = challenges)
    }
}
