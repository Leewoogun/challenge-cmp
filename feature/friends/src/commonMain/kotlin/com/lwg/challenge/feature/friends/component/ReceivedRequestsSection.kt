@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.lwg.challenge.feature.friends.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.friend.FriendRequestCard
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.friend.FriendRequest
import com.lwg.challenge.domain.model.friend.UserBrief
import com.lwg.challenge.feature.friends.contract.FriendRequestItemState
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 받은 요청 섹션. design.md §A.2 — 헤더 "받은 요청 N건"("N건" primary) + [FriendRequestCard] 리스트.
 */
@Composable
internal fun ReceivedRequestsSection(
    requests: List<FriendRequestItemState>,
    onAccept: (FriendRequestItemState) -> Unit,
    onReject: (FriendRequestItemState) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row {
            Text(
                text = "받은 요청 ",
                style = ChallengeTheme.typography.bold14,
                color = ChallengeTheme.colorScheme.onBackground,
            )
            Text(
                text = "${requests.size}건",
                style = ChallengeTheme.typography.bold14,
                color = ChallengeTheme.colorScheme.primary,
            )
        }

        requests.forEach { request ->
            FriendRequestCard(
                profileImageUrl = request.profileImageUrl,
                nickname = request.nickname,
                onAccept = { onAccept(request) },
                onReject = { onReject(request) },
            )
        }
    }
}

@Preview
@Composable
private fun ReceivedRequestsSectionPreview() {
    val now = Clock.System.now()
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            ReceivedRequestsSection(
                requests = listOf(
                    FriendRequestItemState(
                        FriendRequest(
                            requestId = 1,
                            fromUser = UserBrief(userId = 1, nickname = "도전왕", profileImageUrl = null),
                            requestedAt = now - 1.days,
                        ),
                        now,
                    ),
                    FriendRequestItemState(
                        FriendRequest(
                            requestId = 2,
                            fromUser = UserBrief(userId = 2, nickname = "우건", profileImageUrl = null),
                            requestedAt = now - 2.days,
                        ),
                        now,
                    ),
                ),
                onAccept = {},
                onReject = {},
            )
        }
    }
}
