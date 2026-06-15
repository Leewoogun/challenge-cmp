@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.lwg.challenge.domain.model

import kotlin.time.Instant

/**
 * 홈 화면에 노출되는 진행 중 챌린지 1건.
 *
 * api-contract.md §성공 Response 의 7개 표시 필드 + 식별자.
 * "내" / "상대" 시점은 백엔드가 현재 사용자 기준으로 이미 매핑하여 응답한다.
 *
 * @property challengeId 챌린지 식별자 (카드 탭 시 상세로 전달)
 * @property myMission 현재 사용자의 미션 (예: "오늘 운동 1시간 하기")
 * @property opponentNickname 상대 닉네임
 * @property opponentMission 상대 미션
 * @property deadline 마감 시각 (ISO-8601 UTC). 상대 시간 텍스트는 표시 시점에 변환.
 * @property myVerificationStatus 현재 사용자 인증 상태
 * @property opponentVerificationStatus 상대 인증 상태
 * @property bet 내기 내용 (예: "커피 사기 ☕")
 */
data class ActiveChallenge(
    val challengeId: Long,
    val myMission: String,
    val opponentNickname: String,
    val opponentMission: String,
    val deadline: Instant,
    val myVerificationStatus: VerificationStatus,
    val opponentVerificationStatus: VerificationStatus,
    val bet: String,
)
