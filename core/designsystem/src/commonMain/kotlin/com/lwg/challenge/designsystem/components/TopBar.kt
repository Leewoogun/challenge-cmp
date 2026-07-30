package com.lwg.challenge.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

object ChallengeTopBarDefaults {
    val Height = 56.dp
    val HorizontalPadding = 20.dp
    val ItemSpacing = 8.dp
    val IconButtonSize = 40.dp
    val IconSize = 20.dp
}

/**
 * 슬롯 기반 공용 상단바. `[navigationIcon] [title] [actions]` 를 한 줄에 배치한다.
 *
 * [title] / [actions] 는 [RowScope] 이므로 남는 폭 배분은 호출부가 `Modifier.weight` 로 정한다
 * (검색 입력창처럼 title 이 폭을 모두 먹는 경우 대응).
 * 텍스트 제목 + 뒤로가기 조합이면 아래 String 오버로드를 쓴다.
 *
 * [navigationIcon] 이 있으면 좌측 여백을 주지 않는다 — 아이콘 버튼의 자체 내부 여백이 그 역할을 한다.
 * 유무를 알아야 여백을 계산할 수 있어 빈 람다가 아니라 nullable 로 받는다.
 */
@Composable
fun ChallengeTopBar(
    modifier: Modifier = Modifier,
    containerColor: Color = ChallengeTheme.colorScheme.surface,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    title: @Composable RowScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = containerColor,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(ChallengeTopBarDefaults.Height)
                .padding(
                    start = if (navigationIcon != null) 0.dp else ChallengeTopBarDefaults.HorizontalPadding,
                    end = ChallengeTopBarDefaults.HorizontalPadding,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ChallengeTopBarDefaults.ItemSpacing),
        ) {
            navigationIcon?.invoke()
            title()
            actions()
        }
    }
}

/**
 * 텍스트 제목 상단바. [onBack] 이 null 이면 뒤로가기 버튼을 그리지 않는다.
 *
 * 제목이 남는 폭을 차지하므로 [actions] 는 항상 우측 정렬된다.
 */
@Composable
fun ChallengeTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    containerColor: Color = ChallengeTheme.colorScheme.surface,
    titleStyle: TextStyle = ChallengeTheme.typography.bold18,
    titleColor: Color = ChallengeTheme.colorScheme.onBackground,
    actions: @Composable RowScope.() -> Unit = {},
) {
    ChallengeTopBar(
        modifier = modifier,
        containerColor = containerColor,
        navigationIcon = onBack?.let { { ChallengeTopBarBackButton(onClick = it) } },
        actions = actions,
        title = {
            Text(
                text = title,
                style = titleStyle,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        },
    )
}

@Composable
fun ChallengeTopBarBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(ChallengeTopBarDefaults.IconButtonSize),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "뒤로 가기",
            modifier = Modifier.size(ChallengeTopBarDefaults.IconSize),
            tint = ChallengeTheme.colorScheme.onBackground,
        )
    }
}

@Preview
@Composable
private fun ChallengeTopBarTitleOnlyPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            ChallengeTopBar(title = "친구")
        }
    }
}

@Preview
@Composable
private fun ChallengeTopBarWithBackPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            ChallengeTopBar(
                title = "새 챌린지",
                onBack = {},
            )
        }
    }
}

@Preview
@Composable
private fun ChallengeTopBarWithActionsPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            ChallengeTopBar(
                title = "새 챌린지",
                onBack = {},
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = "알림",
                            modifier = Modifier.size(24.dp),
                            tint = ChallengeTheme.colorScheme.onBackground,
                        )
                    }
                },
            )
        }
    }
}

@Preview
@Composable
private fun ChallengeTopBarSlotPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            ChallengeTopBar(
                navigationIcon = { ChallengeTopBarBackButton(onClick = {}) },
                title = {
                    Text(
                        text = "커스텀 슬롯",
                        style = ChallengeTheme.typography.medium14,
                        color = ChallengeTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                },
            )
        }
    }
}
