package com.lwg.challenge.domain.model.challenge

/**
 * 챌린지 신청 입력값.
 *
 * @property opponentId 도전장을 받을 친구의 사용자 식별자
 * @property myMission 내가 수행할 미션 (1~100자)
 * @property betContent 내기 내용 (1~100자)
 * @property deadlineType 마감 기준
 */
data class ChallengeCreateInput(
    val opponentId: Long,
    val myMission: String,
    val betContent: String,
    val deadlineType: DeadlineType,
)
