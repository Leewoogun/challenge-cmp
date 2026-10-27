package com.lwg.challenge.feature.ranking.contract

import com.lwg.challenge.domain.model.ranking.LoserRankingEntry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** 랭킹 1행의 표시 규칙. 기록 없음 캡션과 1위 연패 절 조건부는 근거가 사라지기 쉬워 고정해 둔다. */
class LoserRankingEntryDisplayTest {

    private fun entry(
        rank: Int = 1,
        userId: Long = 1L,
        nickname: String = "준혁",
        profileImageUrl: String? = null,
        isMe: Boolean = false,
        losses: Int = 10,
        lossRate: Int = 50,
        currentLossStreak: Int = 0,
        totalChallenges: Int = 20,
    ) = LoserRankingEntry(
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

    @Test
    fun `내 행이면 닉네임 대신 나 로 표시한다`() {
        assertEquals("나", entry(nickname = "이우건", isMe = true).displayName)
    }

    @Test
    fun `내 행이 아니면 닉네임을 그대로 표시한다`() {
        assertEquals("이우건", entry(nickname = "이우건", isMe = false).displayName)
    }

    @Test
    fun `기록이 없으면 명단 캡션이 아직 기록 없음 이다`() {
        val item = entry(totalChallenges = 0, losses = 0, lossRate = 0)

        assertEquals("아직 기록 없음", item.listCaption)
    }

    @Test
    fun `기록이 있으면 명단 캡션이 패 수와 패배율이다`() {
        val item = entry(totalChallenges = 32, losses = 25, lossRate = 78)

        assertEquals("25패 · 패배율 78%", item.listCaption)
    }

    /** 둘 다 `0패 · 패배율 0%` 인데 한 명은 한 번도 안 했고 한 명은 전승이다. */
    @Test
    fun `기록 없는 사람과 전승자는 수치가 같아도 명단 캡션이 서로 다르다`() {
        val noRecord = entry(totalChallenges = 0, losses = 0, lossRate = 0)
        val allWins = entry(totalChallenges = 12, losses = 0, lossRate = 0)

        assertEquals("아직 기록 없음", noRecord.listCaption)
        assertEquals("0패 · 패배율 0%", allWins.listCaption)
        assertNotEquals(noRecord.listCaption, allWins.listCaption)
    }

    @Test
    fun `1위이고 연패 중이면 Top3 카드 캡션에 돼지 연패 절이 붙는다`() {
        val item = entry(rank = 1, currentLossStreak = 7, losses = 25)

        assertEquals("🐷7연패 · 25패", item.cardCaption)
    }

    /** 정렬 1차 키가 패 수라 연패 0 인 1위가 실제로 나온다. 조건부가 없으면 `🐷0연패` 가 찍힌다. */
    @Test
    fun `1위여도 연패가 0 이면 Top3 카드 캡션은 패 수만 찍는다`() {
        val item = entry(rank = 1, currentLossStreak = 0, losses = 25)

        assertEquals("25패", item.cardCaption)
    }

    @Test
    fun `2위는 연패 중이어도 Top3 카드 캡션에 돼지를 붙이지 않는다`() {
        val item = entry(rank = 2, currentLossStreak = 4, losses = 18)

        assertEquals("18패", item.cardCaption)
        assertFalse(item.cardCaption.contains("연패"))
    }

    @Test
    fun `3위는 연패 중이어도 Top3 카드 캡션에 돼지를 붙이지 않는다`() {
        val item = entry(rank = 3, currentLossStreak = 2, losses = 12)

        assertEquals("12패", item.cardCaption)
        assertFalse(item.cardCaption.contains("연패"))
    }

    @Test
    fun `연패가 0 이면 연패 뱃지를 그리지 않는다`() {
        val item = entry(currentLossStreak = 0)

        assertNull(item.streakBadge)
    }

    @Test
    fun `연패 중이면 뱃지를 그리고 연패 수를 찍는다`() {
        val item = entry(currentLossStreak = 3)

        assertEquals("🐷3", item.streakBadge)
    }

    @Test
    fun `3위까지는 isTopThree 가 true 이고 4위부터 false 다`() {
        assertTrue(entry(rank = 1).isTopThree)
        assertTrue(entry(rank = 3).isTopThree)
        assertFalse(entry(rank = 4).isTopThree)
    }
}
