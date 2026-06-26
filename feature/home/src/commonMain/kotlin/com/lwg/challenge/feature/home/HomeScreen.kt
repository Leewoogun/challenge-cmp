@file:OptIn(kotlin.time.ExperimentalTime::class)

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
import com.lwg.challenge.designsystem.components.challenge.ChallengeCard
import com.lwg.challenge.designsystem.components.challenge.ChallengeVerificationStatus
import com.lwg.challenge.designsystem.components.challenge.HomeEmptyState
import com.lwg.challenge.designsystem.components.challenge.HomeEmptyStateType
import com.lwg.challenge.designsystem.components.challenge.StatsBar
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.ActiveChallenge
import com.lwg.challenge.domain.model.UserRecord
import com.lwg.challenge.domain.model.VerificationStatus
import com.lwg.challenge.feature.home.component.HomeTopBar
import com.lwg.challenge.feature.home.contract.HomeEmptyType
import com.lwg.challenge.feature.home.contract.HomeUiState
import kotlin.time.Instant
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun HomeScreen(
    uiState: HomeUiState.Data,
    onBellClick: () -> Unit,
    onFabClick: () -> Unit,
    onChallengeClick: (Long) -> Unit,
    onAddFriendClick: () -> Unit,
    onCreateChallengeClick: () -> Unit,
) {
    ChallengeScaffold(
        statusBarColor = ChallengeTheme.colorScheme.surface,
        topBar = {
            HomeTopBar(
                hasUnreadNotification = false,
                onBellClick = onBellClick,
            )
        },
        floatingActionButton = {
            HomeFab(onClick = onFabClick)
        },
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
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
                        currentStreak = uiState.record.currentStreak,
                    )
                }
                challengesSection(
                    uiState = uiState,
                    onChallengeClick = onChallengeClick,
                    onAddFriendClick = onAddFriendClick,
                    onCreateChallengeClick = onCreateChallengeClick,
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
        SectionTitleRow(count = uiState.challenges.size)
    }
    if (uiState.emptyType == HomeEmptyType.NONE) {
        items(
            items = uiState.challenges,
            key = { it.challengeId },
        ) { challenge ->
            ChallengeCard(
                challengeId = challenge.challengeId,
                myMission = challenge.myMission,
                opponentNickname = challenge.opponentNickname,
                opponentMission = challenge.opponentMission,
                deadline = challenge.deadline,
                myVerificationStatus = challenge.myVerificationStatus.toDesignSystem(),
                opponentVerificationStatus = challenge.opponentVerificationStatus.toDesignSystem(),
                bet = challenge.bet,
                onClick = { onChallengeClick(challenge.challengeId) },
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

@Composable
private fun SectionTitleRow(count: Int) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "진행 중인 챌린지",
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

private fun VerificationStatus.toDesignSystem(): ChallengeVerificationStatus = when (this) {
    VerificationStatus.PENDING -> ChallengeVerificationStatus.PENDING
    VerificationStatus.VERIFIED -> ChallengeVerificationStatus.VERIFIED
    VerificationStatus.FAILED -> ChallengeVerificationStatus.FAILED
}

private fun HomeEmptyType.toDesignSystem(): HomeEmptyStateType = when (this) {
    HomeEmptyType.FIRST_USER -> HomeEmptyStateType.FIRST_USER
    HomeEmptyType.NO_ACTIVE_CHALLENGE -> HomeEmptyStateType.NO_ACTIVE_CHALLENGE
    HomeEmptyType.NONE -> error("HomeEmptyType.NONE 는 HomeEmptyState 진입 전에 분기됨")
}

private val previewChallenges = listOf(
    ActiveChallenge(
        challengeId = 1L,
        myMission = "오늘 운동 1시간 하기",
        opponentNickname = "우건",
        opponentMission = "책 30페이지 읽기",
        deadline = Instant.parse("2026-06-27T10:00:00Z"),
        myVerificationStatus = VerificationStatus.VERIFIED,
        opponentVerificationStatus = VerificationStatus.PENDING,
        bet = "커피 사기 ☕",
    ),
    ActiveChallenge(
        challengeId = 2L,
        myMission = "물 2L 마시기",
        opponentNickname = "민지",
        opponentMission = "10000보 걷기",
        deadline = Instant.parse("2026-06-27T22:00:00Z"),
        myVerificationStatus = VerificationStatus.PENDING,
        opponentVerificationStatus = VerificationStatus.FAILED,
        bet = "점심 사기 🍚",
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
                ),
                onBellClick = {},
                onFabClick = {},
                onChallengeClick = {},
                onAddFriendClick = {},
                onCreateChallengeClick = {},
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
                ),
                onBellClick = {},
                onFabClick = {},
                onChallengeClick = {},
                onAddFriendClick = {},
                onCreateChallengeClick = {},
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
                ),
                onBellClick = {},
                onFabClick = {},
                onChallengeClick = {},
                onAddFriendClick = {},
                onCreateChallengeClick = {},
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
