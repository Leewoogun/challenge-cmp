package com.lwg.challenge.data.datasource.remote

import com.lwg.challenge.domain.model.verification.ChallengeVerifications
import com.lwg.challenge.domain.model.verification.VerificationSubmitOutcome
import kotlinx.coroutines.flow.Flow

interface VerificationRemoteDataSource {

    suspend fun submitVerification(challengeId: Long, jpegBytes: ByteArray): VerificationSubmitOutcome

    fun getVerifications(
        challengeId: Long,
        onError: (String) -> Unit,
    ): Flow<ChallengeVerifications>
}
