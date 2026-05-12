package com.lwg.challenge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lwg.challenge.designsystem.theme.ChallengeTheme

/**
 * 후속 feature 개발 전 임시 화면용 공통 placeholder.
 *
 * 다크 배경 + 화면명(bold18) + 보조 문구(medium14) 두 줄. 친구/랭킹/MY 등 BottomBar 라우팅 검증용으로 사용.
 *
 * @param title 화면명 (예: "친구", "랭킹", "MY")
 * @param subtitle 보조 문구. 기본값 "준비 중"
 */
@Composable
fun PlaceholderScreen(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String = "준비 중",
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ChallengeTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            style = ChallengeTheme.typography.bold18,
            color = ChallengeTheme.colorScheme.onBackground,
        )
        Text(
            text = subtitle,
            style = ChallengeTheme.typography.medium14,
            color = ChallengeTheme.colorScheme.onSurfaceVariant,
        )
    }
}
