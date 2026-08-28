package com.lwg.challenge.domain.model.verification

import com.lwg.challenge.domain.model.VerificationStatus
import kotlinx.datetime.LocalDateTime

/**
 * 챌린지 양측 인증 현황.
 *
 * `challenger` / `opponent` 는 역할 그대로다 — '나 / 상대' 관점 변환은 하지 않는다.
 */
data class ChallengeVerifications(
    val challengeId: Long,
    val challenger: PartyVerification,
    val opponent: PartyVerification,
) {

    /** 역할 그대로 오는 응답에서 특정 사용자의 칸을 집는 조회다(관점 변환이 아니다). 어느 쪽도 아니면 null. */
    fun partyOf(userId: Long): PartyVerification? = when (userId) {
        challenger.userId -> challenger
        opponent.userId -> opponent
        else -> null
    }
}

/**
 * 당사자 한 명의 인증 현황.
 *
 * @property photoUrl 사진 조회 엔드포인트의 절대 URL (base 가 조립된 상태라 이미지 로더에
 *   그대로 넘길 수 있다). 미인증이면 null.
 *   인증 헤더를 붙여 요청해야 하므로 외부에 그대로 노출할 수 있는 URL 이 아니다
 * @property verifiedAt 인증 확정 시각 (KST). 미인증이면 null
 * @property photoDeleted 사진이 영구 삭제됨(탈퇴). 인증했다는 사실은 상대의 기록이라 남지만
 *   사진은 되살아나지 않으므로, [photoUrl] 이 null 이어도 `VERIFIED` 인 상태와 구분된다
 */
data class PartyVerification(
    val userId: Long,
    val status: VerificationStatus,
    val photoUrl: String?,
    val verifiedAt: LocalDateTime?,
    val photoDeleted: Boolean,
)
