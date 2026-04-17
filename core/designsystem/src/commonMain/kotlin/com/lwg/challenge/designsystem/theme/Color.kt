package com.lwg.challenge.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val LocalColorScheme = staticCompositionLocalOf { ChallengeColorScheme.lightColorScheme }
// Primary Colors - 주요 액션 및 강조 색상
val Orange1 = Color(0xFFFF7033)
val Orange2 = Color(0xFFF49D25)
val Orange3 = Color(0xFFF2F0E7)

// Secondary
val Red1 = Color(0xFFEF4444)
val Red2 = Color(0xFFEF4444).copy(alpha = 0.1f)

// Background Colors - 배경 색상
val Ivory1 = Color(0xFFFCFAF8)
val Ivory2 = Color(0xFFF3ECE2)

val White = Color(0xFFFFFFFF)
val Gray50 = Color(0xFFF9FAFB)

// Text Colors - 텍스트 색상
val FontBlack = Color(0xFF32221B)

val Brown1 = Color(0xFF93796C)
val Brown2 = Color(0xFF53392D)

val GradientColor = Brush.horizontalGradient(
    colors = listOf(
        Orange1,
        Orange2
    )
)

@Immutable
data class ChallengeColorScheme(
    val orange1: Color,
    val orange2: Color,
    val orange3: Color,
    val red1: Color,
    val red2: Color,
    val ivory1: Color,
    val ivory2: Color,
    val white: Color,
    val gray50: Color,
    val fontBlack: Color,
    val brown1: Color,
    val brown2: Color,
    val gradientColor: Brush
) {
    companion object {
        val lightColorScheme = ChallengeColorScheme(
            orange1 = Orange1,
            orange2 = Orange2,
            orange3 = Orange3,
            red1 = Red1,
            red2 = Red2,
            ivory1 = Ivory1,
            ivory2 = Ivory2,
            white = White,
            gray50 = Gray50,
            fontBlack = FontBlack,
            brown1 = Brown1,
            brown2 = Brown2,
            gradientColor = GradientColor
        )
    }
}
