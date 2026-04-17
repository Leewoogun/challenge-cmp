package com.lwg.challenge.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import challenge.core.designsystem.generated.resources.GmarketSansTTFBold
import challenge.core.designsystem.generated.resources.GmarketSansTTFLight
import challenge.core.designsystem.generated.resources.GmarketSansTTFMedium
import challenge.core.designsystem.generated.resources.Res
import org.jetbrains.compose.resources.Font

val LocalTypography = staticCompositionLocalOf<ChallengeTypoGraphy> {
    error("ChallengeTypoGraphy를 provide 해야합니다.")
}

val TextUnit.nonScaledSp
    @Composable
    @ReadOnlyComposable
    get() = (this.value / LocalDensity.current.fontScale).sp

val TextStyle.nonScaledSp
    @Composable
    @ReadOnlyComposable
    get() = this.copy(
        fontSize = this.fontSize.nonScaledSp,
        lineHeight = this.lineHeight.nonScaledSp,
    )

@Composable
fun gmarketSans() = FontFamily(
    Font(Res.font.GmarketSansTTFLight, FontWeight.Light),
    Font(Res.font.GmarketSansTTFMedium, FontWeight.Medium),
    Font(Res.font.GmarketSansTTFBold, FontWeight.Bold),
)

private fun gmarketSansStyle(fontFamily: FontFamily) = TextStyle(
    fontFamily = fontFamily,
    fontWeight = FontWeight.Normal,
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None,
    ),
)

@Immutable
data class ChallengeTypoGraphy(
    val bold48: TextStyle,

    val light22: TextStyle,
    val medium22: TextStyle,
    val bold22: TextStyle,

    val light20: TextStyle,
    val medium20: TextStyle,
    val bold20: TextStyle,

    val light18: TextStyle,
    val medium18: TextStyle,
    val bold18: TextStyle,

    val light16: TextStyle,
    val medium16: TextStyle,
    val bold16: TextStyle,

    val light14: TextStyle,
    val medium14: TextStyle,
    val bold14: TextStyle,
)

@Composable
fun createTypography(): ChallengeTypoGraphy {
    val gmarketSansFamily = gmarketSans()
    val baseStyle = gmarketSansStyle(gmarketSansFamily)

    return ChallengeTypoGraphy(
        bold48 = baseStyle.copy(
            fontSize = 48.sp,
            lineHeight = 60.sp,
            fontWeight = FontWeight.Bold,
        ),
        light22 = baseStyle.copy(
            fontSize = 22.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight.Light,
        ),
        medium22 = baseStyle.copy(
            fontSize = 22.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight.Medium,
        ),
        bold22 = baseStyle.copy(
            fontSize = 22.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight.Bold,
        ),
        light20 = baseStyle.copy(
            fontSize = 20.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Light,
        ),
        medium20 = baseStyle.copy(
            fontSize = 20.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Medium,
        ),
        bold20 = baseStyle.copy(
            fontSize = 20.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Bold,
        ),
        light18 = baseStyle.copy(
            fontSize = 18.sp,
            lineHeight = 26.sp,
            fontWeight = FontWeight.Light,
        ),
        medium18 = baseStyle.copy(
            fontSize = 18.sp,
            lineHeight = 26.sp,
            fontWeight = FontWeight.Medium,
        ),
        bold18 = baseStyle.copy(
            fontSize = 18.sp,
            lineHeight = 26.sp,
            fontWeight = FontWeight.Bold,
        ),
        light16 = baseStyle.copy(
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Light,
        ),
        medium16 = baseStyle.copy(
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Medium,
        ),
        bold16 = baseStyle.copy(
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Bold,
        ),
        light14 = baseStyle.copy(
            fontSize = 14.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Light,
        ),
        medium14 = baseStyle.copy(
            fontSize = 14.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Medium,
        ),
        bold14 = baseStyle.copy(
            fontSize = 14.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Bold,
        ),
    )
}