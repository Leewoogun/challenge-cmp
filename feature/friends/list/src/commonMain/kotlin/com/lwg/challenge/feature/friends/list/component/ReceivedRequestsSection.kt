package com.lwg.challenge.feature.friends.list.component

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
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.friend.FriendRequest
import com.lwg.challenge.domain.model.friend.UserBrief
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.ui.tooling.preview.Preview

/** 받은 요청 섹션 — 헤더 "받은 요청 N건" + [FriendRequestCard] 리스트. */
@Composable
internal fun ReceivedRequestsSection(
    requests: List<FriendRequest>,
    onAccept: (FriendRequest) -> Unit,
    onReject: (FriendRequest) -> Unit,
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
                color = ChallengeTheme.colorScheme.text.primary,
            )
            Text(
                text = "${requests.size}건",
                style = ChallengeTheme.typography.bold14,
                color = ChallengeTheme.colorScheme.text.point,
            )
        }

        requests.forEach { request ->
            FriendRequestCard(
                profileImageUrl = request.fromUser.profileImageUrl,
                nickname = request.fromUser.nickname,
                onAccept = { onAccept(request) },
                onReject = { onReject(request) },
            )
        }
    }
}

@Preview
@Composable
private fun ReceivedRequestsSectionPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(20.dp),
        ) {
            ReceivedRequestsSection(
                requests = listOf(
                    FriendRequest(
                        requestId = 1,
                        fromUser = UserBrief(userId = 1, nickname = "도전왕", profileImageUrl = null),
                        requestedAt = LocalDateTime.parse("2026-06-26T12:00:00"),
                    ),
                    FriendRequest(
                        requestId = 2,
                        fromUser = UserBrief(userId = 2, nickname = "우건", profileImageUrl = null),
                        requestedAt = LocalDateTime.parse("2026-06-25T12:00:00"),
                    ),
                ),
                onAccept = {},
                onReject = {},
            )
        }
    }
}
