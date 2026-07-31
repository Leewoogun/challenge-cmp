package com.lwg.challenge.domain.model.challenge

import kotlinx.datetime.LocalDateTime

/**
 * 내가 받은 도전장 1건 (응답 대기 중).
 *
 * @property challengeId 챌린지 식별자 (수락 / 거절 요청에 사용)
 * @property challengerId 도전장을 보낸 사용자 식별자
 * @property challengerNickname 도전장을 보낸 사용자 닉네임
 * @property challengerProfileImageUrl 프로필 이미지. 카카오 프로필 미설정 시 null
 * @property challengerMission 상대가 스스로에게 건 미션
 * @property betContent 내기 내용
 * @property deadline 마감 시각 (KST). 마감 임박 표시는 표시 시점에 계산
 * @property createdAt 도전장 생성 시각 (KST). 파싱 실패 시 null
 */
data class ReceivedChallenge(
    val challengeId: Long,
    val challengerId: Long,
    val challengerNickname: String,
    val challengerProfileImageUrl: String?,
    val challengerMission: String,
    val betContent: String,
    val deadline: LocalDateTime,
    val createdAt: LocalDateTime?,
)
