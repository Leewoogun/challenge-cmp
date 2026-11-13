package com.lwg.challenge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.VerificationStatus

/**
 * 인증 상태(대기중/인증완료/실패)를 아이콘 + 라벨 캡슐로 표시한다.
 *
 * 점 + 텍스트 캡슐인 [StatusPillBadge] 와는 용도·모양이 모두 다르다. 통합하지 말 것.
 */
@Composable
fun VerificationStatusPill(
    status: VerificationStatus,
    modifier: Modifier = Modifier,
) {
    val visual = verificationStatusVisualOf(status)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = visual.accent.copy(alpha = 0.10f),
        contentColor = visual.accent,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = visual.icon,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = visual.accent,
            )
            Text(
                text = visual.label,
                style = ChallengeTheme.typography.medium10,
                color = visual.accent,
            )
        }
    }
}

private data class VerificationStatusVisual(
    val accent: Color,
    val label: String,
    val icon: ImageVector,
)

@Composable
private fun verificationStatusVisualOf(status: VerificationStatus): VerificationStatusVisual =
    when (status) {
        VerificationStatus.PENDING -> VerificationStatusVisual(
            accent = ChallengeTheme.colorScheme.warning,
            label = "대기중",
            icon = Icons.Filled.Schedule,
        )
        VerificationStatus.VERIFIED -> VerificationStatusVisual(
            accent = ChallengeTheme.colorScheme.success,
            label = "인증완료",
            icon = Icons.Filled.CheckCircle,
        )
        VerificationStatus.FAILED -> VerificationStatusVisual(
            accent = ChallengeTheme.colorScheme.error,
            label = "실패",
            icon = Icons.Filled.Cancel,
        )
    }

@ThemePreviews
@Composable
private fun VerificationStatusPillPendingPreview() {
    ChallengeTheme {
        Column(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            VerificationStatusPill(status = VerificationStatus.PENDING)
        }
    }
}

@ThemePreviews
@Composable
private fun VerificationStatusPillVerifiedPreview() {
    ChallengeTheme {
        Column(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            VerificationStatusPill(status = VerificationStatus.VERIFIED)
        }
    }
}

@ThemePreviews
@Composable
private fun VerificationStatusPillFailedPreview() {
    ChallengeTheme {
        Column(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            VerificationStatusPill(status = VerificationStatus.FAILED)
        }
    }
}
