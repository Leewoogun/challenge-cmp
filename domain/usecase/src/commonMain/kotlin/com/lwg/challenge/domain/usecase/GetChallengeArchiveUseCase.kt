package com.lwg.challenge.domain.usecase

import com.lwg.challenge.domain.model.challenge.ChallengeArchive
import com.lwg.challenge.domain.repository.ChallengeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.YearMonth

class GetChallengeArchiveUseCase(
    private val challengeRepository: ChallengeRepository,
) {
    operator fun invoke(month: YearMonth, onError: (String) -> Unit): Flow<ChallengeArchive> =
        challengeRepository.getChallengeArchive(month, onError)
}
