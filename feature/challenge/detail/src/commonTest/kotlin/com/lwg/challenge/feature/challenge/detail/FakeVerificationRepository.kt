package com.lwg.challenge.feature.challenge.detail

import com.lwg.challenge.domain.model.verification.ChallengeVerifications
import com.lwg.challenge.domain.model.verification.VerificationSubmitOutcome
import com.lwg.challenge.domain.repository.VerificationRepository

/**
 * 테스트용 [VerificationRepository] 페이크.
 *
 * 상세 화면은 조회만 쓴다 — [submitVerification] 은 인터페이스를 채우기 위한 구현이라 호출되면
 * 테스트가 잘못된 것이므로 즉시 깨뜨린다.
 */
class FakeVerificationRepository : VerificationRepository {

    /** 조회 성공 시 줄 현황. */
    var verifications: ChallengeVerifications? = null

    /** 조회 실패 메시지. null 이면 [verifications] 로 성공. */
    var getVerificationsError: String? = null

    var getVerificationsCallCount: Int = 0

    override suspend fun submitVerification(
        challengeId: Long,
        jpegBytes: ByteArray,
    ): VerificationSubmitOutcome = error("상세 화면은 인증을 제출하지 않는다")

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
