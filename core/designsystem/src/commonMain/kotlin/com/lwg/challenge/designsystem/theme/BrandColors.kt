package com.lwg.challenge.designsystem.theme

import androidx.compose.ui.graphics.Color

/**
 * External brand colors that are fixed by the brand owner and must NOT be themed.
 *
 * These colors are intentionally kept outside of [ChallengeColorScheme] /
 * [ChallengeExtendedColors]. They never change with theme or mode.
 *
 * Source: Kakao official brand guideline. Cross-checked with Lovable
 * `src/components/ui/button.tsx` (`variant="kakao"`) and the design-system catalog
 * at `challenge_hub/docs/design-system/colors.md` (section 3).
 */
object BrandColors {
    /** Kakao Yellow — login button background. */
    val KakaoYellow: Color = Color(0xFFFEE500)

    /** Kakao Yellow pressed/hover variant. Lovable uses this for `:hover` only;
     *  Kakao official guideline does not specify a pressed shade — keep here for
     *  optional use, otherwise prefer system ripple. */
    val KakaoYellowPressed: Color = Color(0xFFFDD835)

    /** Kakao label color — used for text and the talk icon on Kakao Yellow. */
    val KakaoLabel: Color = Color(0xFF191919)
}
