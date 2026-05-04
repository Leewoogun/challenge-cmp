package com.lwg.challenge.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/**
 * Challenge (MAENGSE) Root Theme — 단일 다크 톤.
 *
 * 색에 접근하는 정식 경로:
 * - 시멘틱 색상  → `ChallengeTheme.colorScheme.<name>`
 * - 그라데이션   → `ChallengeTheme.brushes.<name>`
 * - 외부 브랜드  → `BrandColors.<name>`
 *
 * Material3 컴포넌트(Surface/Scaffold 등)와의 호환을 위해 내부적으로 동일 색상의
 * Material3 [androidx.compose.material3.ColorScheme] 도 함께 제공한다.
 * 단, **Feature 코드는 `MaterialTheme.colorScheme.X` 를 직접 호출하지 않는다.**
 *
 * ```
 * ChallengeTheme {
 *     Text(
 *         text = "...",
 *         color = ChallengeTheme.colorScheme.onBackground,
 *         style = ChallengeTheme.typography.medium16,
 *     )
 *     Box(Modifier.background(ChallengeTheme.brushes.fire))
 * }
 * ```
 */
@Composable
fun ChallengeTheme(
    content: @Composable () -> Unit,
) {
    val colorScheme = DefaultChallengeColorScheme
    val brushes = buildChallengeBrushes(colorScheme)
    val typography = createTypography()

    CompositionLocalProvider(
        LocalChallengeColorScheme provides colorScheme,
        LocalChallengeBrushes provides brushes,
        LocalTypography provides typography,
    ) {
        MaterialTheme(
            colorScheme = colorScheme.toMaterialColorScheme(),
            content = content,
        )
    }
}

object ChallengeTheme {

    /** 모든 시멘틱 색상의 단일 진입점. */
    val colorScheme: ChallengeColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalChallengeColorScheme.current

    /** 그라데이션 브러시 (fire / card / glow). */
    val brushes: ChallengeBrushes
        @Composable
        @ReadOnlyComposable
        get() = LocalChallengeBrushes.current

    val typography: ChallengeTypoGraphy
        @Composable
        @ReadOnlyComposable
        get() = LocalTypography.current
}

private fun ChallengeColorScheme.toMaterialColorScheme() = darkColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    primaryContainer = primary,
    onPrimaryContainer = onPrimary,

    secondary = secondary,
    onSecondary = onSecondary,
    secondaryContainer = secondary,
    onSecondaryContainer = onSecondary,

    tertiary = tertiary,
    onTertiary = onTertiary,
    tertiaryContainer = tertiary,
    onTertiaryContainer = onTertiary,

    background = background,
    onBackground = onBackground,

    surface = surface,
    onSurface = onSurface,
    surfaceVariant = surfaceVariant,
    onSurfaceVariant = onSurfaceVariant,

    outline = outline,
    outlineVariant = outline,

    error = error,
    onError = onError,
    errorContainer = error,
    onErrorContainer = onError,
)
