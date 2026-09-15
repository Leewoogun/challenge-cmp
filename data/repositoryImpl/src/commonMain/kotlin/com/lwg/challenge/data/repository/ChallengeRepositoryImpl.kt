package com.lwg.challenge.data.repository

import com.lwg.challenge.data.datasource.remote.ChallengeRemoteDataSource
import com.lwg.challenge.domain.model.challenge.ChallengeArchive
import com.lwg.challenge.domain.model.challenge.ChallengeCreateInput
import com.lwg.challenge.domain.model.challenge.ChallengeDetail
import com.lwg.challenge.domain.model.challenge.ReceivedChallenge
import com.lwg.challenge.domain.model.signature.Signature
import com.lwg.challenge.domain.repository.ChallengeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.YearMonth
import org.koin.core.annotation.Single

@Single(binds = [ChallengeRepository::class])
class ChallengeRepositoryImpl(
    private val challengeRemoteDataSource: ChallengeRemoteDataSource,
) : ChallengeRepository {

    override fun getReceivedChallenges(
        onError: (String) -> Unit,
    ): Flow<List<ReceivedChallenge>> = challengeRemoteDataSource.getReceivedChallenges(onError)

    override fun getChallengeArchive(
        month: YearMonth?,
        onError: (String) -> Unit,
    ): Flow<ChallengeArchive> = challengeRemoteDataSource.getChallengeArchive(month, onError)

    override suspend fun createChallenge(
        input: ChallengeCreateInput,
        onSuccess: (Long) -> Unit,
        onError: (String) -> Unit,
    ) {
        challengeRemoteDataSource.createChallenge(input, onSuccess, onError)
    }

    override suspend fun getChallengeDetail(
        challengeId: Long,
        onSuccess: (ChallengeDetail) -> Unit,
        onError: (String) -> Unit,
    ) {
        challengeRemoteDataSource.getChallengeDetail(challengeId, onSuccess, onError)
    }

    override suspend fun acceptChallenge(
        challengeId: Long,
        myMission: String,
        signature: Signature,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        challengeRemoteDataSource.acceptChallenge(challengeId, myMission, signature, onSuccess, onError)
    }

    override suspend fun rejectChallenge(
        challengeId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        challengeRemoteDataSource.rejectChallenge(challengeId, onSuccess, onError)
    }

    override suspend fun forfeitChallenge(
        challengeId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        challengeRemoteDataSource.forfeitChallenge(challengeId, onSuccess, onError)
    }

    override suspend fun cancelChallenge(
        challengeId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        challengeRemoteDataSource.cancelChallenge(challengeId, onSuccess, onError)
    }
}
