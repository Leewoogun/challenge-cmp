package com.lwg.challenge.feature.friends.record

import com.lwg.challenge.domain.model.challenge.ChallengeOutcome
import com.lwg.challenge.domain.model.friend.FriendChallengeStatus
import com.lwg.challenge.domain.model.friend.FriendOngoingChallenge
import com.lwg.challenge.domain.model.friend.FriendPastChallenge
import com.lwg.challenge.domain.model.friend.FriendRecord
import com.lwg.challenge.domain.model.friend.FriendScore
import com.lwg.challenge.domain.model.friend.UserBrief
import com.lwg.challenge.feature.friends.record.contract.FriendRecordUiState
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/** 프리뷰 기준 시각. 진행 중 카드의 남은 시간이 이 값에서 나온다. */
internal val PreviewFriendRecordNow: LocalDateTime = LocalDateTime(2026, 11, 17, 13, 0)

internal val PreviewFriend: UserBrief = UserBrief(userId = 42L, nickname = "민수", profileImageUrl = null)

internal fun friendOngoingPreviewItem(
    challengeId: Long = 31L,
    status: FriendChallengeStatus = FriendChallengeStatus.IN_PROGRESS,
    sentByMe: Boolean = true,
    deadline: LocalDateTime = LocalDateTime(2026, 11, 18, 0, 0),
    myMission: String? = "책 한 권 읽기",
    friendMission: String? = "러닝 5km",
    bet: String = "커피 사기 ☕",
): FriendOngoingChallenge = FriendOngoingChallenge(
    challengeId = challengeId,
    status = status,
    sentByMe = sentByMe,
    deadline = deadline,
    myMission = myMission,
    friendMission = friendMission,
    bet = bet,
)

internal val PreviewInProgressChallenge: FriendOngoingChallenge = friendOngoingPreviewItem()

/** 받은 도전장 — 내 미션은 수락 때 정한다. 마감 1시간 이내라 남은 시간이 error 색이다. */
internal val PreviewReceivedChallenge: FriendOngoingChallenge = friendOngoingPreviewItem(
    challengeId = 33L,
    status = FriendChallengeStatus.PENDING,
    sentByMe = false,
    deadline = LocalDateTime(2026, 11, 17, 13, 40),
    myMission = null,
    friendMission = "물 2L 마시기",
    bet = "밥 사기 🍚",
)

/** 보낸 도전장 — 친구 미션이 아직 없다. */
internal val PreviewSentChallenge: FriendOngoingChallenge = friendOngoingPreviewItem(
    challengeId = 35L,
    status = FriendChallengeStatus.PENDING,
    sentByMe = true,
    deadline = LocalDateTime(2026, 11, 20, 0, 0),
    myMission = "새벽 6시 기상",
    friendMission = null,
)

internal fun friendPastPreviewItem(
    challengeId: Long,
    myResult: ChallengeOutcome,
    challengeDate: LocalDate = LocalDate(2026, 11, 12),
): FriendPastChallenge = FriendPastChallenge(
    challengeId = challengeId,
    challengeDate = challengeDate,
    myMission = "운동 1시간",
    friendMission = "책 30페이지",
    bet = "커피 사기 ☕",
    myResult = myResult,
)

/** 결과 4값이 한 번씩 나온다. 서버 순서(id DESC)라 날짜가 거꾸로 놓인 행도 있다. */
internal val PreviewPastChallenges: List<FriendPastChallenge> = listOf(
    friendPastPreviewItem(19L, ChallengeOutcome.WIN, LocalDate(2026, 11, 12)),
    friendPastPreviewItem(17L, ChallengeOutcome.BOTH_LOSE, LocalDate(2026, 11, 14)),
    friendPastPreviewItem(15L, ChallengeOutcome.LOSE, LocalDate(2026, 11, 2)),
    friendPastPreviewItem(12L, ChallengeOutcome.DRAW, LocalDate(2026, 10, 28)),
    friendPastPreviewItem(9L, ChallengeOutcome.WIN, LocalDate(2026, 10, 20)),
)

internal fun friendRecordPreviewData(
    score: FriendScore = FriendScore(myWins = 2, friendWins = 1, draws = 1, bothLose = 1),
    ongoing: List<FriendOngoingChallenge> = listOf(PreviewReceivedChallenge, PreviewInProgressChallenge, PreviewSentChallenge),
    pastChallenges: List<FriendPastChallenge> = PreviewPastChallenges,
    isPageEnd: Boolean = true,
): FriendRecordUiState.Data = FriendRecordUiState.Data(
    record = FriendRecord(friend = PreviewFriend, score = score, ongoing = ongoing),
    pastChallenges = pastChallenges,
    isPageEnd = isPageEnd,
    now = PreviewFriendRecordNow,
)
