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

/**
 * ChallengeTypography — Lovable(웹 디자인 레포) typography 매핑
 *
 * 디자인 단일 소스: `challenge-design/oathbound-challenges/src/styles.css` (Tailwind v4 기본 토큰).
 * 본 파일은 size / lineHeight / fontWeight 만 반영한다.
 * letter-spacing/tracking, leading-none/leading-relaxed 등 line-height 토큰은 무시.
 *
 * ## 사이즈 매핑 (Tailwind v4 기본 토큰)
 *
 * | Tailwind 클래스 | 슬롯       | fontSize | lineHeight |
 * |-----------------|------------|----------|------------|
 * | text-xs         | text12*    | 12sp     | 16sp       |
 * | text-sm         | *14        | 14sp     | 20sp       |
 * | text-base       | *16        | 16sp     | 24sp       |
 * | text-lg         | *18        | 18sp     | 28sp       |
 * | text-xl         | *20        | 20sp     | 28sp       |
 * | text-2xl        | text24*    | 24sp     | 32sp       |
 * | text-3xl        | text30*    | 30sp     | 36sp       |
 * | text-5xl        | bold48     | 48sp     | 48sp       |
 *
 * 비고:
 * - `*22` (light22/medium22/bold22)는 Lovable 디자인에 대응하는 사이즈가 없다.
 *   호출부 보호를 위해 일단 유지하지만, 점진적 제거 후보.
 *
 * ## fontWeight 매핑 (GmarketSans Light/Medium/Bold 3종 자산만 보유)
 *
 * | Lovable 클래스 | 무게 | 모바일 매핑 슬롯 |
 * |----------------|------|------------------|
 * | font-light     | 300  | light* (Light)   |
 * | font-normal    | 400  | light* (Light, Regular 자산 부재)   |
 * | font-medium    | 500  | medium* (Medium) |
 * | font-semibold  | 600  | medium* (Semi 자산 부재 → Medium)   |
 * | font-bold      | 700  | bold* (Bold)     |
 * | font-extrabold | 800  | bold* (Heavy 자산 부재 → Bold)      |
 * | font-black     | 900  | bold* (Black 자산 부재 → Bold)      |
 *
 * 결정 사유: semibold→Medium 매핑은 Bold로 떨어뜨리는 것보다 시각적 무게 차이가 작아
 * 디자인 의도(중간 강조) 보존에 유리. extrabold/black은 추가 자산 없이는 Bold가 최대치.
 * 폰트 자산 추가는 본 스코프 외.
 */
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

    val light30: TextStyle,
    val medium30: TextStyle,
    val bold30: TextStyle,

    val light24: TextStyle,
    val medium24: TextStyle,
    val bold24: TextStyle,

    // design 미사용. 점진적 제거 후보 (호출부 보호 위해 유지)
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

    val light12: TextStyle,
    val medium12: TextStyle,
    val bold12: TextStyle,
)

@Composable
fun createTypography(): ChallengeTypoGraphy {
    val gmarketSansFamily = gmarketSans()
    val baseStyle = gmarketSansStyle(gmarketSansFamily)

    return ChallengeTypoGraphy(
        // text-5xl (48) + Lovable이 leading-none 사용
        bold48 = baseStyle.copy(
            fontSize = 48.sp,
            lineHeight = 48.sp,
            fontWeight = FontWeight.Bold,
        ),
        // text-3xl (30 / 36)
        light30 = baseStyle.copy(
            fontSize = 30.sp,
            lineHeight = 36.sp,
            fontWeight = FontWeight.Light,
        ),
        medium30 = baseStyle.copy(
            fontSize = 30.sp,
            lineHeight = 36.sp,
            fontWeight = FontWeight.Medium,
        ),
        bold30 = baseStyle.copy(
            fontSize = 30.sp,
            lineHeight = 36.sp,
            fontWeight = FontWeight.Bold,
        ),
        // text-2xl (24 / 32)
        light24 = baseStyle.copy(
            fontSize = 24.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Light,
        ),
        medium24 = baseStyle.copy(
            fontSize = 24.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Medium,
        ),
        bold24 = baseStyle.copy(
            fontSize = 24.sp,
            lineHeight = 32.sp,
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
        // text-xl (20 / 28)
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
        // text-lg (18 / 28)
        light18 = baseStyle.copy(
            fontSize = 18.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Light,
        ),
        medium18 = baseStyle.copy(
            fontSize = 18.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Medium,
        ),
        bold18 = baseStyle.copy(
            fontSize = 18.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Bold,
        ),
        // text-base (16 / 24)
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
        // text-sm (14 / 20)
        light14 = baseStyle.copy(
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Light,
        ),
        medium14 = baseStyle.copy(
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Medium,
        ),
        bold14 = baseStyle.copy(
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Bold,
        ),
        // text-xs (12 / 16)
        light12 = baseStyle.copy(
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Light,
        ),
        medium12 = baseStyle.copy(
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Medium,
        ),
        bold12 = baseStyle.copy(
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Bold,
        ),
    )
}
