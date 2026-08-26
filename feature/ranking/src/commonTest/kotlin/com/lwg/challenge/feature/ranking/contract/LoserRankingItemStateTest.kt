package com.lwg.challenge.feature.ranking.contract

import com.lwg.challenge.domain.model.ranking.LoserRankingEntry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * 개돼지 랭킹 1행의 표시 규칙 전량. 파생값이 전부 [LoserRankingItemState] 의 `get()` 에 몰려 있어
 * 화면 문구가 어긋나면 원인이 거의 여기다.
 *
 * 특히 두 분기는 Lovable 정본과 다르거나 정본에 아예 없는 것이라 근거가 사라지기 쉽다.
 * - `totalChallenges == 0` 캡션 분기 (기록 없는 사람 vs 전승자 구분)
 * - 1위 포디움 캡션의 연패 절 조건부 (연패 0 인 1위가 실제로 발생)
 */
class LoserRankingItemStateTest {

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

    private fun state(entry: LoserRankingEntry) = LoserRankingItemState(entry)

    @Test
    fun `내 행이면 닉네임 대신 나 로 표시한다`() {
        val item = state(entry(nickname = "이우건", isMe = true))

        assertEquals("나", item.displayName)
        assertTrue(item.isMe)
    }

    @Test
    fun `내 행이 아니면 닉네임을 그대로 표시한다`() {
        val item = state(entry(nickname = "이우건", isMe = false))

        assertEquals("이우건", item.displayName)
        assertFalse(item.isMe)
    }

    @Test
    fun `rank 와 userId 는 그대로 노출된다`() {
        val item = state(entry(rank = 7, userId = 42L))

        assertEquals("7", item.rankText)
        assertEquals(42L, item.userId)
    }

    @Test
    fun `기록이 없으면 명단 캡션이 아직 기록 없음 이다`() {
        val item = state(entry(totalChallenges = 0, losses = 0, lossRate = 0))

        assertEquals("아직 기록 없음", item.listCaptionText)
    }

    @Test
    fun `기록이 있으면 명단 캡션이 패 수와 패배율이다`() {
        val item = state(entry(totalChallenges = 32, losses = 25, lossRate = 78))

        assertEquals("25패 · 패배율 78%", item.listCaptionText)
    }

    /**
     * 이 분기가 존재하는 유일한 이유. 두 사람은 `losses` 와 `lossRate` 가 글자 하나까지 같은데
     * 정반대 상태다 — 한 명은 챌린지를 한 번도 안 했고, 한 명은 12번 전부 이겼다.
     * 분기가 사라지면 둘 다 `0패 · 패배율 0%` 로 합쳐져 화면에서 구분이 불가능해진다.
     */
    @Test
    fun `기록 없는 사람과 전승자는 수치가 같아도 명단 캡션이 서로 다르다`() {
        val noRecord = state(entry(totalChallenges = 0, losses = 0, lossRate = 0))
        val allWins = state(entry(totalChallenges = 12, losses = 0, lossRate = 0))

        assertEquals("아직 기록 없음", noRecord.listCaptionText)
        assertEquals("0패 · 패배율 0%", allWins.listCaptionText)
        assertNotEquals(noRecord.listCaptionText, allWins.listCaptionText)
    }

    @Test
    fun `1위이고 연패 중이면 포디움 캡션에 돼지 연패 절이 붙는다`() {
        val item = state(entry(rank = 1, currentLossStreak = 7, losses = 25))

        assertEquals("🐷7연패 · 25패", item.podiumCaptionText)
    }

    /**
     * 정본에 없는 분기다. 서버 정렬 1차 키가 `losses DESC` 라 연패 0 인 1위가 실제로 발생하고,
     * 조건부가 사라지면 개돼지왕 자리에 `🐷0연패` 가 찍힌다.
     */
    @Test
    fun `1위여도 연패가 0 이면 포디움 캡션은 패 수만 찍는다`() {
        val item = state(entry(rank = 1, currentLossStreak = 0, losses = 25))

        assertEquals("25패", item.podiumCaptionText)
    }

    @Test
    fun `2위는 연패 중이어도 포디움 캡션에 돼지를 붙이지 않는다`() {
        val item = state(entry(rank = 2, currentLossStreak = 4, losses = 18))

        assertEquals("18패", item.podiumCaptionText)
        assertFalse(item.podiumCaptionText.contains("연패"))
    }

    @Test
    fun `3위는 연패 중이어도 포디움 캡션에 돼지를 붙이지 않는다`() {
        val item = state(entry(rank = 3, currentLossStreak = 2, losses = 12))

        assertEquals("12패", item.podiumCaptionText)
        assertFalse(item.podiumCaptionText.contains("연패"))
    }

    @Test
    fun `연패가 0 이면 연패 뱃지를 그리지 않는다`() {
        val item = state(entry(currentLossStreak = 0))

        assertFalse(item.hasStreakBadge)
    }

    @Test
    fun `연패 중이면 뱃지를 그리고 연패 수를 찍는다`() {
        val item = state(entry(currentLossStreak = 3))

        assertTrue(item.hasStreakBadge)
        assertEquals("🐷3", item.streakBadgeText)
    }

    @Test
    fun `1위 rank 숫자는 FIRST 로 강조한다`() {
        assertEquals(RankEmphasis.FIRST, state(entry(rank = 1)).rankEmphasis)
    }

    @Test
    fun `3위 rank 숫자는 THIRD 로 강조한다`() {
        assertEquals(RankEmphasis.THIRD, state(entry(rank = 3)).rankEmphasis)
    }

    /**
     * 2위가 PLAIN 인 건 누락이 아니라 의도다 — 강조 축이 1위·3위 두 자리뿐이라
     * 포디움 3자리와 강조 3색이 1대1로 대응하지 않는다.
     */
    @Test
    fun `2위 rank 숫자는 강조하지 않고 PLAIN 이다`() {
        assertEquals(RankEmphasis.PLAIN, state(entry(rank = 2)).rankEmphasis)
    }

    @Test
    fun `4위 rank 숫자는 PLAIN 이다`() {
        assertEquals(RankEmphasis.PLAIN, state(entry(rank = 4)).rankEmphasis)
    }

    @Test
    fun `3위까지는 isTopThree 가 true 이고 4위부터 false 다`() {
        assertTrue(state(entry(rank = 1)).isTopThree)
        assertTrue(state(entry(rank = 3)).isTopThree)
        assertFalse(state(entry(rank = 4)).isTopThree)
    }
}
