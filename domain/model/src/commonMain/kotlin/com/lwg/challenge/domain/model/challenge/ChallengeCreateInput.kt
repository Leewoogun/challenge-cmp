package com.lwg.challenge.domain.model.challenge

import com.lwg.challenge.domain.model.signature.Signature

/**
 * 챌린지 신청 입력값.
 *
 * @property opponentId 도전장을 받을 친구의 사용자 식별자
 * @property myMission 내가 수행할 미션 (1~100자)
 * @property betContent 내기 내용 (1~100자)
 * @property deadlineType 마감 기준
 * @property signature 챌린저(나)의 영혼의 맹세 서명. 빈 서명은 저장 시도 전에 거부된다
 */
data class ChallengeCreateInput(
    val opponentId: Long,
    val myMission: String,
    val betContent: String,
    val deadlineType: DeadlineType,
    val signature: Signature,
)
