package com.lwg.challenge.feature.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.challenge.ReceivedChallenge
import com.lwg.challenge.domain.model.challenge.ReceivedOpponentInfo
import com.lwg.challenge.domain.model.UserRecord
import com.lwg.challenge.feature.home.contract.HomeUiState
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 받은 도전장 섹션.
 */
internal fun LazyListScope.receivedChallengesSection(
    challenges: List<ReceivedChallenge>,
    deadlineTextOf: (ReceivedChallenge) -> String,
    isUrgent: (ReceivedChallenge) -> Boolean,
    isActionable: (ReceivedChallenge) -> Boolean,
    onAccept: (ReceivedChallenge) -> Unit,
    onReject: (ReceivedChallenge) -> Unit,
) {
    item(key = "received-challenges-section-title") {
        ReceivedChallengesHeader(count = challenges.size)
    }
    items(
        items = challenges,
        key = { "received-${it.challengeId}" },
    ) { challenge ->
        ReceivedChallengeCard(
            challenge = challenge,
            deadlineText = deadlineTextOf(challenge),
            isUrgent = isUrgent(challenge),
            onAccept = { onAccept(challenge) },
            onReject = { onReject(challenge) },
            actionsEnabled = isActionable(challenge),
        )
    }
}

@Composable
internal fun ReceivedChallengesHeader(
    count: Int,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        Text(
            text = "받은 도전장 ",
            style = ChallengeTheme.typography.bold14,
            color = ChallengeTheme.colorScheme.onBackground,
        )
        Text(
            text = "${count}건",
            style = ChallengeTheme.typography.bold14,
            color = ChallengeTheme.colorScheme.primary,
        )
    }
}

@Preview
@Composable
private fun ReceivedChallengesHeaderPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            ReceivedChallengesHeader(count = 2)
        }
    }
}

@Preview
@Composable
private fun ReceivedChallengesSectionPreview() {
    ChallengeTheme {
        LazyColumn(
            modifier = Modifier.background(ChallengeTheme.colorScheme.background),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val state = previewState()
            receivedChallengesSection(
                challenges = state.receivedChallenges,
                deadlineTextOf = state::deadlineTextOf,
                isUrgent = state::isUrgent,
                isActionable = state::isActionable,
                onAccept = {},
                onReject = {},
            )
        }
    }
}

private fun previewState(): HomeUiState.Data {
    val now = LocalDateTime.parse("2026-06-27T09:00:00")
    val challenges = listOf(
        ReceivedChallenge(
            challengeId = 501L,
            opponent = ReceivedOpponentInfo(
                id = 3L,
                nickname = "도윤",
                profileImageUrl = null,
                mission = "아침 6시 기상하기",
            ),
            penalty = "치킨 사기 🍗",
            deadline = LocalDateTime.parse("2026-06-27T14:00:00"),
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
            deadline = LocalDateTime.parse("2026-06-27T09:42:00"),
            createdAt = LocalDateTime.parse("2026-06-27T07:00:00"),
        ),
    )
    return HomeUiState.Data(
        record = UserRecord.Empty,
        challenges = emptyList(),
        now = now,
        receivedChallenges = challenges,
    )
}
