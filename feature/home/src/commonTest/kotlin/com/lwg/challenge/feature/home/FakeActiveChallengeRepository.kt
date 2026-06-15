package com.lwg.challenge.feature.home

import com.lwg.challenge.domain.model.ActiveChallenge
import com.lwg.challenge.domain.repository.ActiveChallengeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeActiveChallengeRepository : ActiveChallengeRepository {

    var challenges: List<ActiveChallenge>? = null
    var error: String? = null

    var callCount: Int = 0
        private set

    override fun getActiveChallenges(onError: (String) -> Unit): Flow<List<ActiveChallenge>> = flow {
        callCount += 1
        val current = challenges
        val currentError = error
        when {
            current != null -> emit(current)
            currentError != null -> onError(currentError)
        }
    }
}
