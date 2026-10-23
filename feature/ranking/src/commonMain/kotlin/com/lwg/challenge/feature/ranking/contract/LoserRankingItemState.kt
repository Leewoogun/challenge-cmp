package com.lwg.challenge.feature.ranking.contract

import androidx.compose.runtime.Immutable
import com.lwg.challenge.domain.model.ranking.LoserRankingEntry

/** 챌린지 상세(`MY_LABEL`)와 같은 관례 — 내 행은 닉네임 자리에 이 라벨이 들어간다. */
private const val MY_LABEL = "나"

private const val NO_RECORD_TEXT = "아직 기록 없음"

private const val FIRST_RANK = 1
private const val THIRD_RANK = 3

/** 명단 rank 숫자의 강조 축. 색 슬롯 선택은 컴포넌트가 하고, 어떤 자리를 강조할지는 여기서 정한다. */
enum class RankEmphasis {
    FIRST,
    THIRD,
    PLAIN,
}

/** 개돼지 랭킹 1행 UI State. 포디움과 명단이 같은 인스턴스를 공유한다. */
@Immutable
data class LoserRankingItemState(
    private val model: LoserRankingEntry,
) {

    val userId: Long get() = model.userId
    val profileImageUrl: String? get() = model.profileImageUrl
    val rankText: String get() = model.rank.toString()
    val isMe: Boolean get() = model.isMe

    /** 내 행은 닉네임 대신 "나". 아바타 이니셜도 이 값을 쓴다. */
    val displayName: String get() = if (model.isMe) MY_LABEL else model.nickname

    /** 카드 보더를 error 계열로 올릴지. rank 숫자 색과 축이 달라 [rankEmphasis] 와 따로 둔다. */
    val isTopThree: Boolean get() = model.rank <= THIRD_RANK

    val rankEmphasis: RankEmphasis
        get() = when (model.rank) {
            FIRST_RANK -> RankEmphasis.FIRST
            THIRD_RANK -> RankEmphasis.THIRD
            else -> RankEmphasis.PLAIN
        }

    /**
     * 명단 캡션.
     *
     * `totalChallenges == 0` 분기가 필요한 이유: 챌린지를 한 번도 안 한 사람과 전승한 사람이
     * 둘 다 `0패 · 패배율 0%` 라 글자 하나까지 같아진다. 정반대 상태라 구분한다.
     * `lossRate` 는 서버가 쥔 정렬 키라 앱이 `losses / total` 로 재계산하지 않는다.
     */
    val listCaptionText: String
        get() = if (model.totalChallenges == 0) {
            NO_RECORD_TEXT
        } else {
            "${model.losses}패 · 패배율 ${model.lossRate}%"
        }

    /**
     * 포디움 캡션.
     *
     * 🐷 뒤에 오는 수는 언제나 연패다 — 그래서 2·3위 캡션에는 🐷 를 붙이지 않는다.
     *
     * 1위의 연패 절이 조건부인 것은 Lovable 정본에 없는 분기다. 정본은 1위에 연패를 무조건 찍지만,
     * 서버 정렬 1차 키가 `losses DESC` 라 **연패 0 인 1위가 실제로 발생**하고 그대로 두면
     * `🐷0연패` 가 찍힌다. 같은 화면의 명단 뱃지가 이미 `연패 > 0` 조건부라, 정본에서 벗어나는 게
     * 아니라 같은 규칙을 화면 전체에 일관되게 적용하는 것이다. (design.md §1.2.5 / §1.2.6)
     *
     * 가정이 아니라 실측이다 — backend 실서버 응답의 1위(테스터1)가 `losses=4, currentLossStreak=0`
     * 이라, 개발 DB 로 랭킹을 처음 띄우면 그 자리에서 바로 이 분기를 밟는다.
     */
    val podiumCaptionText: String
        get() = if (model.rank == FIRST_RANK && model.currentLossStreak > 0) {
            "🐷${model.currentLossStreak}연패 · ${model.losses}패"
        } else {
            "${model.losses}패"
        }

    /** 연패 중인 사람에게만 붙는 표식이라 0 이면 뱃지 자체를 그리지 않는다. */
    val hasStreakBadge: Boolean get() = model.currentLossStreak > 0
    val streakBadgeText: String get() = "🐷${model.currentLossStreak}"
}
