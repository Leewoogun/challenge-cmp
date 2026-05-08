package com.lwg.challenge.feature.login

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme

/**
 * 로그인 Hero 의 "영혼의 인장" 스탬프 로고.
 *
 * 구조 (디자인 결정 2026-05-08 반영 — 외곽 회전 링은 제거):
 * - 140dp 원형 fire-gradient 배경 + `pulse-fire` 맥동
 * - 내부 ring (`onPrimary` 30% alpha) 인셋 8dp
 * - 중앙 Flame 아이콘 (56dp, `onPrimary`)
 * - 외부 Sparkles 2개 (우상단 20dp / 좌하단 14dp, `primary` + 깜빡임)
 *
 * 이 컴포넌트는 로그인 외 화면에서 사용되지 않으므로 `:feature:login` 내부에 둔다.
 * 다른 화면에서 재사용 필요해지면 `:core:designsystem` 으로 승격.
 */
@Composable
internal fun SoulStampLogo(
    modifier: Modifier = Modifier,
) {
    val infinite = rememberInfiniteTransition(label = "soul-stamp")

    // pulse-fire: 스탬프 자체의 맥동 (alpha 0.85 ~ 1.0).
    val stampAlpha by infinite.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2_000),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "stamp-alpha",
    )

    // sparkle pulse — 두 개를 다른 위상으로.
    val sparkleAlpha1 by infinite.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_400),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "sparkle1-alpha",
    )
    val sparkleAlpha2 by infinite.animateFloat(
        initialValue = 1.0f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_400),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "sparkle2-alpha",
    )

    val onPrimary = ChallengeTheme.colorScheme.onPrimary
    val primary = ChallengeTheme.colorScheme.primary
    val fireBrush = ChallengeTheme.brushes.fire

    Box(modifier = modifier.size(140.dp)) {
        // Inner stamp — fire-gradient 원
        Box(
            modifier = Modifier
                .size(140.dp)
                .alpha(stampAlpha)
                .clip(CircleShape)
                .background(brush = fireBrush),
            contentAlignment = Alignment.Center,
        ) {
            // 내부 링 (onPrimary 30% alpha)
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(124.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = onPrimary.copy(alpha = 0.30f),
                        shape = CircleShape,
                    ),
            )
            // 중앙 Flame 아이콘
            Icon(
                imageVector = Icons.Filled.LocalFireDepartment,
                contentDescription = null,
                tint = onPrimary,
                modifier = Modifier.size(56.dp),
            )
        }

        // 우상단 Sparkles (20dp)
        Icon(
            imageVector = Icons.Filled.AutoAwesome,
            contentDescription = null,
            tint = primary,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 8.dp, y = (-8).dp)
                .size(20.dp)
                .alpha(sparkleAlpha1),
        )
        // 좌하단 Sparkles (14dp)
        Icon(
            imageVector = Icons.Filled.AutoAwesome,
            contentDescription = null,
            tint = primary,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-8).dp, y = 4.dp)
                .size(14.dp)
                .alpha(sparkleAlpha2),
        )
    }
}
