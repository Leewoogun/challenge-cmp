package com.lwg.challenge.feature.notification.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 날짜 그룹 헤더. 건수는 붙이지 않는다 — 지난 날짜의 알림 수는 아무 행동도 바꾸지 않는다.
 * 그룹이 하루 몇 건으로 짧아 stickyHeader 로 두지 않는다.
 *
 * @param modifier 그룹 사이 위 여백은 호출부가 준다. 첫 그룹은 목록 contentPadding 이 이미 냈다.
 */
@Composable
internal fun NotificationDateHeader(
    label: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = label,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 8.dp),
        style = ChallengeTheme.typography.bold14,
        color = ChallengeTheme.colorScheme.text.primary,
    )
}

@Preview
@Composable
private fun NotificationDateHeaderPreview() {
    ChallengeTheme {
        Column(modifier = Modifier.background(ChallengeTheme.colorScheme.background.screen)) {
            NotificationDateHeader(label = "오늘")
            NotificationDateHeader(label = "어제", modifier = Modifier.padding(top = 16.dp))
            NotificationDateHeader(label = "8월 28일", modifier = Modifier.padding(top = 16.dp))
        }
    }
}
