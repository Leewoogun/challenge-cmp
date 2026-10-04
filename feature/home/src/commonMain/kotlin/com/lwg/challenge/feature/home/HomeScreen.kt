package com.lwg.challenge.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.ChallengeScaffold
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.ActiveChallenge
import com.lwg.challenge.domain.model.ActiveChallengeStatus
import com.lwg.challenge.domain.model.MyChallengeInfo
import com.lwg.challenge.domain.model.OpponentChallengeInfo
import com.lwg.challenge.domain.model.UserRecord
import com.lwg.challenge.domain.model.VerificationStatus
import com.lwg.challenge.domain.model.challenge.ChallengeOutcome
import com.lwg.challenge.domain.model.challenge.ReceivedChallenge
import com.lwg.challenge.feature.home.component.ChallengeCard
import com.lwg.challenge.feature.home.component.HomeEmptyState
import com.lwg.challenge.feature.home.component.HomeEmptyStateType
import com.lwg.challenge.feature.home.component.HomeTopBar
import com.lwg.challenge.feature.home.component.StatsBar
import com.lwg.challenge.feature.home.component.receivedChallengesSection
import com.lwg.challenge.feature.home.contract.HomeEmptyType
import com.lwg.challenge.feature.home.contract.HomeUiState
import com.lwg.challenge.feature.home.contract.ReceivedChallengeItemState
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun HomeScreen(
    uiState: HomeUiState.Data,
    onBellClick: () -> Unit,
    onFabClick: () -> Unit,
    onChallengeClick: (Long) -> Unit,
    onAddFriendClick: () -> Unit,
    onCreateChallengeClick: () -> Unit,
    onAcceptClick: (ReceivedChallengeItemState) -> Unit,
    onRejectClick: (ReceivedChallengeItemState) -> Unit,
) {
    ChallengeScaffold(
        statusBarColor = ChallengeTheme.colorScheme.surface,
        topBar = {
            HomeTopBar(
                hasUnreadNotification = uiState.hasUnreadNotification,
                onBellClick = onBellClick,
                debugAccountLabel = uiState.debugAccountLabel,
            )
        },
        floatingActionButton = {
            HomeFab(onClick = onFabClick)
        },
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
            // 0건이면 섹션 통째로 미노출 — 전용 빈 상태 카드를 두지 않는다 (design.md §2.4).
            if (uiState.receivedChallenges.isNotEmpty()) {
                receivedChallengesSection(
                    challenges = uiState.receivedChallenges,
                    onAccept = onAcceptClick,
                    onReject = onRejectClick,
                    isActionable = uiState::isActionable,
                )
            }
            challengesSection(
                uiState = uiState,
                onChallengeClick = onChallengeClick,
                onAddFriendClick = onAddFriendClick,
                onCreateChallengeClick = onCreateChallengeClick,
            )
            // 진행 중 빈 상태 카드가 떠 있어도 그 아래 이어 붙는다 — 진행 중은 0건인데 최근 결과는
            // 있는 조합이 정상이라, 빈 상태와 이 구획은 서로를 가리지 않는다.
            if (uiState.completedChallenges.isNotEmpty()) {
                completedChallengesSection(
                    challenges = uiState.completedChallenges,
                    onChallengeClick = onChallengeClick,
                )
            }
        }
    }
}

