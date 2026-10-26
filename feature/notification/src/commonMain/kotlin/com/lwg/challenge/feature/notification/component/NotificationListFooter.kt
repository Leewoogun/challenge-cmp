package com.lwg.challenge.feature.notification.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.HorizontalSpacer
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/** 다음 쪽 로딩. 끝인지 로딩 중인지 구분되지 않으면 사용자는 끝인 줄 알고 나간다. */
@Composable
internal fun NotificationAppendLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = FooterVerticalPadding),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            color = ChallengeTheme.colorScheme.primary,
            // 기본 4dp 는 20dp 원에서 도넛처럼 두껍다.
            strokeWidth = 2.dp,
        )
    }
}

/** 다음 쪽 실패. 로딩이 있던 같은 자리에서 스크롤 위치를 유지한 채 다시 시도한다. */
@Composable
internal fun NotificationAppendRetry(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = FooterVerticalPadding),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Refresh,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = ChallengeTheme.colorScheme.primary,
        )

        HorizontalSpacer(6.dp)

        Text(
            text = "다시 시도",
            style = ChallengeTheme.typography.medium14,
            color = ChallengeTheme.colorScheme.primary,
        )
    }
}

/** 로딩과 실패가 같은 높이를 쓰는지 본다. 다르면 교체될 때 목록이 튄다. */
@Preview
@Composable
private fun NotificationListFooterPreview() {
    ChallengeTheme {
        Column(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            NotificationAppendLoading()
            NotificationAppendRetry(onClick = {})
        }
    }
}

/** 인디케이터와 "다시 시도" 가 같은 높이를 쓰게 하는 상하 여백. */
private val FooterVerticalPadding = 16.dp
