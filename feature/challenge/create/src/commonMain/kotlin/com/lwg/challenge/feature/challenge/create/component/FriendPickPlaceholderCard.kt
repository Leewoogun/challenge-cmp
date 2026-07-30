package com.lwg.challenge.feature.challenge.create.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.IconTextButton
import com.lwg.challenge.designsystem.components.VerticalSpacer
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * step0 의 빈 상태 / 로드 실패 카드. design.md §1.2.
 *
 * `:feature:friends:list` 의 `FriendsEmptyState` 는 internal 이라 재사용할 수 없어 별도로 둔다.
 *
 * @param ctaIcon 빈 상태는 친구 추가, 실패는 재시도 — 아이콘까지 호출부가 정한다.
 */
@Composable
internal fun FriendPickPlaceholderCard(
    title: String,
    subtitle: String,
    ctaLabel: String,
    ctaIcon: ImageVector,
    onClickCta: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val primary = ChallengeTheme.colorScheme.primary

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ChallengeTheme.colorScheme.surface,
        border = BorderStroke(width = 1.dp, color = ChallengeTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(primary.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Group,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = primary,
                )
            }

            VerticalSpacer(16.dp)

            Text(
                text = title,
                style = ChallengeTheme.typography.bold16,
                color = ChallengeTheme.colorScheme.onBackground,
            )

            VerticalSpacer(4.dp)

            Text(
                text = subtitle,
                style = ChallengeTheme.typography.medium12,
                color = ChallengeTheme.colorScheme.onSurfaceVariant,
            )

            VerticalSpacer(20.dp)

            IconTextButton(
                text = ctaLabel,
                icon = ctaIcon,
                onClick = onClickCta,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview
@Composable
private fun FriendPickPlaceholderCardEmptyPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            FriendPickPlaceholderCard(
                title = "아직 친구가 없어요",
                subtitle = "친구를 추가하고 챌린지를 걸어보세요",
                ctaLabel = "친구 추가하기",
                ctaIcon = Icons.Filled.PersonAdd,
                onClickCta = {},
            )
        }
    }
}

@Preview
@Composable
private fun FriendPickPlaceholderCardErrorPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            FriendPickPlaceholderCard(
                title = "친구 목록을 불러오지 못했어요",
                subtitle = "잠시 후 다시 시도해주세요",
                ctaLabel = "다시 시도",
                ctaIcon = Icons.Filled.Refresh,
                onClickCta = {},
            )
        }
    }
}
