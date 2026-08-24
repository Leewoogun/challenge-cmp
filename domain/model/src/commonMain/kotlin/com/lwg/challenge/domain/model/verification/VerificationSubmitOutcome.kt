package com.lwg.challenge.domain.model.verification

/**
 * 인증 제출 결과.
 *
 * 콜백이 아닌 반환값인 이유: 서버가 판정해 거부한 것([Rejected])과 전송이 끊겨 서버 반영
 * 여부를 모르는 것([Unreachable])은 앱의 조치가 정반대다 — 전자는 알리고 끝, 후자는
 * 실제 인증 상태를 조회해 확인해야 한다. `onError(String)` 한 채널로는 구분이 불가능하다.
 */
sealed interface VerificationSubmitOutcome {
    data class Success(val result: VerificationSubmitResult) : VerificationSubmitOutcome
    data class Rejected(val message: String) : VerificationSubmitOutcome
    data class Unreachable(val message: String) : VerificationSubmitOutcome
}
