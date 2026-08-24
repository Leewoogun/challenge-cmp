package com.lwg.challenge.remote.mapper

import com.lwg.challenge.domain.model.VerificationStatus
import com.lwg.challenge.domain.model.verification.ChallengeVerifications
import com.lwg.challenge.domain.model.verification.PartyVerification
import com.lwg.challenge.domain.model.verification.VerificationSubmitResult
import com.lwg.challenge.remote.model.verification.ChallengeVerificationsResponse
import com.lwg.challenge.remote.model.verification.PartyVerificationDto
import com.lwg.challenge.remote.model.verification.SubmitVerificationResponse

// 챌린지 인증 응답 DTO → Domain 매핑.
// 시간 파싱은 DTO 직렬화기(WireLocalDateTimeSerializer)가 하고, 실패는 null 로 도착한다.

/**
 * `verifiedAt` null 은 그대로 통과한다 — 도메인 계약이 nullable 로 명시한 필드라 '없는 게
 * 사실'일 수 있고(`contract: null` 과 같은 판단), 확정 자체는 status 로 이미 성립했다.
 */
fun SubmitVerificationResponse.toSubmitResult(): VerificationSubmitResult = VerificationSubmitResult(
    challengeId = data.challengeId,
    status = data.status.toVerificationStatusOrDefault(),
    verifiedAt = data.verifiedAt,
)

fun ChallengeVerificationsResponse.toChallengeVerifications(): ChallengeVerifications = ChallengeVerifications(
    challengeId = data.challengeId,
    challenger = data.challenger.toPartyVerification(),
    opponent = data.opponent.toPartyVerification(),
)

private fun PartyVerificationDto.toPartyVerification(): PartyVerification = PartyVerification(
    userId = userId,
    status = status.toVerificationStatusOrDefault(),
    photoUrl = photoUrl,
    verifiedAt = verifiedAt,
)

/**
 * 모르는 상태값은 [VerificationStatus.PENDING] 으로 둔다 — enum 조회 실패는 기본값으로
 * 흡수한다는 도메인 규칙(예외 throw 금지)과 같고, 서버가 상태를 추가해도 조회 전체가
 * 깨지지 않는다. PENDING 은 '인증 없음'이라 잘못돼도 사진을 날조하지 않는 보수적 표시다.
 */
internal fun String.toVerificationStatusOrDefault(): VerificationStatus =
    VerificationStatus.entries.find { it.name == this } ?: VerificationStatus.PENDING
