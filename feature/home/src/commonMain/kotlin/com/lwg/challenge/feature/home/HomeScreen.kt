package com.lwg.challenge.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.ActiveChallenge
import com.lwg.challenge.domain.model.ActiveChallengeStatus
import com.lwg.challenge.domain.model.MyChallengeInfo
import com.lwg.challenge.domain.model.OpponentChallengeInfo
import com.lwg.challenge.domain.model.UserRecord
import com.lwg.challenge.domain.model.VerificationStatus
import com.lwg.challenge.domain.model.challenge.ChallengeCardInfo
import com.lwg.challenge.domain.model.challenge.ChallengeOutcome
import com.lwg.challenge.domain.model.challenge.ReceivedChallenge
import com.lwg.challenge.domain.model.challenge.ReceivedOpponentInfo
import com.lwg.challenge.feature.home.component.NoChallengeSection
import com.lwg.challenge.feature.home.component.StatsBar
import com.lwg.challenge.feature.home.component.receivedChallengesSection
import com.lwg.challenge.feature.home.contract.HomeUiState
import com.lwg.challenge.ui.components.card.ChallengeCard
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun HomeScreen(
    uiState: HomeUiState.Data,
    onChallengeClick: (Long) -> Unit,
    onAddFriendClick: () -> Unit,
    onCreateChallengeClick: () -> Unit,
    onAcceptClick: (ReceivedChallenge) -> Unit,
    onRejectClick: (ReceivedChallenge) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 16.dp,
            bottom = 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "record") {
            StatsBar(
                win = uiState.record.win,
                lose = uiState.record.lose,
                draw = uiState.record.draw,
                streakText = uiState.streakText,
                muted = uiState.isRecordMuted,
            )
        }

        if (uiState.receivedChallenges.isNotEmpty()) {
            receivedChallengesSection(
                challenges = uiState.receivedChallenges,
                deadlineTextOf = uiState::deadlineTextOf,
                isUrgent = uiState::isUrgent,
                isActionable = uiState::isActionable,
                onAccept = onAcceptClick,
                onReject = onRejectClick,
            )
        }
        challengesSection(
            uiState = uiState,
            onChallengeClick = onChallengeClick,
            onAddFriendClick = onAddFriendClick,
            onCreateChallengeClick = onCreateChallengeClick,
        )
        if (uiState.completedChallenges.isNotEmpty()) {
            completedChallengesSection(
                challenges = uiState.completedChallenges,
                onChallengeClick = onChallengeClick,
            )
        }
    }
}

private fun LazyListScope.challengesSection(
    uiState: HomeUiState.Data,
    onChallengeClick: (Long) -> Unit,
    onAddFriendClick: () -> Unit,
    onCreateChallengeClick: () -> Unit,
) {
    item(key = "challenges-section-title") {
        SectionTitleRow(title = "진행 중인 챌린지", count = uiState.inProgressChallenges.size)
    }
    if (!uiState.isInProgressEmpty) {
        items(
            items = uiState.inProgressChallenges,
            key = { it.challengeId },
        ) { challenge ->
            ChallengeCard(
                cardInfo = ChallengeCardInfo(challenge),
                deadlineTimeText = uiState.deadlineTextOf(challenge),
                onClick = { onChallengeClick(challenge.challengeId) },
            )
        }
    } else {
        item(key = "challenges-empty-state") {
            NoChallengeSection(
                onClickAddFriend = onAddFriendClick,
                onClickCreateChallenge = onCreateChallengeClick,
            )
        }
    }
}

private fun LazyListScope.completedChallengesSection(
    challenges: List<ActiveChallenge>,
    onChallengeClick: (Long) -> Unit,
) {
    item(key = "completed-challenges-section-title") {
        SectionTitleRow(title = "최근 결과", count = challenges.size)
    }
    items(
        items = challenges,
        key = { it.challengeId },
    ) { challenge ->
        ChallengeCard(
            cardInfo = ChallengeCardInfo(challenge),
            onClick = { onChallengeClick(challenge.challengeId) },
        )
    }
}

@Composable
private fun SectionTitleRow(title: String, count: Int) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = ChallengeTheme.typography.bold18,
            color = ChallengeTheme.colorScheme.onBackground,
        )
        Text(
            text = "${count}개",
            style = ChallengeTheme.typography.medium10,
            color = ChallengeTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp),
        )
    }
}

private val previewChallenges = listOf(
    ActiveChallenge(
        challengeId = 1L,
        me = MyChallengeInfo(
            mission = "오늘 운동 1시간 하기",
            verificationStatus = VerificationStatus.VERIFIED,
        ),
        opponent = OpponentChallengeInfo(
            nickname = "우건",
            mission = "책 30페이지 읽기",
            verificationStatus = VerificationStatus.PENDING,
        ),
        deadline = LocalDateTime.parse("2026-06-27T10:00:00"),
        penalty = "커피 사기 ☕",
        status = ActiveChallengeStatus.IN_PROGRESS,
        outcome = null,
    ),
    ActiveChallenge(
        challengeId = 2L,
        me = MyChallengeInfo(
            mission = "물 2L 마시기",
            verificationStatus = VerificationStatus.PENDING,
        ),
        opponent = OpponentChallengeInfo(
            nickname = "민지",
            mission = "10000보 걷기",
            verificationStatus = VerificationStatus.FAILED,
        ),
        deadline = LocalDateTime.parse("2026-06-27T22:00:00"),
        penalty = "점심 사기 🍚",
        status = ActiveChallengeStatus.IN_PROGRESS,
        outcome = null,
    ),
)

