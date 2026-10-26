package com.lwg.challenge.feature.mypage.archive.component

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.HorizontalDivider
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 보관함의 월 이동 컨트롤.
 */
@Composable
internal fun ArchiveMonthNavBar(
    label: String,
    countText: String,
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                // 상단바와 같은 surface 로 칠해 하나의 크롬 블록으로 읽히게 한다.
                .background(ChallengeTheme.colorScheme.surface)
                // 8 + (48 - 24) / 2 = 20 — 아이콘 모서리가 카드 gutter(20dp)와 같은 세로선에 선다.
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MonthArrowButton(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "이전 달",
                enabled = canGoPrevious,
                onClick = onPreviousClick,
            )

            // 가운데가 남는 폭을 전부 먹어야 라벨 길이(1월 ↔ 12월)가 달라져도 화살표가 좌우 끝에 고정된다.
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = label,
                    style = ChallengeTheme.typography.bold16,
                    color = ChallengeTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = countText,
                    style = ChallengeTheme.typography.medium12,
                    color = ChallengeTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            MonthArrowButton(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "다음 달",
                enabled = canGoNext,
                onClick = onNextClick,
            )
        }

        // 없으면 첫 카드가 바의 일부처럼 보여 divider 를 이 컴포넌트가 소유한다.
        HorizontalDivider(modifier = Modifier.fillMaxWidth())
    }
}

/** 월 이동 화살표 한 개. 응답이 올 때까지 월 라벨이 움직이지 않아 눌림 표현이 탭 피드백을 맡는다. */
@Composable
private fun MonthArrowButton(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    IconButton(onClick = onClick, enabled = enabled, interactionSource = interactionSource) {
        // 40dp 배경은 그림일 뿐 터치 타깃은 IconButton 의 48dp 그대로라 슬롯 기하가 흔들리지 않는다.
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = if (enabled) ChallengeTheme.colorScheme.secondary else Color.Transparent,
                    shape = RoundedCornerShape(12.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            // 기본 ripple 은 불투명 배경 아래에 깔려 안 보여 위에 얹는다. 비활성엔 눌림이 없고, 연타 중 움직이지 않게 스케일은 쓰지 않는다.
            if (enabled && pressed) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            color = ChallengeTheme.colorScheme.onBackground.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(12.dp),
                        ),
                )
            }

            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp),
                // 배경이 사라지는 만큼 아이콘을 0.25 로 더 내린다. 0.2 미만은 다크 테마에서 숨김이 된다(실기 확인 필요).
                tint = if (enabled) {
                    ChallengeTheme.colorScheme.onBackground
                } else {
                    ChallengeTheme.colorScheme.onBackground.copy(alpha = 0.25f)
                },
            )
        }
    }
}

@Preview
@Composable
private fun ArchiveMonthNavBarPreview() {
    ArchiveMonthNavBarPreviewContainer {
        ArchiveMonthNavBar(
            label = "2026년 5월",
            countText = "3건",
            canGoPrevious = true,
            canGoNext = true,
            onPreviousClick = {},
            onNextClick = {},
        )
    }
}

/** 첫 진입 상태 — 이번 달이라 다음 이동이 막혀 있다. */
@Preview
@Composable
private fun ArchiveMonthNavBarAtLatestPreview() {
    ArchiveMonthNavBarPreviewContainer {
        ArchiveMonthNavBar(
            label = "2026년 8월",
            countText = "2건",
            canGoPrevious = true,
            canGoNext = false,
            onPreviousClick = {},
            onNextClick = {},
        )
    }
}

/** `<` 가 막힌 모습. 보관함은 지금 이 상태를 만들지 않고, 컴포넌트의 표현 능력만 남긴다. */
@Preview
@Composable
private fun ArchiveMonthNavBarAtEarliestPreview() {
    ArchiveMonthNavBarPreviewContainer {
        ArchiveMonthNavBar(
            label = "2025년 11월",
            countText = "1건",
            canGoPrevious = false,
            canGoNext = true,
            onPreviousClick = {},
            onNextClick = {},
        )
    }
}

/** 양쪽이 모두 막힌 모습. 사용자가 고장으로 읽어 과거 하한을 없앴고, 다시 만들지 않으려고 남긴다. */
@Preview
@Composable
private fun ArchiveMonthNavBarNoMovementPreview() {
    ArchiveMonthNavBarPreviewContainer {
        ArchiveMonthNavBar(
            label = "2026년 8월",
            countText = "0건",
            canGoPrevious = false,
            canGoNext = false,
            onPreviousClick = {},
            onNextClick = {},
        )
    }
}

/** 기록이 있는 달들 사이의 빈 달 — 0건에도 바 높이가 그대로라 화살표가 움직이지 않는다. */
@Preview
@Composable
private fun ArchiveMonthNavBarZeroCountPreview() {
    ArchiveMonthNavBarPreviewContainer {
        ArchiveMonthNavBar(
            label = "2026년 3월",
            countText = "0건",
            canGoPrevious = true,
            canGoNext = true,
            onPreviousClick = {},
            onNextClick = {},
        )
    }
}

@Composable
private fun ArchiveMonthNavBarPreviewContainer(content: @Composable () -> Unit) {
    ChallengeTheme {
        Column(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            content()
        }
    }
}
