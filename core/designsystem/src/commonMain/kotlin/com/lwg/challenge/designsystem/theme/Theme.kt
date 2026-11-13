package com.lwg.challenge.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import com.lwg.challenge.designsystem.statusbar.SetNavigationBarAppearance

val LocalDarkTheme = staticCompositionLocalOf { true }

@Composable
fun ChallengeTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkChallengeColorScheme else LightChallengeColorScheme
    val brushes = remember(colorScheme) { buildChallengeBrushes(colorScheme.gradient) }
    val typography = createTypography()

    SetNavigationBarAppearance(darkTheme)

    CompositionLocalProvider(
        LocalDarkTheme provides darkTheme,
        LocalChallengeColorScheme provides colorScheme,
        LocalChallengeBrushes provides brushes,
        LocalTypography provides typography,
    ) {
        MaterialTheme(
            colorScheme = colorScheme.toMaterialColorScheme(darkTheme),
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

    val isDarkTheme: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalDarkTheme.current
}

private fun ChallengeColorScheme.toMaterialColorScheme(darkTheme: Boolean): ColorScheme {
    val base = if (darkTheme) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = point,
        onPrimary = text.onPoint,
        primaryContainer = point,
        onPrimaryContainer = text.onPoint,

        secondary = background.field,
        onSecondary = text.primary,
        secondaryContainer = background.field,
        onSecondaryContainer = text.primary,

        tertiary = background.cardSub,
        onTertiary = text.primary,
        tertiaryContainer = background.cardSub,
        onTertiaryContainer = text.primary,

        background = background.screen,
        onBackground = text.primary,

        surface = background.card,
        onSurface = text.primary,
        surfaceVariant = background.cardSub,
        onSurfaceVariant = text.secondary,
        surfaceContainer = background.card,
        surfaceContainerHigh = background.card,
        surfaceContainerHighest = background.cardSub,
        surfaceContainerLow = background.card,
        surfaceContainerLowest = background.screen,
        inverseSurface = text.primary,
        inverseOnSurface = background.screen,

        outline = line.border,
        outlineVariant = line.divider,

        error = error,
        onError = text.primary,
        errorContainer = error,
        onErrorContainer = text.primary,
    )
}
