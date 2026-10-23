package com.lwg.challenge.feature.challenge.verify

import com.lwg.challenge.domain.model.VerificationStatus
import com.lwg.challenge.domain.model.verification.ChallengeVerifications
import com.lwg.challenge.domain.model.verification.VerificationSubmitOutcome
import com.lwg.challenge.domain.model.verification.VerificationSubmitResult
import com.lwg.challenge.domain.repository.VerificationRepository
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDateTime

/** 테스트용 [VerificationRepository] 페이크. */
class FakeVerificationRepository : VerificationRepository {

    /** 제출 결과. 테스트가 케이스별로 주입한다. */
    var submitOutcome: VerificationSubmitOutcome =
        VerificationSubmitOutcome.Success(
            VerificationSubmitResult(
                challengeId = 0L,
                status = VerificationStatus.VERIFIED,
                verifiedAt = LocalDateTime.parse("2026-01-01T00:05:00"),
            ),
        )

    /** true 면 호출만 기록하고 영원히 매달린다 — in-flight 상태를 붙잡아 두기 위한 스위치. */
    var submitPending: Boolean = false

    val submittedChallengeIds = mutableListOf<Long>()
    val submittedJpegBytes = mutableListOf<ByteArray>()

    val submitCallCount: Int get() = submittedChallengeIds.size

    /** 제출 경로가 현황 조회를 부르지 않는다는 것만 확인하면 되므로 호출 수만 센다. */
    var getVerificationsCallCount: Int = 0

    override suspend fun submitVerification(
        challengeId: Long,
        jpegBytes: ByteArray,
    ): VerificationSubmitOutcome {
        submittedChallengeIds += challengeId
        submittedJpegBytes += jpegBytes
        if (submitPending) awaitCancellation()
        return submitOutcome
    }

    override fun getVerifications(
        challengeId: Long,
        onError: (String) -> Unit,
    ): Flow<ChallengeVerifications> = flow {
        getVerificationsCallCount++
    }
}
