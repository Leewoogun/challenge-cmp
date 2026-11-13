package com.lwg.challenge.feature.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.ChallengeBaseTopBar
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme

@Composable
internal fun HomeTopBar(
    hasUnreadNotification: Boolean,
    onBellClick: () -> Unit,
    modifier: Modifier = Modifier,
    debugAccountLabel: String? = null,
) {
    ChallengeBaseTopBar(
        modifier = modifier,
        containerColor = ChallengeTheme.colorScheme.background.card.copy(alpha = 0.95f),
        title = {
            Icon(
                imageVector = Icons.Filled.LocalFireDepartment,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = ChallengeTheme.colorScheme.icon.point,
            )
            Text(
                text = "맹세",
                style = ChallengeTheme.typography.bold20,
                color = ChallengeTheme.colorScheme.text.primary,
                modifier = Modifier.weight(1f),
            )
            if (debugAccountLabel != null) {
                Text(
                    text = debugAccountLabel,
                    style = ChallengeTheme.typography.medium10,
                    color = ChallengeTheme.colorScheme.text.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        // 비정상적으로 긴 닉네임이 타이틀 폭을 다 먹지 않도록 상한을 둔다.
                        .widthIn(max = 160.dp)
                        .background(
                            color = ChallengeTheme.colorScheme.background.field,
                            shape = RoundedCornerShape(6.dp),
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
        },
        actions = {
            Box {
                IconButton(onClick = onBellClick) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "알림",
                        modifier = Modifier.size(24.dp),
                        tint = ChallengeTheme.colorScheme.icon.primary,
                    )
                }
                if (hasUnreadNotification) {
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
        },
    )
}

@ThemePreviews
@Composable
private fun HomeTopBarUnreadPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier.background(ChallengeTheme.colorScheme.background.screen),
        ) {
            HomeTopBar(
                hasUnreadNotification = true,
                onBellClick = {},
            )
        }
    }
}

@ThemePreviews
@Composable
private fun HomeTopBarReadPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier.background(ChallengeTheme.colorScheme.background.screen),
        ) {
            HomeTopBar(
                hasUnreadNotification = false,
                onBellClick = {},
            )
        }
    }
}

@ThemePreviews
@Composable
private fun HomeTopBarDebugAccountPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier.background(ChallengeTheme.colorScheme.background.screen),
        ) {
            HomeTopBar(
                hasUnreadNotification = true,
                onBellClick = {},
                debugAccountLabel = "테스터1",
            )
        }
    }
}
