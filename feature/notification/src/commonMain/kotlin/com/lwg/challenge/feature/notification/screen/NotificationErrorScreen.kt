package com.lwg.challenge.feature.notification.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.ui.components.EmptyStateCard

/** 첫 조회 실패. 빈 상태와 헷갈리지 않게 아이콘부터 다르게 둔다. */
@Composable
internal fun NotificationErrorScreen(onRetryClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        EmptyStateCard(
            title = "알림을 불러오지 못했어요",
            subtitle = "잠시 후 다시 시도해주세요",
            ctaLabel = "다시 시도",
            icon = Icons.Filled.CloudOff,
            onClickCta = onRetryClick,
            // 기본값이 PersonAdd 라 그냥 두면 "다시 시도" 에 친구 추가 아이콘이 붙는다.
            ctaIcon = Icons.Filled.Refresh,
        )
    }
}

@ThemePreviews
@Composable
private fun NotificationErrorScreenPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background.screen)) {
            NotificationErrorScreen(onRetryClick = {})
        }
    }
}
