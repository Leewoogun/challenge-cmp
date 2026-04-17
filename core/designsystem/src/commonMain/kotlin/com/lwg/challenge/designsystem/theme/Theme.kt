package com.lwg.challenge.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

@Composable
fun ChallengeTheme(
    content: @Composable () -> Unit,
) {
    val typography = createTypography()

    CompositionLocalProvider(
        LocalTypography provides typography,
        LocalColorScheme provides ChallengeColorScheme.lightColorScheme
    ) {
        MaterialTheme(
            content = content,
        )
    }
}

object ChallengeTheme {

    val colorScheme: ChallengeColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalColorScheme.current

    val typography: ChallengeTypoGraphy
        @Composable
        @ReadOnlyComposable
        get() = LocalTypography.current
}
