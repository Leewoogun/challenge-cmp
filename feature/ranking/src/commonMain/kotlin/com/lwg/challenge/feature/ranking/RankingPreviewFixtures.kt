package com.lwg.challenge.feature.ranking

import com.lwg.challenge.domain.model.ranking.LoserRankingEntry

/**
 * 프리뷰 공용 픽스처 — backend 가 넘긴 실서버 `GET /api/v1/rankings/losers` 응답 5행 그대로다
 * (테스터1 = user 14 시점. `LoserRankingResponseMapperTest` 의 실서버 픽스처와 같은 데이터).
 *
 * 표시 분기가 이 한 응답에 다 들어 있어 프리뷰용 데이터를 따로 만들지 않는다.
 * - rank 1 — 내 행이면서 1위라 포디움·명단 양쪽에서 "나" 치환이 걸린다. 동시에 연패 0 인 1위라
 *   포디움 캡션의 🐷 절 생략 분기를 기본값으로 밟는다.
 * - rank 2·4 — 연패 중이라 명단 뱃지가 붙는다.
 * - rank 3 — `profileImageUrl` 이 non-null 이지만 화면은 이니셜 placeholder 를 유지한다.
 * - rank 5 — `totalChallenges` 가 0 이라 명단 캡션이 "아직 기록 없음" 으로 떨어진다.
 *
 * 세 프리뷰 파일이 같은 출처를 쓰도록 여기 한 곳에 둔다. 잘라 쓰는 쪽은 `take(n)` 으로 인원수를 줄이거나
 * 특정 행만 `copy` 로 뒤집는다.
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

/**
 * 1위 행만 프리뷰용으로 뒤집는다.
 *
 * 긴 닉네임을 입힐 때 [LoserRankingEntry.isMe] 를 함께 내리는 이유: 내 행은 닉네임이 "나" 로 치환돼
 * 아무리 긴 값을 넣어도 화면에 안 나타난다. 실측 1위가 하필 내 행이라 남으로 돌려야 확인이 된다.
 */
internal fun LoserRankingEntry.previewAsFirst(
    streak: Int = currentLossStreak,
    longNickname: Boolean = false,
): LoserRankingEntry = copy(
    nickname = if (longNickname) PREVIEW_LONG_NICKNAME else nickname,
    isMe = if (longNickname) false else isMe,
    currentLossStreak = streak,
)