private val previewCompletedChallenges = listOf(
    ActiveChallenge(
        challengeId = 3L,
        me = MyChallengeInfo(
            mission = "아침 6시 기상하기",
            verificationStatus = VerificationStatus.VERIFIED,
        ),
        opponent = OpponentChallengeInfo(
            nickname = "도윤",
            mission = "영어 단어 30개 외우기",
            verificationStatus = VerificationStatus.FAILED,
        ),
        deadline = LocalDateTime.parse("2026-06-26T23:59:00"),
        penalty = "치킨 사기 🍗",
        status = ActiveChallengeStatus.COMPLETED,
        outcome = ChallengeOutcome.WIN,
    ),
    ActiveChallenge(
        challengeId = 4L,
        me = MyChallengeInfo(
            mission = "야식 참기",
            verificationStatus = VerificationStatus.FAILED,
        ),
        opponent = OpponentChallengeInfo(
            nickname = "지훈",
            mission = "일기 쓰기",
            verificationStatus = VerificationStatus.FAILED,
        ),
        deadline = LocalDateTime.parse("2026-06-25T23:59:00"),
        penalty = "떡볶이 사기 🍢",
        status = ActiveChallengeStatus.COMPLETED,
        outcome = ChallengeOutcome.BOTH_LOSE,
    ),
)

private val previewNow = LocalDateTime.parse("2026-06-27T09:00:00")

private val previewReceivedChallenges = listOf(
    ReceivedChallenge(
        challengeId = 501L,
        opponent = ReceivedOpponentInfo(
            id = 3L,
            nickname = "도윤",
            profileImageUrl = null,
            mission = "아침 6시 기상하기",
        ),
        penalty = "치킨 사기 🍗",
        deadline = LocalDateTime.parse("2026-06-27T09:40:00"),
        createdAt = LocalDateTime.parse("2026-06-27T08:00:00"),
    ),
    ReceivedChallenge(
        challengeId = 502L,
        opponent = ReceivedOpponentInfo(
            id = 4L,
            nickname = "민지",
            profileImageUrl = null,
            mission = "10000보 걷기",
        ),
        penalty = "커피 사기 ☕",
        deadline = LocalDateTime.parse("2026-06-28T12:00:00"),
        createdAt = LocalDateTime.parse("2026-06-27T07:00:00"),
    ),
)

@Preview
@Composable
private fun HomeScreenDataPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            HomeScreen(
                uiState = HomeUiState.Data(
                    record = UserRecord(win = 12, lose = 3, draw = 1, currentStreak = 4),
                    challenges = previewChallenges,
                    now = previewNow,
                ),
                onChallengeClick = {},
                onAddFriendClick = {},
                onCreateChallengeClick = {},
                onAcceptClick = {},
                onRejectClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun HomeScreenNoRecordPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            HomeScreen(
                uiState = HomeUiState.Data(
                    record = UserRecord.Empty,
                    challenges = emptyList(),
                    now = previewNow,
                ),
                onChallengeClick = {},
                onAddFriendClick = {},
                onCreateChallengeClick = {},
                onAcceptClick = {},
                onRejectClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun HomeScreenNoActiveChallengePreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            HomeScreen(
                uiState = HomeUiState.Data(
                    record = UserRecord(win = 8, lose = 5, draw = 2, currentStreak = 0),
                    challenges = emptyList(),
                    now = previewNow,
                ),
                onChallengeClick = {},
                onAddFriendClick = {},
                onCreateChallengeClick = {},
                onAcceptClick = {},
                onRejectClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun HomeScreenWithCompletedChallengesPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            HomeScreen(
                uiState = HomeUiState.Data(
                    record = UserRecord(win = 12, lose = 3, draw = 1, currentStreak = 4),
                    challenges = previewChallenges + previewCompletedChallenges,
                    now = previewNow,
                ),
                onChallengeClick = {},
                onAddFriendClick = {},
                onCreateChallengeClick = {},
                onAcceptClick = {},
                onRejectClick = {},
            )
        }
    }
}

/**
 * 이번 변경의 핵심 장면 — 진행 중 0건이라 빈 상태 카드가 뜨고, 그 아래 최근 결과 구획이 함께 보인다.
 */
@Preview
@Composable
private fun HomeScreenNoActiveWithCompletedChallengesPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            HomeScreen(
                uiState = HomeUiState.Data(
                    record = UserRecord.Empty,
                    challenges = previewCompletedChallenges,
                    now = previewNow,
                ),
                onChallengeClick = {},
                onAddFriendClick = {},
                onCreateChallengeClick = {},
                onAcceptClick = {},
                onRejectClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun HomeScreenWithReceivedChallengesPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            HomeScreen(
                uiState = HomeUiState.Data(
                    record = UserRecord(win = 12, lose = 3, draw = 1, currentStreak = 4),
                    challenges = previewChallenges,
                    now = previewNow,
                    receivedChallenges = previewReceivedChallenges,
                ),
                onChallengeClick = {},
                onAddFriendClick = {},
                onCreateChallengeClick = {},
                onAcceptClick = {},
                onRejectClick = {},
            )
        }
    }
}

/** 벨 뱃지가 켜진 장면 — 붉은 점이 벨 아이콘 어디에 찍히는지 눈으로 확인하는 자리다. */
@Preview
@Composable
private fun HomeScreenUnreadNotificationPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            HomeScreen(
                uiState = HomeUiState.Data(
                    record = UserRecord(win = 12, lose = 3, draw = 1, currentStreak = 4),
                    challenges = previewChallenges,
                    now = previewNow,
                    unreadCount = 3,
                ),
                onChallengeClick = {},
                onAddFriendClick = {},
                onCreateChallengeClick = {},
                onAcceptClick = {},
                onRejectClick = {},
            )
        }
    }
}
