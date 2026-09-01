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

/**
 * 다음 쪽을 받는 중임을 목록 **마지막 항목 자리**에서만 알린다. design.md §6.3 ①.
 *
 * 무한 스크롤은 끝까지 내렸을 때 아무 일도 안 일어나는 것처럼 보이는 순간이 있는데, 그 순간이
 * *"로딩 중"* 인지 *"끝"* 인지 구분되지 않으면 사용자는 끝인 줄 알고 나간다.
 *
 * 🔴 전체 화면 로딩으로 덮지 않는다 — 읽던 목록이 사라지고 스크롤 위치를 잃는다.
 */
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
            // 기본 4dp 는 20dp 원에서 도넛처럼 두껍다. 첫 조회의 전체 화면 인디케이터와 달리
            // 이건 카드 사이에 끼는 작은 표식이다.
            strokeWidth = 2.dp,
        )
    }
}

/**
 * 다음 쪽 실패 자리. design.md §6.3 ③.
 *
 * [NotificationAppendLoading] 이 있던 **같은 자리**를 대체한다 — 스크롤 위치를 유지한 채 그 자리에서
 * 다시 시도하기 위해서다. 🔴 스낵바만 띄우고 끝내면 스낵바가 사라진 뒤 더 볼 수단이 없어진다.
 */
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

/** 로딩과 실패가 **같은 높이의 같은 자리**를 쓰는지 나란히 놓고 본다 — 교체될 때 목록이 튀면 안 된다. */
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

/** 인디케이터와 "다시 시도" 가 같은 자리를 쓰게 하는 상하 여백. design.md §6.3 ①. */
private val FooterVerticalPadding = 16.dp
