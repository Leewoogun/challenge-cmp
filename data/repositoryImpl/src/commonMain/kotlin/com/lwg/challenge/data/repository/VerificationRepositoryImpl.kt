package com.lwg.challenge.data.repository

import com.lwg.challenge.data.datasource.remote.VerificationRemoteDataSource
import com.lwg.challenge.domain.model.verification.ChallengeVerifications
import com.lwg.challenge.domain.model.verification.VerificationSubmitOutcome
import com.lwg.challenge.domain.repository.VerificationRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single(binds = [VerificationRepository::class])
class VerificationRepositoryImpl(
    private val verificationRemoteDataSource: VerificationRemoteDataSource,
) : VerificationRepository {

    override suspend fun submitVerification(
        challengeId: Long,
        jpegBytes: ByteArray,
    ): VerificationSubmitOutcome = verificationRemoteDataSource.submitVerification(challengeId, jpegBytes)

    override fun getVerifications(
        challengeId: Long,
        onError: (String) -> Unit,
    ): Flow<ChallengeVerifications> = verificationRemoteDataSource.getVerifications(challengeId, onError)
}
