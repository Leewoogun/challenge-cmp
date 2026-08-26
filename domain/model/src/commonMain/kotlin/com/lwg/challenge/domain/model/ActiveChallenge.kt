package com.lwg.challenge.domain.model

import com.lwg.challenge.domain.model.challenge.ChallengeOutcome
import kotlinx.datetime.LocalDateTime

/**
 * 홈 화면에 노출되는 챌린지 1건 (진행 중 + 최근 판정된 건).
 *
 * @property challengeId 챌린지 식별자 (카드 탭 시 상세로 전달)
 * @property myMission 현재 사용자의 미션 (예: "오늘 운동 1시간 하기")
 * @property opponentNickname 상대 닉네임
 * @property opponentMission 상대 미션
 * @property deadline 마감 시각 (KST). 상대 시간 텍스트는 표시 시점에 변환.
 * @property myVerificationStatus 현재 사용자 인증 상태
 * @property opponentVerificationStatus 상대 인증 상태
 * @property bet 내기 내용 (예: "커피 사기 ☕")
 * @property status 홈 카드가 남은 시간을 그릴지 결과를 그릴지 가르는 값
 * @property outcome 판정 결과. **이미 내 시점**이다 — 상세([ChallengeDetail.result])가 역할
 *   (challenger/opponent) 기준인 것과 대비된다. 이 응답은 `myMission` 등 전 필드가 내 시점이라
 *   좌표계가 일관되고, 그래서 여기만 역할 기준으로 두면 오히려 화면이 한 번 더 뒤집어야 한다.
 *   [ActiveChallengeStatus.IN_PROGRESS] 면 null.
 */
data class ActiveChallenge(
    val challengeId: Long,
    val myMission: String,
    val opponentNickname: String,
    val opponentMission: String,
    val deadline: LocalDateTime,
    val myVerificationStatus: VerificationStatus,
    val opponentVerificationStatus: VerificationStatus,
    val bet: String,
    val status: ActiveChallengeStatus,
    val outcome: ChallengeOutcome?,
)
