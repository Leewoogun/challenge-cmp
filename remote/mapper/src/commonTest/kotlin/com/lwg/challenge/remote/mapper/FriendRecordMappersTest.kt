package com.lwg.challenge.remote.mapper

import com.lwg.challenge.domain.model.challenge.ChallengeOutcome
import com.lwg.challenge.domain.model.friend.FriendChallengeStatus
import com.lwg.challenge.domain.model.friend.FriendScore
import com.lwg.challenge.domain.model.friend.UserBrief
import com.lwg.challenge.remote.model.challenge.ChallengeOutcomeDto
import com.lwg.challenge.remote.model.friend.FriendChallengeStatusDto
import com.lwg.challenge.remote.model.friend.FriendChallengesData
import com.lwg.challenge.remote.model.friend.FriendChallengesResponse
import com.lwg.challenge.remote.model.friend.FriendOngoingChallengeDto
import com.lwg.challenge.remote.model.friend.FriendPastChallengeDto
import com.lwg.challenge.remote.model.friend.FriendRecordData
import com.lwg.challenge.remote.model.friend.FriendRecordFriendDto
import com.lwg.challenge.remote.model.friend.FriendRecordResponse
import com.lwg.challenge.remote.model.friend.FriendScoreDto
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class FriendRecordMappersTest {

    private val receivedPending = FriendOngoingChallengeDto(
        challengeId = 33L,
        status = FriendChallengeStatusDto.PENDING,
        sentByMe = false,
        deadline = LocalDateTime(2026, 11, 23, 0, 0, 0),
        myMission = null,
        friendMission = "물 2L 마시기",
        bet = "밥 사기",
    )

    private fun record(ongoing: List<FriendOngoingChallengeDto>) = FriendRecordResponse(
        FriendRecordData(
            friend = FriendRecordFriendDto(userId = 42L, nickname = "수아", profileImageUrl = null),
            score = FriendScoreDto(myWins = 2, friendWins = 1, draws = 1, bothLose = 1),
            ongoing = ongoing,
        ),
    )

    private fun past(challengeId: Long, myResult: ChallengeOutcomeDto) = FriendPastChallengeDto(
        challengeId = challengeId,
        challengeDate = LocalDate(2026, 11, 2),
        myMission = "운동 1시간",
        friendMission = "책 30페이지",
        bet = "커피 사기",
        myResult = myResult,
    )

    @Test
    fun `친구와 스코어 4값을 그대로 옮긴다`() {
        val result = record(emptyList()).toFriendRecord()

        assertEquals(UserBrief(userId = 42L, nickname = "수아", profileImageUrl = null), result.friend)
        assertEquals(FriendScore(myWins = 2, friendWins = 1, draws = 1, bothLose = 1), result.score)
    }

    @Test
    fun `받은 PENDING 은 sentByMe false 와 myMission null 이 유지된다`() {
        val ongoing = record(listOf(receivedPending)).toFriendRecord().ongoing.single()

        assertEquals(33L, ongoing.challengeId)
        assertEquals(FriendChallengeStatus.PENDING, ongoing.status)
        assertFalse(ongoing.sentByMe)
        assertNull(ongoing.myMission)
        assertEquals("물 2L 마시기", ongoing.friendMission)
        assertEquals(LocalDateTime(2026, 11, 23, 0, 0, 0), ongoing.deadline)
        assertEquals("밥 사기", ongoing.bet)
    }

    @Test
    fun `deadline 을 못 읽은 진행 중 항목만 제외하고 순서를 지킨다`() {
        val inProgress = receivedPending.copy(
            challengeId = 31L,
            status = FriendChallengeStatusDto.IN_PROGRESS,
            sentByMe = true,
            myMission = "책 한 권 읽기",
        )

        val ongoing = record(
            listOf(inProgress, receivedPending.copy(challengeId = 32L, deadline = null), receivedPending),
        ).toFriendRecord().ongoing

        assertEquals(listOf(31L, 33L), ongoing.map { it.challengeId })
        assertEquals(FriendChallengeStatus.IN_PROGRESS, ongoing[0].status)
    }

    @Test
    fun `myResult BOTH_LOSE 는 LOSE 로 접히지 않는다`() {
        val challenges = FriendChallengesResponse(
            FriendChallengesData(
                challenges = listOf(past(19L, ChallengeOutcomeDto.BOTH_LOSE), past(17L, ChallengeOutcomeDto.LOSE)),
            ),
        ).toFriendChallengePage().challenges

        assertEquals(ChallengeOutcome.BOTH_LOSE, challenges[0].myResult)
        assertEquals(ChallengeOutcome.LOSE, challenges[1].myResult)
    }

    /** 마지막 항목 id(17)로 바꿔 쓰면 다음 쪽 첫 항목이 중복된다. */
    @Test
    fun `nextChallengeId 와 isPageEnd 를 손대지 않고 통과시킨다`() {
        val page = FriendChallengesResponse(
            FriendChallengesData(
                challenges = listOf(past(19L, ChallengeOutcomeDto.WIN), past(17L, ChallengeOutcomeDto.DRAW)),
                nextChallengeId = 12L,
                isPageEnd = false,
            ),
        ).toFriendChallengePage()

        assertEquals(listOf(19L, 17L), page.challenges.map { it.challengeId })
        assertEquals(12L, page.nextChallengeId)
        assertFalse(page.isPageEnd)
        with(page.challenges[0]) {
            assertEquals(LocalDate(2026, 11, 2), challengeDate)
            assertEquals("운동 1시간", myMission)
            assertEquals("책 30페이지", friendMission)
            assertEquals("커피 사기", bet)
            assertEquals(ChallengeOutcome.WIN, myResult)
        }
    }
}
