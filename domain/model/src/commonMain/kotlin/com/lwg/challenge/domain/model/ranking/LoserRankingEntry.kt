package com.lwg.challenge.domain.model.ranking

/**
 * 개돼지 랭킹 1행.
 *
 * @property rank 1부터 시작하는 연속 고유값. 공동 순위 없음. 앱은 재랭킹·재정렬하지 않는다
 * @property userId 해당 사용자 id
 * @property nickname 사용자 닉네임
 * @property profileImageUrl null = 미설정
 * @property isMe "나" 판정의 정본. userId 대조로 다시 구하지 않는다
 * @property losses 총 패배 수
 * @property lossRate 0~100 정수 퍼센트. losses/total 로 재계산 금지 — 이 값이 정렬 키라 앱이
 *   따로 계산하면 정렬 순서와 표시 값이 갈린다
 * @property currentLossStreak 현재 연패 수
 * @property totalChallenges 캡션 표시 분기 전용(`0` = 기록 없음). 정렬 키가 아니다
 */
data class LoserRankingEntry(
    val rank: Int,
    val userId: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val isMe: Boolean,
    val losses: Int,
    val lossRate: Int,
    val currentLossStreak: Int,
    val totalChallenges: Int,
)
