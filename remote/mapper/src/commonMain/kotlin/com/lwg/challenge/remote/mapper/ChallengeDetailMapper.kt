package com.lwg.challenge.remote.mapper

import com.lwg.challenge.domain.model.challenge.ChallengeDetail
import com.lwg.challenge.domain.model.challenge.ChallengeResult
import com.lwg.challenge.domain.model.challenge.Contract
import com.lwg.challenge.domain.model.challenge.ContractParty
import com.lwg.challenge.remote.model.challenge.ChallengeDetailResponse
import com.lwg.challenge.remote.model.challenge.ContractDto
import com.lwg.challenge.remote.model.challenge.ContractPartyDto

/**
 * 챌린지 상세 응답 DTO → Domain 매핑.
 *
 * `deadline` 이 null(포맷 불일치) 이면 **전체를 null 로** 돌린다 — 마감이 없으면 계약서를 그릴 수
 * 없다. `ReceivedChallengeDto.toReceivedChallenge()` 와 같은 정책이다.
 * `challengeDate` null 은 그대로 통과한다 — 카운트다운이 `deadline` 기준이라 중요도가 갈린다.
 *
 * 서명 문자열 디코드 실패(빈 문자열 포함)는 매핑 실패로 승격하지 않는다. `signedAt` 은 남기고
 * `signature` 만 null 로 두면 `Contract` 의 상태 헬퍼가 `UNREADABLE` 로 판정한다 —
 * '서명 대기 중'과 '읽지 못함'을 구분하기 위해서다.
 *
 * `result` 는 모르는 문자열이면 null 로 흡수한다 — 아래 [toChallengeResultOrNull] 참고.
 *
 * `challenger` / `opponent` 는 역할 그대로 옮긴다. '나 / 상대' 관점 변환은 하지 않는다.
 * `result` 도 역할 기준 그대로다.
 * `mission` 의 null 은 빈 문자열로 뭉개지 않는다 — '아직 미션이 없음'(PENDING) 과 '빈 미션'을
 * 같은 값으로 만들면 화면이 빈 행을 그리게 된다.
 *
 * `contract` 의 null 은 `deadline` 과 달리 전체 매핑 실패로 승격하지 않는다. soul-oath 도입 이전에
 * 만들어진 챌린지는 계약서가 없고, 그건 계약이 허용하는 정상 상태다 — 빈 `Contract` 로 채우면
 * `IN_PROGRESS` 인데 `isFinalized = false` 인 없는 맹세를 날조하게 된다.
 */
fun ChallengeDetailResponse.toChallengeDetail(): ChallengeDetail? {
    val deadline = data.deadline ?: return null
    return ChallengeDetail(
        challengeId = data.challengeId,
        status = data.status,
        result = data.result.toChallengeResultOrNull(),
        forfeitedBy = data.forfeitedBy,
        challengeDate = data.challengeDate,
        deadline = deadline,
        betContent = data.betContent,
        challenger = data.challenger.toContractParty(),
        opponent = data.opponent.toContractParty(),
        contract = data.contract?.toContract(),
    )
}

/**
 * 모르는 결과값은 null 로 흡수한다 — enum 조회 실패로 예외를 던지지 않는다는 도메인 규칙이고,
 * 서버가 결과 종류를 추가해도 상세 조회 전체가 깨지지 않는다.
 *
 * 흡수한 값이 '아직 판정되지 않음'(`COMPLETED` 이전) 과 같은 자리로 떨어지는 건 의도한 것이다.
 * 모르는 결과를 아무 결과로 날조하는 것보다 '결과 없음' 이 보수적이다.
 *
 * 선례는 같은 모듈 `VerificationMappers.kt` 의 [toVerificationStatusOrDefault] 다. 거기는 기본값
 * `PENDING` 이 보수적 표시라 기본값을 썼고, 여기서는 null 이 그 자리다.
 */
internal fun String?.toChallengeResultOrNull(): ChallengeResult? =
    this?.let { raw -> ChallengeResult.entries.find { it.name == raw } }

private fun ContractPartyDto.toContractParty(): ContractParty = ContractParty(
    userId = userId,
    nickname = nickname,
    profileImageUrl = profileImageUrl,
    mission = mission,
)

private fun ContractDto.toContract(): Contract = Contract(
    content = content,
    isFinalized = isFinalized,
    challengerSignature = challengerSignature?.let(SignatureCodec::decode),
    challengerSignedAt = challengerSignedAt,
    opponentSignature = opponentSignature?.let(SignatureCodec::decode),
    opponentSignedAt = opponentSignedAt,
)
