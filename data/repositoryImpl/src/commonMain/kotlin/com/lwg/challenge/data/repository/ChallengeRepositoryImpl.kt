package com.lwg.challenge.data.repository

import com.lwg.challenge.domain.model.challenge.ChallengeCreateInput
import com.lwg.challenge.domain.model.challenge.ReceivedChallenge
import com.lwg.challenge.domain.repository.ChallengeRepository
import com.lwg.challenge.remote.api.ChallengeApi
import com.lwg.challenge.remote.mapper.toBody
import com.lwg.challenge.remote.mapper.toReceivedChallenges
import com.lwg.challenge.remote.model.challenge.AcceptChallengeBody
import com.lwg.challenge.remote.network.util.suspendOnFailureWithErrorHandling
import com.lwg.challenge.remote.network.util.suspendOnSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single

@Single(binds = [ChallengeRepository::class])
class ChallengeRepositoryImpl(
    private val challengeApi: ChallengeApi,
) : ChallengeRepository {

    override fun getReceivedChallenges(
        onError: (String) -> Unit,
    ): Flow<List<ReceivedChallenge>> = flow {
        challengeApi.getReceivedChallenges()
            .suspendOnSuccess { this@flow.emit(response.toReceivedChallenges()) }
            .suspendOnFailureWithErrorHandling(onError)
    }

    override suspend fun createChallenge(
        input: ChallengeCreateInput,
        onSuccess: (Long) -> Unit,
        onError: (String) -> Unit,
    ) {
        challengeApi.createChallenge(input.toBody())
            .suspendOnSuccess { onSuccess(response.data.challengeId) }
            .suspendOnFailureWithErrorHandling(onError)
    }

    override suspend fun acceptChallenge(
        challengeId: Long,
        myMission: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        challengeApi.acceptChallenge(challengeId, AcceptChallengeBody(myMission))
            .suspendOnSuccess { onSuccess() }
            .suspendOnFailureWithErrorHandling(onError)
    }

    override suspend fun rejectChallenge(
        challengeId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        challengeApi.rejectChallenge(challengeId)
            .suspendOnSuccess { onSuccess() }
            .suspendOnFailureWithErrorHandling(onError)
    }

    override suspend fun cancelChallenge(
        challengeId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        challengeApi.cancelChallenge(challengeId)
            .suspendOnSuccess { onSuccess() }
            .suspendOnFailureWithErrorHandling(onError)
    }
}
