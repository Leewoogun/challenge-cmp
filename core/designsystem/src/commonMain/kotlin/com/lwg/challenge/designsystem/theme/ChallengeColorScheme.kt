package com.lwg.challenge.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.lwg.challenge.designsystem.theme.color.BackgroundColor
import com.lwg.challenge.designsystem.theme.color.ButtonColor
import com.lwg.challenge.designsystem.theme.color.GradientColor
import com.lwg.challenge.designsystem.theme.color.IconColor
import com.lwg.challenge.designsystem.theme.color.InputFieldColor
import com.lwg.challenge.designsystem.theme.color.LineColor
import com.lwg.challenge.designsystem.theme.color.TextColor

/**
 * Challenge 디자인 시스템의 모든 시멘틱 색상을 담는 단일 진입점.
 *
 * Feature 코드는 `ChallengeTheme.colorScheme.<group>.<name>` 으로만 색에 접근한다.
 * (Material3 [androidx.compose.material3.MaterialTheme.colorScheme] 직접 사용 금지)
 *
 * @property point 브랜드 오렌지. 진행바 · 강조 배경처럼 텍스트/아이콘/버튼이 아닌 자리에 쓴다.
 */
@Immutable
data class ChallengeColorScheme(
    val background: BackgroundColor,
    val text: TextColor,
    val icon: IconColor,
    val button: ButtonColor,
    val line: LineColor,
    val inputField: InputFieldColor,
    val gradient: GradientColor,

    val point: Color,
    val error: Color,
    val success: Color,
    val warning: Color,
)

internal val DarkChallengeColorScheme = ChallengeColorScheme(
    background = BackgroundColor(
        screen = black2,
        card = black3,
        cardSub = gray4,
        field = gray1,
    ),
    text = TextColor(
        primary = white1,
        secondary = gray5,
        point = orange1,
        onPoint = black1,
    ),
    icon = IconColor(
        primary = white1,
        secondary = gray5,
        point = orange1,
        onPoint = black1,
    ),
    button = ButtonColor(
        background = orange1,
        text = black1,
        subBackground = gray4,
        subText = white1,
        border = gray3,
    ),
    line = LineColor(
        border = gray3,
        divider = gray3,
        dividerSub = gray4,
    ),
    inputField = InputFieldColor(
        background = gray1,
        text = white1,
        hint = gray5,
        cursor = orange1,
        focusedBorder = orange1,
    ),
    gradient = GradientColor(
        fireStart = orange1,
        fireEnd = orange2,
        cardStart = black4,
        cardEnd = black5,
        glow = orange1.copy(alpha = 0.15f),
    ),
    point = orange1,
    error = red1,
    success = green1,
    warning = yellow1,
)

internal val LightChallengeColorScheme = ChallengeColorScheme(
    background = BackgroundColor(
        screen = light1,
        card = white2,
        cardSub = light2,
        field = light3,
    ),
    text = TextColor(
        primary = black1,
        secondary = gray7,
        point = orange2,
        onPoint = black1,
    ),
    icon = IconColor(
        primary = black1,
        secondary = gray7,
        point = orange1,
        onPoint = black1,
    ),
    button = ButtonColor(
        background = orange1,
        text = black1,
        subBackground = light2,
        subText = black1,
        border = light4,
    ),
    line = LineColor(
        border = light4,
        divider = light4,
        dividerSub = light2,
    ),
    inputField = InputFieldColor(
        background = light3,
        text = black1,
        hint = gray7,
        cursor = orange1,
        focusedBorder = orange1,
    ),
    gradient = GradientColor(
        fireStart = orange1,
        fireEnd = orange2,
        cardStart = white2,
        cardEnd = light5,
        glow = orange1.copy(alpha = 0.12f),
    ),
    point = orange1,
    error = red1,
    success = green1,
    warning = yellow2,
)

internal val LocalChallengeColorScheme = staticCompositionLocalOf<ChallengeColorScheme> {
    error("ChallengeColorScheme not provided. Wrap your composable in ChallengeTheme {}.")
}
