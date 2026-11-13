package com.lwg.challenge.feature.notification.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.VerticalSpacer
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme

/**
 * 알림 0건. 놓을 CTA 가 없어(이 사용자는 방금 홈에서 왔다) `EmptyStateCard` 대신 카드 크롬 없는 담담한 안내로 둔다.
 * 알림 권한과 목록은 다른 축이라 "알림을 켜세요" 같은 유도를 넣지 않는다.
 */
@Composable
internal fun NotificationEmptyScreen(modifier: Modifier = Modifier) {
    val primary = ChallengeTheme.colorScheme.point

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 64.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(primary.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center,
        ) {
            // filled 벨은 모르는 타입 폴백이 써서 outline 으로 둔다. NotificationsOff 는 끈 것처럼 보인다.
            Icon(
                imageVector = Icons.Filled.NotificationsNone,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = primary,
            )
        }

        VerticalSpacer(16.dp)

        Text(
            text = "아직 알림이 없어요",
            style = ChallengeTheme.typography.bold16,
            color = ChallengeTheme.colorScheme.text.primary,
        )

        VerticalSpacer(4.dp)

        Text(
            text = "챌린지가 오가면 여기에 쌓여요",
            style = ChallengeTheme.typography.medium12,
            color = ChallengeTheme.colorScheme.text.secondary,
        )
    }
}

@ThemePreviews
@Composable
private fun NotificationEmptyScreenPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background.screen)) {
            NotificationEmptyScreen()
        }
    }
}
