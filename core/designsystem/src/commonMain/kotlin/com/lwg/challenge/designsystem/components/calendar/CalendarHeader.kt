package com.lwg.challenge.designsystem.components.calendar

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme

/**
 * @param isTitleExpandable 연·월 선택 화면으로 전환 가능한지. false 면 제목을 눌러도 반응하지 않는다.
 */
@Composable
internal fun CalendarHeader(
    title: String,
    isTitleExpandable: Boolean,
    isExpanded: Boolean,
    isPreviousEnabled: Boolean,
    isNextEnabled: Boolean,
    previousDescription: String,
    nextDescription: String,
    onTitleClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val arrowRotation by animateFloatAsState(if (isExpanded) 180f else 0f)

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable(enabled = isTitleExpandable, onClick = onTitleClick)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = ChallengeTheme.typography.bold18,
                color = ChallengeTheme.colorScheme.text.primary,
            )
            if (isTitleExpandable) {
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = ChallengeTheme.colorScheme.icon.secondary,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(arrowRotation),
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CalendarArrowButton(
                icon = Icons.Rounded.ChevronLeft,
                contentDescription = previousDescription,
                enabled = isPreviousEnabled,
                onClick = onPreviousClick,
            )
            CalendarArrowButton(
                icon = Icons.Rounded.ChevronRight,
                contentDescription = nextDescription,
                enabled = isNextEnabled,
                onClick = onNextClick,
            )
        }
    }
}

@Composable
private fun CalendarArrowButton(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(ChallengeTheme.colorScheme.background.field)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) {
                ChallengeTheme.colorScheme.icon.primary
            } else {
                ChallengeTheme.colorScheme.calendar.disabledText
            },
            modifier = Modifier.size(20.dp),
        )
    }
}

@ThemePreviews
@Composable
private fun CalendarHeaderPreview() {
    ChallengeTheme {
        Column(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CalendarHeader(
                title = "2026년 11월",
                isTitleExpandable = true,
                isExpanded = false,
                isPreviousEnabled = false,
                isNextEnabled = true,
                previousDescription = "이전 달",
                nextDescription = "다음 달",
                onTitleClick = {},
                onPreviousClick = {},
                onNextClick = {},
            )
            CalendarHeader(
                title = "2026년",
                isTitleExpandable = true,
                isExpanded = true,
                isPreviousEnabled = true,
                isNextEnabled = true,
                previousDescription = "이전 해",
                nextDescription = "다음 해",
                onTitleClick = {},
                onPreviousClick = {},
                onNextClick = {},
            )
        }
    }
}
