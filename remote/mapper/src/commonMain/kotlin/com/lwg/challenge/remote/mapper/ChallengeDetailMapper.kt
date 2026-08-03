package com.lwg.challenge.remote.mapper

import com.lwg.challenge.domain.model.challenge.ChallengeDetail
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
 * `challenger` / `opponent` 는 역할 그대로 옮긴다. '나 / 상대' 관점 변환은 하지 않는다.
 * `mission` 의 null 은 빈 문자열로 뭉개지 않는다 — '아직 미션이 없음'(PENDING) 과 '빈 미션'을
 * 같은 값으로 만들면 화면이 빈 행을 그리게 된다.
 */
fun ChallengeDetailResponse.toChallengeDetail(): ChallengeDetail? {
    val deadline = data.deadline ?: return null
    return ChallengeDetail(
        challengeId = data.challengeId,
        status = data.status,
        challengeDate = data.challengeDate,
        deadline = deadline,
        betContent = data.betContent,
        challenger = data.challenger.toContractParty(),
        opponent = data.opponent.toContractParty(),
        contract = data.contract.toContract(),
    )
}

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
