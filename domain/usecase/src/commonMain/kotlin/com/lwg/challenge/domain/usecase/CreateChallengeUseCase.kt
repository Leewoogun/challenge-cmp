package com.lwg.challenge.domain.usecase

import com.lwg.challenge.domain.model.challenge.ChallengeCreateInput
import com.lwg.challenge.domain.repository.ChallengeRepository

class CreateChallengeUseCase(
    private val challengeRepository: ChallengeRepository,
) {

    suspend operator fun invoke(
        input: ChallengeCreateInput,
        onSuccess: (Long) -> Unit,
        onError: (String) -> Unit,
    ) = challengeRepository.createChallenge(
        input = input,
        onSuccess = onSuccess,
        onError = onError,
    )
}
