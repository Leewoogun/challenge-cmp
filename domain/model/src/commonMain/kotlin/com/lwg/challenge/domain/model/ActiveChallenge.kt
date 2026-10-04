package com.lwg.challenge.domain.model

import com.lwg.challenge.domain.model.challenge.ChallengeOutcome
import kotlinx.datetime.LocalDateTime

/**
 * 홈 화면에 노출되는 챌린지 1건 (진행 중 + 최근 판정된 건).
 *
 * @property challengeId 챌린지 식별자 (카드 탭 시 상세로 전달)
 * @property me 내 미션 · 인증 상태
 * @property opponent 상대 닉네임 · 미션 · 인증 상태
 * @property deadline 마감 시각 (KST). 상대 시간 텍스트는 표시 시점에 변환.
 * @property penalty 진 쪽이 해야 하는 벌칙 (예: "커피 사기 ☕")
 * @property status 홈 카드가 남은 시간을 그릴지 결과를 그릴지 가르는 값
 * @property outcome 판정 결과.
 *   [ActiveChallengeStatus.IN_PROGRESS] 면 null.
 */
data class ActiveChallenge(
    val challengeId: Long,
    val me: MyChallengeInfo,
    val opponent: OpponentChallengeInfo,
    val deadline: LocalDateTime,
    val penalty: String,
    val status: ActiveChallengeStatus,
    val outcome: ChallengeOutcome?,
)

/**
 * 챌린지에서 내 몫.
 *
 * @property mission 내 미션 (예: "오늘 운동 1시간 하기")
 * @property verificationStatus 내 인증 상태
 */
data class MyChallengeInfo(
    val mission: String,
    val verificationStatus: VerificationStatus,
)

/**
 * 챌린지에서 상대 몫.
 *
 * @property nickname 상대 닉네임
 * @property mission 상대 미션
 * @property verificationStatus 상대 인증 상태
 */
data class OpponentChallengeInfo(
    val nickname: String,
    val mission: String,
    val verificationStatus: VerificationStatus,
)
