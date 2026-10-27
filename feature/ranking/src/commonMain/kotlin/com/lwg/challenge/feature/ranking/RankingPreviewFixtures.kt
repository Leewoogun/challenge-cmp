package com.lwg.challenge.feature.ranking

import com.lwg.challenge.domain.model.ranking.LoserRankingEntry

/**
 * 프리뷰 공용 픽스처. 실서버 응답 5행으로 표시 분기를 다 덮는다.
 * 1위 = 내 행·연패 0, 2·4위 = 연패 중, 5위 = 기록 없음.
 */
internal val PreviewLoserRankingEntries = listOf(
    LoserRankingEntry(
        rank = 1,
        userId = 14L,
        nickname = "테스터1",
        profileImageUrl = null,
        isMe = true,
        losses = 4,
        lossRate = 80,
        currentLossStreak = 0,
        totalChallenges = 5,
    ),
    LoserRankingEntry(
        rank = 2,
        userId = 15L,
        nickname = "테스터2",
        profileImageUrl = null,
        isMe = false,
        losses = 3,
        lossRate = 100,
        currentLossStreak = 3,
        totalChallenges = 3,
    ),
    LoserRankingEntry(
        rank = 3,
        userId = 1L,
        nickname = "이우건",
        profileImageUrl = "http://img1.kakaocdn.net/thumb/R640x640.q70/?fname=http%3A%2F%2Ft1.kakaocdn.net%2Faccount_images%2Fdefault_profile.jpeg",
        isMe = false,
        losses = 3,
        lossRate = 75,
        currentLossStreak = 0,
        totalChallenges = 4,
    ),
    LoserRankingEntry(
        rank = 4,
        userId = 16L,
        nickname = "테스터3",
        profileImageUrl = null,
        isMe = false,
        losses = 2,
        lossRate = 100,
        currentLossStreak = 2,
        totalChallenges = 2,
    ),
    LoserRankingEntry(
        rank = 5,
        userId = 17L,
        nickname = "테스터4",
        profileImageUrl = null,
        isMe = false,
        losses = 0,
        lossRate = 0,
        currentLossStreak = 0,
        totalChallenges = 0,
    ),
)

/** 한 줄로 안 끝나는 닉네임의 말줄임을 보는 프리뷰용 값. */
internal const val PREVIEW_LONG_NICKNAME = "엄청나게긴닉네임을가진사용자가존재한다면어떻게보일까요"

/** 1위 행만 뒤집는다. 내 행은 "나" 로 바뀌어 긴 닉네임이 안 보여, 긴 닉네임일 때는 남으로 돌린다. */
internal fun LoserRankingEntry.previewAsFirst(
    streak: Int = currentLossStreak,
    longNickname: Boolean = false,
): LoserRankingEntry = copy(
    nickname = if (longNickname) PREVIEW_LONG_NICKNAME else nickname,
    isMe = if (longNickname) false else isMe,
    currentLossStreak = streak,
)
