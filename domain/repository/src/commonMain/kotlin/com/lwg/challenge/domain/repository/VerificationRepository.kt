package com.lwg.challenge.domain.repository

import com.lwg.challenge.domain.model.verification.ChallengeVerifications
import com.lwg.challenge.domain.model.verification.VerificationSubmitOutcome

/**
 * 챌린지 인증(사진 제출/조회) Repository.
 *
 * 제출은 [submitVerification] 한 번의 호출로 끝난다 — 발급/업로드 선행 단계나
 * photoKey 개념은 앱에 존재하지 않는다.
 */
interface VerificationRepository {

    /**
     * 콜백이 아닌 반환값 시그니처다 — [VerificationSubmitOutcome.Rejected](서버가 판정해 거부)와
     * [VerificationSubmitOutcome.Unreachable](서버 반영 여부 불명)은 앱의 조치가 정반대라
     * `onError(String)` 한 채널로는 구분할 수 없다.
     */
    suspend fun submitVerification(challengeId: Long, jpegBytes: ByteArray): VerificationSubmitOutcome

    /**
     * 화면 진입 시 1회 조회만 있어 관찰 대상이 아니다. Flow 가 아니라 suspend + 콜백으로 둔다.
     */
    suspend fun getVerifications(
        challengeId: Long,
        onSuccess: (ChallengeVerifications) -> Unit,
        onError: (String) -> Unit,
    )
}
