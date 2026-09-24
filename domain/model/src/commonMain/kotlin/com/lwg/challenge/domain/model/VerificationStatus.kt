package com.lwg.challenge.domain.model

/**
 * 챌린지 인증 상태.
 *
 * - [PENDING] : 미인증 (기본값)
 * - [VERIFIED] : 인증 완료
 * - [FAILED] : 인증 실패 — 마감까지 미제출했거나, 제출한 사진을 AI 가 반려했다 (계약 §4)
 *
 * [FAILED] 가 두 사건을 함께 뜻하는 이유는 결과가 같아서다 — 어느 쪽이든 그 날의 인증은 성립하지
 * 않고 패배 집계도 동일해서, 값을 갈라도 화면이나 판정이 달리 행동할 것이 없다.
 *
 * 둘을 갈라야 할 때 기준은 **사진 유무**다. 미제출은 `photoUrl` 이 null 이고, AI 반려는 사용자가
 * 실제로 올린 사진이 남아 있다. 이 상태만 보고 "사진이 없다" 고 단정하면 반려 건에서 사용자가 올린
 * 사진을 화면에서 지우게 된다.
 */
enum class VerificationStatus {
    PENDING,
    VERIFIED,
    FAILED,
}
