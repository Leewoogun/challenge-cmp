package com.lwg.challenge.feature.challenge.create.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme

/**
 * @param overlay true 면 아래 화면 위에 반투명 배경으로 덮고 터치를 막는다 (제출 대기용).
 */
@Composable
internal fun ChallengeCreateLoading(
    modifier: Modifier = Modifier,
    overlay: Boolean = false,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (overlay) {
                    Modifier
                        .background(ChallengeTheme.colorScheme.background.screen.copy(alpha = 0.6f))
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    awaitPointerEvent().changes.forEach { it.consume() }
                                }
                            }
                        }
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = ChallengeTheme.colorScheme.point)
    }
}

@ThemePreviews
@Composable
private fun ChallengeCreateLoadingOverlayPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background.screen)) {
            ChallengeCreateLoading(overlay = true)
        }
    }
}
