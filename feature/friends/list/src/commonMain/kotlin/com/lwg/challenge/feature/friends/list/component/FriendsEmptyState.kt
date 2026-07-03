package com.lwg.challenge.feature.friends.list.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.IconTextButton
import com.lwg.challenge.designsystem.components.IconTextButtonStyle
import com.lwg.challenge.designsystem.components.VerticalSpacer
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 친구 탭 빈 상태 카드. design.md §1/§3.1/§4 — 1차 1단계 친구 0명 진입 사용자 유도.
 *
 * @param title 헤드라인(`bold16` + `onBackground`).
 * @param subtitle 서브라인(`medium12` + `onSurfaceVariant`).
 * @param ctaLabel CTA 버튼 라벨(`medium14` + `onPrimary`).
 * @param icon 일러스트 자리 아이콘(`Icons.Filled.Group` 32.dp, primary tint).
 * @param onClickCta CTA 클릭 콜백(1차 1단계는 stub — "준비 중입니다" 스낵바).
 * @param onClickInvite 카카오톡 초대 보조 CTA 콜백. null 이면 보조 버튼을 노출하지 않는다.
 */
@Composable
internal fun FriendsEmptyState(
    title: String,
    subtitle: String,
    ctaLabel: String,
    icon: ImageVector,
    onClickCta: () -> Unit,
    modifier: Modifier = Modifier,
    onClickInvite: (() -> Unit)? = null,
) {
    val primary = ChallengeTheme.colorScheme.primary

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ChallengeTheme.colorScheme.surface,
        border = BorderStroke(width = 1.dp, color = ChallengeTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(primary.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = primary,
                )
            }

            VerticalSpacer(16.dp)

            Text(
                text = title,
                style = ChallengeTheme.typography.bold16,
                color = ChallengeTheme.colorScheme.onBackground,
            )

            VerticalSpacer(4.dp)

            Text(
                text = subtitle,
                style = ChallengeTheme.typography.medium12,
                color = ChallengeTheme.colorScheme.onSurfaceVariant,
            )

            VerticalSpacer(20.dp)

            IconTextButton(
                text = ctaLabel,
                icon = Icons.Filled.PersonAdd,
                onClick = onClickCta,
                modifier = Modifier.fillMaxWidth(),
            )

            if (onClickInvite != null) {
                VerticalSpacer(8.dp)

                IconTextButton(
                    text = "카카오톡으로 초대하기",
                    icon = Icons.AutoMirrored.Filled.Send,
                    onClick = onClickInvite,
                    modifier = Modifier.fillMaxWidth(),
                    style = IconTextButtonStyle.Outlined,
                )
            }
        }
    }
}

@Preview
@Composable
private fun FriendsEmptyStatePreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            FriendsEmptyState(
                title = "아직 친구가 없어요",
                subtitle = "친구를 추가하고 함께 챌린지를 시작해보세요",
                ctaLabel = "친구 추가하기",
                icon = Icons.Filled.Group,
                onClickCta = {},
            )
        }
    }
}

@Preview
@Composable
private fun FriendsEmptyStateWithInvitePreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            FriendsEmptyState(
                title = "아직 친구가 없어요",
                subtitle = "친구를 추가하고 함께 챌린지를 시작해보세요",
                ctaLabel = "친구 추가하기",
                icon = Icons.Filled.Group,
                onClickCta = {},
                onClickInvite = {},
            )
        }
    }
}
