package com.lwg.challenge.data.datasource.remote

import com.lwg.challenge.domain.model.verification.ChallengeVerifications
import com.lwg.challenge.domain.model.verification.VerificationSubmitOutcome

interface VerificationRemoteDataSource {

    suspend fun submitVerification(challengeId: Long, jpegBytes: ByteArray): VerificationSubmitOutcome

    suspend fun getVerifications(
        challengeId: Long,
        onSuccess: (ChallengeVerifications) -> Unit,
        onError: (String) -> Unit,
    )
}
