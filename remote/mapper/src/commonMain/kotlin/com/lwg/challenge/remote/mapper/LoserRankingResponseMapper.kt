package com.lwg.challenge.remote.mapper

import com.lwg.challenge.domain.model.ranking.LoserRankingEntry
import com.lwg.challenge.remote.model.ranking.LoserRankingDto
import com.lwg.challenge.remote.model.ranking.LoserRankingResponse

/**
 * 서버가 준 순서를 그대로 보존한다 — 정렬·랭크 부여는 서버 책임이라 앱이 재정렬하지 않는다.
 */
fun LoserRankingResponse.toLoserRankingEntries(): List<LoserRankingEntry> =
    data.rankings.map(LoserRankingDto::toLoserRankingEntry)

private fun LoserRankingDto.toLoserRankingEntry(): LoserRankingEntry = LoserRankingEntry(
    rank = rank,
    userId = userId,
    nickname = nickname,
    profileImageUrl = profileImageUrl,
    isMe = isMe,
    losses = losses,
    lossRate = lossRate,
    currentLossStreak = currentLossStreak,
    totalChallenges = totalChallenges,
)
