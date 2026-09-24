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
 *   그대로 넘길 수 있다). **제출한 적이 없으면** null — 미인증과 같은 말이 아니다. AI 가 반려한
 *   칸은 미인증 처리인데도 사용자가 올린 사진이 그대로 남아 있다(계약 §4 필드 표).
 *   인증 헤더를 붙여 요청해야 하므로 외부에 그대로 노출할 수 있는 URL 이 아니다
 * @property verifiedAt **제출 확정 시각** (KST). 제출한 적이 없으면 null
 *   (계약 §4 Q&A 표 — 2026-09-22 에 `VERIFIED 가 아니면 null` 에서 이 문면으로 정정됐다).
 *
 *   '인증 확정' 이 아니다 — 서버가 제출 시점에만 쓰고 이후 판정도 탈퇴도 이 값을 건드리지 않아,
 *   반려된 칸에도 남아 있다. 그렇게 읽어야 두 곳이 성립한다: ① 앱의 사진 캐시 키가 이 값으로
 *   갈리므로(교체하면 URL 은 그대로인 채 이 값만 움직인다) '인증 확정' 으로 좁히면 반려된 칸의
 *   교체가 캐시에서 잡히지 않는다 ② 서버의 `photoDeleted` 판정식이 '이 값이 있으면 사진을 낸 적이
 *   있다' 를 근거로 선다
 * @property photoDeleted 사진이 영구 삭제됨(탈퇴). 제출했다는 사실은 상대의 기록이라 남지만
 *   사진은 되살아나지 않으므로, [photoUrl] 이 null 이어도 제출을 마친 상태(`VERIFIED`, 그리고
 *   AI 가 반려한 `FAILED`)와 구분된다(계약 §4)
 * @property aiReason AI 가 인증 사진을 판정하며 남긴 사유. 없으면 null.
 *
 *   **non-null 이면 그 인증은 AI 판별로 미인증 처리된 것이다**(API 계약 §4.1 보장). 그래서 앱은
 *   [status] 를 함께 보지 않는다 — 같이 보면 '반려란 무엇인가' 의 판정 사본이 앱에 생긴다.
 *   통과·검증 불가·호출 실패 건의 사유는 서버가 컬럼에만 두고 wire 에는 싣지 않는다
 */
data class PartyVerification(
    val userId: Long,
    val status: VerificationStatus,
    val photoUrl: String?,
    val verifiedAt: LocalDateTime?,
    val photoDeleted: Boolean,
    val aiReason: String?,
)
