package com.lwg.challenge.domain.model.verification

import com.lwg.challenge.domain.model.VerificationStatus
import kotlinx.datetime.LocalDateTime

/**
 * 인증 제출 확정 결과.
 *
 * @property verifiedAt 인증 확정 시각 (KST). 도메인 계약상 nullable — 값이 없어도 확정 자체는 유효하다
 */
data class VerificationSubmitResult(
    val challengeId: Long,
    val status: VerificationStatus,
    val verifiedAt: LocalDateTime?,
)
