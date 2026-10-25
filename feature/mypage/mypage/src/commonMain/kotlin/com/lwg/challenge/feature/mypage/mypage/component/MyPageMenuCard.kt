package com.lwg.challenge.feature.mypage.mypage.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 마이페이지 메뉴 1행. design.md §1.4.4.
 *
 * 지금은 "계약서 보관함" 하나뿐이지만 통계 feature 가 이 위로 들어올 자리라 목록 전제로 파라미터화했다.
 */
@Composable
internal fun MyPageMenuCard(
    icon: ImageVector,
    label: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = ChallengeTheme.colorScheme.surface,
        border = BorderStroke(width = 1.dp, color = ChallengeTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ChallengeTheme.colorScheme.secondary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = ChallengeTheme.colorScheme.primary,
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = label,
                    style = ChallengeTheme.typography.medium14,
                    color = ChallengeTheme.colorScheme.onBackground,
                )
                Text(
                    text = description,
                    style = ChallengeTheme.typography.medium10,
                    color = ChallengeTheme.colorScheme.onSurfaceVariant,
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = ChallengeTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview
@Composable
private fun MyPageMenuCardPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            MyPageMenuCard(
                icon = Icons.Filled.Description,
                label = "계약서 보관함",
                description = "과거 영혼의 맹세 열람",
                onClick = {},
            )
        }
    }
}
