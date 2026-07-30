package com.lwg.challenge.feature.home

import com.lwg.challenge.domain.model.challenge.ChallengeCreateInput
import com.lwg.challenge.domain.model.challenge.ReceivedChallenge
import com.lwg.challenge.domain.repository.ChallengeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeChallengeRepository : ChallengeRepository {

    var challenges: List<ReceivedChallenge>? = null
    var error: String? = null

    var callCount: Int = 0
        private set

    var acceptError: String? = null
    var lastAcceptedId: Long? = null
        private set
    var lastAcceptedMission: String? = null
        private set
    var acceptCallCount: Int = 0
        private set

    var rejectError: String? = null
    var lastRejectedId: Long? = null
        private set
    var rejectCallCount: Int = 0
        private set

    // 실패 시 emit 하지 않는 것이 실제 구현 계약이다. 빈 목록을 흘리면
    // GetHomeDataUseCase 의 onEmpty 폴백이 우회돼 회귀 테스트가 무의미해진다.
    override fun getReceivedChallenges(
        onError: (String) -> Unit,
    ): Flow<List<ReceivedChallenge>> = flow {
        callCount += 1
        val current = challenges
        val currentError = error
        when {
            currentError != null -> onError(currentError)
            current != null -> emit(current)
        }
    }

    override suspend fun acceptChallenge(
        challengeId: Long,
        myMission: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        acceptCallCount += 1
        lastAcceptedId = challengeId
        lastAcceptedMission = myMission
        acceptError.let { if (it == null) onSuccess() else onError(it) }
    }

    override suspend fun rejectChallenge(
        challengeId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        rejectCallCount += 1
        lastRejectedId = challengeId
        rejectError.let { if (it == null) onSuccess() else onError(it) }
    }

    override suspend fun createChallenge(
        input: ChallengeCreateInput,
        onSuccess: (challengeId: Long) -> Unit,
        onError: (String) -> Unit,
    ) = Unit

    override suspend fun cancelChallenge(
        challengeId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) = Unit
}
