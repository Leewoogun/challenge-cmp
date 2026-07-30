@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.lwg.challenge.feature.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.challenge.ReceivedChallenge
import com.lwg.challenge.feature.home.contract.ReceivedChallengeItemState
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 받은 도전장 섹션. design.md §2.2 / §4.4.
 *
 * 홈이 이미 `LazyColumn` 이라 중첩 `LazyColumn` 대신 [LazyListScope] 로 펼친다.
 * 빈 목록이면 호출부가 아예 호출하지 않는다 — 전용 빈 상태 카드를 두지 않는다.
 */
internal fun LazyListScope.receivedChallengesSection(
    challenges: List<ReceivedChallengeItemState>,
    onAccept: (ReceivedChallengeItemState) -> Unit,
    onReject: (ReceivedChallengeItemState) -> Unit,
    isActionable: (ReceivedChallengeItemState) -> Boolean,
) {
    item(key = "received-challenges-section-title") {
        ReceivedChallengesHeader(count = challenges.size)
    }
    items(
        items = challenges,
        key = { "received-${it.challengeId}" },
    ) { item ->
        ReceivedChallengeCard(
            challengerNickname = item.challengerNickname,
            challengerProfileImageUrl = item.challengerProfileImageUrl,
            titleText = item.titleText,
            deadlineText = item.deadlineText,
            isUrgent = item.isUrgent,
            missionLabelText = item.missionLabelText,
            challengerMission = item.challengerMission,
            betText = item.betText,
            onAccept = { onAccept(item) },
            onReject = { onReject(item) },
            actionsEnabled = isActionable(item),
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

internal fun previewReceivedChallenges(): List<ReceivedChallengeItemState> {
    val now = Clock.System.now()
    return listOf(
        ReceivedChallengeItemState(
            ReceivedChallenge(
                challengeId = 501L,
                challengerId = 3L,
                challengerNickname = "도윤",
                challengerProfileImageUrl = null,
                challengerMission = "아침 6시 기상하기",
                betContent = "치킨 사기 🍗",
                deadline = now + 5.hours,
                createdAt = now - 1.hours,
            ),
            now,
        ),
        ReceivedChallengeItemState(
            ReceivedChallenge(
                challengeId = 502L,
                challengerId = 4L,
                challengerNickname = "민지",
                challengerProfileImageUrl = null,
                challengerMission = "10000보 걷기",
                betContent = "커피 사기 ☕",
                deadline = now + 42.minutes,
                createdAt = now - 2.hours,
            ),
            now,
        ),
    )
}
