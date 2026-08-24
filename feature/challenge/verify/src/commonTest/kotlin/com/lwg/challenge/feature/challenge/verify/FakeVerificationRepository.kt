package com.lwg.challenge.feature.challenge.verify

import com.lwg.challenge.domain.model.VerificationStatus
import com.lwg.challenge.domain.model.verification.ChallengeVerifications
import com.lwg.challenge.domain.model.verification.VerificationSubmitOutcome
import com.lwg.challenge.domain.model.verification.VerificationSubmitResult
import com.lwg.challenge.domain.repository.VerificationRepository
import kotlinx.coroutines.awaitCancellation
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

    /** 조회 성공 시 줄 현황. */
    var verifications: ChallengeVerifications? = null

    /** 조회 실패 메시지. null 이면 [verifications] 로 성공. */
    var getVerificationsError: String? = null

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

    override suspend fun getVerifications(
        challengeId: Long,
        onSuccess: (ChallengeVerifications) -> Unit,
        onError: (String) -> Unit,
    ) {
        getVerificationsCallCount++
        val error = getVerificationsError
        if (error != null) {
            onError(error)
            return
        }
        verifications?.let(onSuccess)
    }
}
