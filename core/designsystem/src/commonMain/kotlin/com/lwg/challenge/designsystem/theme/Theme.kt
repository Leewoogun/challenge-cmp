package com.lwg.challenge.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

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
