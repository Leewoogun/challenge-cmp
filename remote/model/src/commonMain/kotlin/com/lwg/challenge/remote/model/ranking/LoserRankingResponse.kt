package com.lwg.challenge.remote.model.ranking

import com.lwg.challenge.remote.model.BaseResponse
import kotlinx.serialization.Serializable

/**
 * GET /api/v1/rankings/losers
 *
 * `rankings` 는 정렬된 채로 도착하므로 앱이 재정렬하지 않는다.
 */
@Serializable
data class LoserRankingResponse(
    val data: LoserRankingData = LoserRankingData(),
) : BaseResponse()

@Serializable
data class LoserRankingData(
    val rankings: List<LoserRankingDto> = emptyList(),
)

@Serializable
data class LoserRankingDto(
    val rank: Int = 0,
    val userId: Long = 0L,
    val nickname: String = "",
    val profileImageUrl: String? = null,
    val isMe: Boolean = false,
    val losses: Int = 0,
    /** 0~100 정수 퍼센트. 서버가 반올림한 값이자 정렬 키라, 앱이 losses 로 재계산하지 않는다. */
    val lossRate: Int = 0,
    val currentLossStreak: Int = 0,
    /** 캡션 표시 분기 전용(`0` = 기록 없음). 정렬 키가 아니다. */
    val totalChallenges: Int = 0,
)
