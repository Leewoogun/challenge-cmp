package com.lwg.challenge.ui.components

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
import com.lwg.challenge.designsystem.components.button.IconTextButton
import com.lwg.challenge.designsystem.components.button.IconTextButtonStyle
import com.lwg.challenge.designsystem.components.VerticalSpacer
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme

/**
 * 아이콘 + 헤드라인 + 서브라인 + CTA 로 구성된 빈 상태 카드.
 *
 * @param title 헤드라인(`bold16` + `onBackground`).
 * @param subtitle 서브라인(`medium12` + `onSurfaceVariant`).
 * @param ctaLabel CTA 버튼 라벨(`medium14` + `onPrimary`).
 * @param icon 일러스트 자리 아이콘(32.dp, primary tint).
 * @param onClickCta CTA 클릭 콜백.
 * @param onClickInvite 보조 CTA 콜백. null 이면 보조 버튼을 노출하지 않는다.
 * @param inviteLabel 보조 CTA 라벨.
 * @param ctaIcon CTA 버튼 아이콘. 기본값은 친구 추가다.
 * @param inviteIcon 보조 CTA 버튼 아이콘.
 */
@Composable
fun EmptyStateCard(
    title: String,
    subtitle: String,
    ctaLabel: String,
    icon: ImageVector,
    onClickCta: () -> Unit,
    modifier: Modifier = Modifier,
    onClickInvite: (() -> Unit)? = null,
    inviteLabel: String = "카카오톡으로 초대하기",
    ctaIcon: ImageVector = Icons.Filled.PersonAdd,
    inviteIcon: ImageVector = Icons.AutoMirrored.Filled.Send,
) {
    val primary = ChallengeTheme.colorScheme.point

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ChallengeTheme.colorScheme.background.card,
        border = BorderStroke(width = 1.dp, color = ChallengeTheme.colorScheme.line.border),
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
                color = ChallengeTheme.colorScheme.text.primary,
            )

            VerticalSpacer(4.dp)

            Text(
                text = subtitle,
                style = ChallengeTheme.typography.medium12,
                color = ChallengeTheme.colorScheme.text.secondary,
            )

            VerticalSpacer(20.dp)

            IconTextButton(
                text = ctaLabel,
                icon = ctaIcon,
                onClick = onClickCta,
                modifier = Modifier.fillMaxWidth(),
            )

            if (onClickInvite != null) {
                VerticalSpacer(8.dp)

                IconTextButton(
                    text = inviteLabel,
                    icon = inviteIcon,
                    onClick = onClickInvite,
                    modifier = Modifier.fillMaxWidth(),
                    style = IconTextButtonStyle.Outlined,
                )
            }
        }
    }
}

@ThemePreviews
@Composable
private fun EmptyStateCardPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            EmptyStateCard(
                title = "아직 친구가 없어요",
                subtitle = "친구를 추가하고 함께 챌린지를 시작해보세요",
                ctaLabel = "친구 추가하기",
                icon = Icons.Filled.Group,
                onClickCta = {},
            )
        }
    }
}

@ThemePreviews
@Composable
private fun EmptyStateCardWithInvitePreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            EmptyStateCard(
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
