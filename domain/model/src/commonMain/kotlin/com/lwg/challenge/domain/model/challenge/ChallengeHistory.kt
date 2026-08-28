package com.lwg.challenge.domain.model.challenge

import kotlinx.datetime.LocalDate

/**
 * 계약서 보관함에 쌓이는 종료된 챌린지 1건.
 *
 * @property challengeDate 챌린지 날짜(KST). 월 그룹 키이자 카드에 찍히는 날짜다 — 판정 시각으로
 *   묶으면 자정 배치 때문에 말일 챌린지가 다음 달 헤더 아래로 들어간다.
 * @property myResult **이미 내 시점**이다 — `ActiveChallenge.outcome` 과 같은 좌표계라
 *   앱이 "내가 challenger 인가" 를 알 필요가 없다.
 */
data class ChallengeHistory(
    val challengeId: Long,
    val challengeDate: LocalDate,
    val opponentNickname: String,
    val myMission: String,
    val opponentMission: String,
    val bet: String,
    val myResult: ChallengeOutcome,
)
