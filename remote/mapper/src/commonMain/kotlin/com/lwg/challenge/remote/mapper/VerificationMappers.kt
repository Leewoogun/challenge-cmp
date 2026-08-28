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

fun ChallengeVerificationsResponse.toChallengeVerifications(baseUrl: String): ChallengeVerifications =
    ChallengeVerifications(
        challengeId = data.challengeId,
        challenger = data.challenger.toPartyVerification(baseUrl),
        opponent = data.opponent.toPartyVerification(baseUrl),
    )

private fun PartyVerificationDto.toPartyVerification(baseUrl: String): PartyVerification = PartyVerification(
    userId = userId,
    status = status.toVerificationStatusOrDefault(),
    photoUrl = joinPhotoUrl(baseUrl, photoUrl),
    verifiedAt = verifiedAt,
    photoDeleted = photoDeleted,
)

/**
 * 슬래시가 겹쳐 `//` 가 되면 서버가 404 로 거부하는데, 그 404 가 '상대가 아직 인증 안 함' 과
 * 구분되지 않는다 — 사진이 영영 안 뜨는데 앱은 미인증으로 읽어 에러도 로그도 남지 않는다.
 * 조인이 한 곳뿐이라 이 함수가 그 유일한 방지선이고, 그래서 양쪽을 모두 다듬는다
 * (baseUrl 은 trailing slash 있고 photoUrl 은 leading slash 있는 게 실제 조합이다).
 *
 * baseUrl 을 파라미터로 받는 이유: 이 모듈은 `:remote:network` 에 의존하지 않는다(매퍼를 순수하게 둔다).
 */
internal fun joinPhotoUrl(baseUrl: String, path: String?): String? =
    path?.let { baseUrl.trimEnd('/') + "/" + it.trimStart('/') }

/**
 * 모르는 상태값은 [VerificationStatus.PENDING] 으로 둔다 — enum 조회 실패는 기본값으로
 * 흡수한다는 도메인 규칙(예외 throw 금지)과 같고, 서버가 상태를 추가해도 조회 전체가
 * 깨지지 않는다. PENDING 은 '인증 없음'이라 잘못돼도 사진을 날조하지 않는 보수적 표시다.
 */
internal fun String.toVerificationStatusOrDefault(): VerificationStatus =
    VerificationStatus.entries.find { it.name == this } ?: VerificationStatus.PENDING
