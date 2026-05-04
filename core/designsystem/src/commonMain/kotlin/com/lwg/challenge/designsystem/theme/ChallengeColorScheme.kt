package com.lwg.challenge.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Challenge 디자인 시스템의 모든 시멘틱 색상을 담는 단일 진입점.
 *
 * Feature 코드는 `ChallengeTheme.colorScheme.<name>` 으로만 색에 접근한다.
 * (Material3 [androidx.compose.material3.MaterialTheme.colorScheme] 직접 사용 금지)
 *
 * 단일 다크 톤만 운영하며, 라이트 모드는 지원하지 않는다.
 */
@Immutable
data class ChallengeColorScheme(
    // CTA / 강조
    val primary: Color,
    val onPrimary: Color,

    // 배경
    val background: Color,
    val onBackground: Color,

    // 표면 (카드 / 섹션)
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,

    // 보조 톤
    val secondary: Color,
    val onSecondary: Color,
    val tertiary: Color,
    val onTertiary: Color,

    // 보더 / 구분선
    val outline: Color,
    val border: Color,

    // 상태 색
    val error: Color,
    val onError: Color,
    val success: Color,
    val onSuccess: Color,
    val warning: Color,
    val onWarning: Color,

    // 차트 (랭킹 / 통계)
    val chart1: Color,
    val chart2: Color,
    val chart3: Color,
    val chart4: Color,
    val chart5: Color,

    // 그라데이션 stops (Brush 재료)
    val gradientPrimaryStart: Color,
    val gradientPrimaryEnd: Color,
    val gradientCardStart: Color,
    val gradientCardEnd: Color,
    val glowPrimary: Color,
)

internal val DefaultChallengeColorScheme = ChallengeColorScheme(
    primary = orange1,
    onPrimary = black1,

    background = black2,
    onBackground = white1,

    surface = black3,
    onSurface = white1,
    surfaceVariant = gray4,
    onSurfaceVariant = gray5,

    secondary = gray1,
    onSecondary = gray6,
    tertiary = gray2,
    onTertiary = white1,

    outline = gray3,
    border = gray3,

    error = red1,
    onError = white1,
    success = green1,
    onSuccess = black1,
    warning = yellow1,
    onWarning = black1,

    chart1 = orange1,
    chart2 = green1,
    chart3 = red1,
    chart4 = blue1,
    chart5 = yellow1,

    gradientPrimaryStart = orange1,
    gradientPrimaryEnd = orange2,
    gradientCardStart = black4,
    gradientCardEnd = black5,
    glowPrimary = orange1.copy(alpha = 0.15f),
)

internal val LocalChallengeColorScheme = staticCompositionLocalOf<ChallengeColorScheme> {
    error("ChallengeColorScheme not provided. Wrap your composable in ChallengeTheme {}.")
}
