package com.lwg.challenge.remote.mapper

import com.lwg.challenge.domain.model.ActiveChallenge
import com.lwg.challenge.domain.model.ActiveChallengeStatus
import com.lwg.challenge.domain.model.VerificationStatus
import com.lwg.challenge.domain.model.challenge.ChallengeOutcome
import com.lwg.challenge.remote.model.challenge.ActiveChallengeDto
import com.lwg.challenge.remote.model.challenge.ActiveChallengeResponse
import com.lwg.challenge.remote.model.challenge.VerificationStatusDto

/**
 * Active challenge DTO → Domain 매핑.
 *
 * 시간 파싱은 DTO 직렬화기(WireLocalDateTimeSerializer)가 하고, 실패는 null 로 도착한다.
 * `deadline` 이 null 이면 해당 항목 제외([MappedList.droppedCount]에 집계) — 카드 전체가 deadline 에 걸려 있다.
 * `status` 가 모르는 값이면 해당 항목 제외, 비어 있으면 `IN_PROGRESS` 로 간주 —
 * 아래 [toActiveChallengeStatusOrInProgressIfAbsent] 참고.
 * `myResult` 가 모르는 값이면 null 로 흡수하고 항목은 살린다. 아래 [toChallengeOutcomeOrNull] 참고.
 *
 * 서버가 준 순서를 그대로 보존한다 — 정렬은 서버 책임이라 앱이 재정렬하지 않는다.
 */
fun ActiveChallengeResponse.toActiveChallenges(): MappedList<ActiveChallenge> {
    val mapped = data.activeChallenges.map(ActiveChallengeDto::toActiveChallenge)
    return MappedList(mapped.filterNotNull(), mapped.count { it == null })
}

private fun ActiveChallengeDto.toActiveChallenge(): ActiveChallenge? {
    val deadline = deadline ?: return null
    val status = status.toActiveChallengeStatusOrInProgressIfAbsent() ?: return null
    return ActiveChallenge(
        challengeId = challengeId,
        myMission = myMission,
        opponentNickname = opponentNickname,
        opponentMission = opponentMission,
        deadline = deadline,
        myVerificationStatus = myVerificationStatus.toDomain(),
        opponentVerificationStatus = opponentVerificationStatus.toDomain(),
        bet = bet,
        status = status,
        outcome = myResult.toChallengeOutcomeOrNull(),
    )
}

/**
 * 빈 문자열(= 키 누락 시 DTO 기본값)이면 `IN_PROGRESS`, 값이 있는데 모르는 값이면 null —
 * 호출부가 null 인 항목만 통째로 드롭한다.
 *
 * 두 경우는 뜻이 다르다.
 * - **누락**은 "이 서버는 아직 status 개념이 없다" 는 뜻이다. 이 필드가 생기기 전 이 목록은 정의상
 *   전부 진행 중이었으므로 `IN_PROGRESS` 가 추측이 아니라 구버전 서버 응답의 정확한 의미다.
 *   이걸 드롭으로 처리하면 status 미배포 서버에서 목록이 통째로 비어 "진행 중인 챌린지가 없어요" 가
 *   뜬다 — 앱이 서버보다 먼저 도는 개발 중에 실제로 밟는 전면 장애다.
 * - **모르는 값**은 "서버가 내가 모르는 상태를 보냈다" 는 뜻이라 복원할 의미가 없다. 카드가 상태에
 *   따라 완전히 다른 것을 그리므로(남은 시간 vs 판정 결과) 아무 쪽으로든 추측해 그리면 틀린 카드를
 *   보여주게 되고, 그건 항목 하나가 빠진 것보다 나쁘다. 그래서 `deadline` 과 같은 드롭 정책을 쓴다.
 *
 * 드롭 개수는 [MappedList.droppedCount] 에 집계돼 repository 가 발화를 판단한다.
 */
internal fun String.toActiveChallengeStatusOrInProgressIfAbsent(): ActiveChallengeStatus? =
    if (isEmpty()) ActiveChallengeStatus.IN_PROGRESS
    else ActiveChallengeStatus.entries.find { it.name == this }

/**
 * 모르는 결과값은 null 로 흡수한다 — [toActiveChallengeStatusOrInProgressIfAbsent] 와 달리 항목을 드롭하지 않는다.
 *
 * 결과를 몰라도 카드의 나머지(미션·상대·내기)는 전부 정상이라 카드 자체는 성립하기 때문이다.
 * `status` 와 중요도가 갈리는 지점이다. 상세의 [toChallengeResultOrNull] 과 같은 판단이다.
 */
internal fun String?.toChallengeOutcomeOrNull(): ChallengeOutcome? =
    this?.let { raw -> ChallengeOutcome.entries.find { it.name == raw } }

private fun VerificationStatusDto.toDomain(): VerificationStatus = when (this) {
    VerificationStatusDto.PENDING -> VerificationStatus.PENDING
    VerificationStatusDto.VERIFIED -> VerificationStatus.VERIFIED
    VerificationStatusDto.FAILED -> VerificationStatus.FAILED
}