@Composable
internal fun HomeLoadingScreen(
    onBellClick: () -> Unit,
    onFabClick: () -> Unit,
) {
    ChallengeScaffold(
        statusBarColor = ChallengeTheme.colorScheme.surface,
        topBar = {
            HomeTopBar(
                // 하드코딩이 아니라 "아직 모른다" 의 표현이다. 개수를 받기 전에 켰다가 응답이 0이면
                // 점이 깜빡 켜졌다 꺼진다 — 로딩 중에는 끈 채로 두고 Data 전이 때 한 번에 켠다.
                hasUnreadNotification = false,
                onBellClick = onBellClick,
            )
        },
        floatingActionButton = {
            HomeFab(onClick = onFabClick)
        },
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = ChallengeTheme.colorScheme.primary)
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
    if (uiState.emptyType == HomeEmptyType.NONE) {
        items(
            items = uiState.inProgressChallenges,
            key = { it.challengeId },
        ) { challenge ->
            ChallengeCard(
                challengeId = challenge.challengeId,
                myMission = challenge.me.mission,
                opponentNickname = challenge.opponent.nickname,
                opponentMission = challenge.opponent.mission,
                remainingText = uiState.deadlineTextOf(challenge),
                myVerificationStatus = challenge.me.verificationStatus,
                opponentVerificationStatus = challenge.opponent.verificationStatus,
                penalty = challenge.penalty,
                onClick = { onChallengeClick(challenge.challengeId) },
                outcome = null,
            )
        }
    } else {
        item(key = "challenges-empty-state") {
            HomeEmptyState(
                type = uiState.emptyType.toDesignSystem(),
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
        // "최근 결과" 는 시안에 없는 새 문구다. 전용 디자인이 없으므로 진행 중 구획과 같은 규격을
        // 그대로 써서 두 구획이 같은 위계로 읽히게 한다.
        SectionTitleRow(title = "최근 결과", count = challenges.size)
    }
    items(
        items = challenges,
        key = { it.challengeId },
    ) { challenge ->
        ChallengeCard(
            challengeId = challenge.challengeId,
            myMission = challenge.me.mission,
            opponentNickname = challenge.opponent.nickname,
            opponentMission = challenge.opponent.mission,
            myVerificationStatus = challenge.me.verificationStatus,
            opponentVerificationStatus = challenge.opponent.verificationStatus,
            penalty = challenge.penalty,
            onClick = { onChallengeClick(challenge.challengeId) },
            outcome = challenge.outcome,
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

@Composable
private fun HomeFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        containerColor = ChallengeTheme.colorScheme.primary,
        contentColor = ChallengeTheme.colorScheme.onPrimary,
        modifier = Modifier.size(56.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "챌린지 만들기",
            modifier = Modifier.size(28.dp),
        )
    }
}

private fun HomeEmptyType.toDesignSystem(): HomeEmptyStateType = when (this) {
    HomeEmptyType.FIRST_USER -> HomeEmptyStateType.FIRST_USER
    HomeEmptyType.NO_ACTIVE_CHALLENGE -> HomeEmptyStateType.NO_ACTIVE_CHALLENGE
    HomeEmptyType.NONE -> error("HomeEmptyType.NONE 는 HomeEmptyState 진입 전에 분기됨")
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
    ReceivedChallengeItemState(
        model = ReceivedChallenge(
            challengeId = 501L,
            challengerId = 3L,
            challengerNickname = "도윤",
            challengerProfileImageUrl = null,
            challengerMission = "아침 6시 기상하기",
            betContent = "치킨 사기 🍗",
            deadline = LocalDateTime.parse("2026-06-27T09:40:00"),
            createdAt = LocalDateTime.parse("2026-06-27T08:00:00"),
        ),
        now = previewNow,
    ),
    ReceivedChallengeItemState(
        model = ReceivedChallenge(
            challengeId = 502L,
            challengerId = 4L,
            challengerNickname = "민지",
            challengerProfileImageUrl = null,
            challengerMission = "10000보 걷기",
            betContent = "커피 사기 ☕",
            deadline = LocalDateTime.parse("2026-06-28T12:00:00"),
            createdAt = LocalDateTime.parse("2026-06-27T07:00:00"),
        ),
        now = previewNow,
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
                onBellClick = {},
                onFabClick = {},
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
private fun HomeScreenFirstUserPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            HomeScreen(
                uiState = HomeUiState.Data(
                    record = UserRecord.Empty,
                    challenges = emptyList(),
                    now = previewNow,
                ),
                onBellClick = {},
                onFabClick = {},
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
                onBellClick = {},
                onFabClick = {},
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
                onBellClick = {},
                onFabClick = {},
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
 *
 * 전적까지 0이지만 판정된 결과가 있어 [HomeEmptyType.FIRST_USER] 로 떨어지지 않는 조합도 겸한다.
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
                onBellClick = {},
                onFabClick = {},
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
                onBellClick = {},
                onFabClick = {},
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
                onBellClick = {},
                onFabClick = {},
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
private fun HomeLoadingScreenPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            HomeLoadingScreen(
                onBellClick = {},
                onFabClick = {},
            )
        }
    }
}
