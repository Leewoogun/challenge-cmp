package com.lwg.challenge.domain.model.challenge

import kotlinx.datetime.LocalDateTime

/**
 * 내가 받은 도전장 1건 (응답 대기 중).
 *
 * @property challengeId 챌린지 식별자 (수락 / 거절 요청에 사용)
 * @property opponent 도전장을 보낸 상대
 * @property penalty 진 쪽이 해야 하는 벌칙
 * @property deadline 마감 시각 (KST). 마감 임박 표시는 표시 시점에 계산
 * @property createdAt 도전장 생성 시각 (KST). 파싱 실패 시 null
 */
data class ReceivedChallenge(
    val challengeId: Long,
    val opponent: ReceivedOpponentInfo,
    val penalty: String,
    val deadline: LocalDateTime,
    val createdAt: LocalDateTime?,
)

/**
 * 받은 도전장을 보낸 상대.
 *
 * @property id 상대 사용자 식별자
 * @property nickname 상대 닉네임
 * @property profileImageUrl 프로필 이미지. 카카오 프로필 미설정 시 null
 * @property mission 상대가 스스로에게 건 미션
 */
data class ReceivedOpponentInfo(
    val id: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val mission: String,
)
