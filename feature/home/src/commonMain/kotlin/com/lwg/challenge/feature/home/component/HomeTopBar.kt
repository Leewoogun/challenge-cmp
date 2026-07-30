package com.lwg.challenge.feature.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import com.lwg.challenge.designsystem.components.ChallengeTopBar
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun HomeTopBar(
    hasUnreadNotification: Boolean,
    onBellClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChallengeTopBar(
        modifier = modifier,
        containerColor = ChallengeTheme.colorScheme.surface.copy(alpha = 0.95f),
        title = {
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
                modifier = Modifier.weight(1f),
            )
        },
        actions = {
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
