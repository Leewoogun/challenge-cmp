package com.lwg.challenge.feature.mypage.archive.screen

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
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.ui.components.EmptyStateCard
import org.jetbrains.compose.ui.tooling.preview.Preview

/** 첫 로딩 실패 본문. 빈 상태와 문구만 다르면 0건인지 못 불러온 건지 갈리지 않아 아이콘까지 바꾼다. */
@Composable
internal fun ArchiveErrorScreen(onRetryClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        EmptyStateCard(
            title = "보관함을 불러오지 못했어요",
            subtitle = "잠시 후 다시 시도해주세요",
            ctaLabel = "다시 시도",
            icon = Icons.Filled.CloudOff,
            onClickCta = onRetryClick,
            ctaIcon = Icons.Filled.Refresh,
        )
    }
}

@Preview
@Composable
private fun ArchiveErrorScreenPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            ArchiveErrorScreen(onRetryClick = {})
        }
    }
}
