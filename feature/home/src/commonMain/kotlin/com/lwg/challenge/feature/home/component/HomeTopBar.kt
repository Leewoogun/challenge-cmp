package com.lwg.challenge.feature.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun HomeTopBar(
    hasUnreadNotification: Boolean,
    onBellClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(ChallengeTheme.colorScheme.surface.copy(alpha = 0.95f))
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.LocalFireDepartment,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = ChallengeTheme.colorScheme.primary,
            )
            Text(
                text = "맹세",
                style = ChallengeTheme.typography.bold20,
                color = ChallengeTheme.colorScheme.onBackground,
            )
        }

        Box {
            IconButton(onClick = onBellClick) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "알림",
                    modifier = Modifier.size(24.dp),
                    tint = ChallengeTheme.colorScheme.onBackground,
                )
            }
            if (hasUnreadNotification) {
                // 우상단 2.dp dot (destructive) — 미확인 알림 표시
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 10.dp, end = 10.dp)
                        .size(8.dp)
                        .background(
                            color = ChallengeTheme.colorScheme.error,
                            shape = CircleShape,
                        ),
                )
            }
        }
    }
}

@Preview
@Composable
private fun HomeTopBarUnreadPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier.background(ChallengeTheme.colorScheme.background),
        ) {
            HomeTopBar(
                hasUnreadNotification = true,
                onBellClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun HomeTopBarReadPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier.background(ChallengeTheme.colorScheme.background),
        ) {
            HomeTopBar(
                hasUnreadNotification = false,
                onBellClick = {},
            )
        }
    }
}
