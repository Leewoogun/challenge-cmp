package com.lwg.challenge.remote.mapper

import com.lwg.challenge.domain.model.challenge.ChallengeDetail
import com.lwg.challenge.domain.model.challenge.ChallengeSide
import com.lwg.challenge.domain.model.challenge.Contract
import com.lwg.challenge.domain.model.challenge.ContractUserInfo
import com.lwg.challenge.remote.model.challenge.ChallengeDetailResponse
import com.lwg.challenge.remote.model.challenge.ChallengeSideDto
import com.lwg.challenge.remote.model.challenge.ContractDto
import com.lwg.challenge.remote.model.challenge.ContractUserInfoDto

/**
 * 챌린지 상세 응답 DTO → Domain 매핑.
 *
 * `deadline` 이 null(포맷 불일치) 이면 마감 없이 계약서를 그릴 수 없어 전체를 null 로 돌린다.
 *
 * 서명 문자열 디코드 실패는 `signedAt` 은 남기고 `signature` 만 null 로 둔다 —
 * `Contract` 가 이 조합을 `UNREADABLE` 로 판정해 '서명 대기 중' 과 구분한다.
 */
fun ChallengeDetailResponse.toChallengeDetail(): ChallengeDetail? {
    val deadline = data.deadline ?: return null
    return ChallengeDetail(
        challengeId = data.challengeId,
        status = data.status,
        myResult = data.myResult?.toDomain(),
        forfeitedBy = data.forfeitedBy?.toDomain(),
        challengeDate = data.challengeDate,
        deadline = deadline,
        betContent = data.betContent,
        me = data.me.toContractUserInfo(),
        opponent = data.opponent.toContractUserInfo(),
        contract = data.contract?.toContract(),
    )
}

private fun ChallengeSideDto.toDomain(): ChallengeSide = when (this) {
    ChallengeSideDto.ME -> ChallengeSide.ME
    ChallengeSideDto.OPPONENT -> ChallengeSide.OPPONENT
}

private fun ContractUserInfoDto.toContractUserInfo(): ContractUserInfo = ContractUserInfo(
    nickname = nickname,
    profileImageUrl = profileImageUrl,
    mission = mission,
)

private fun ContractDto.toContract(): Contract = Contract(
    content = content,
    isFinalized = isFinalized,
    mySignature = mySignature?.let(SignatureCodec::decode),
    mySignedAt = mySignedAt,
    opponentSignature = opponentSignature?.let(SignatureCodec::decode),
    opponentSignedAt = opponentSignedAt,
)
