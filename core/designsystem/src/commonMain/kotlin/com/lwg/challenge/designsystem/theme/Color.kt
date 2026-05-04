package com.lwg.challenge.designsystem.theme

import androidx.compose.ui.graphics.Color

/**
 * Raw color palette for the Challenge (MAENGSE) design system — 단일 다크 톤.
 *
 * **Feature 코드는 이 raw 토큰을 직접 import 하지 않는다.** 다음 경로로만 색에 접근한다:
 * - 시멘틱 색상: `ChallengeTheme.colorScheme.<name>`
 * - 그라데이션: `ChallengeTheme.brushes.<name>`
 * - 외부 브랜드: `BrandColors.<name>`
 *
 * 토큰 이름은 색상 자체(orange1, black2 등)를 가리키고, 의미(primary/background)는
 * `ChallengeColorScheme.kt` 의 `DefaultChallengeColorScheme` 에서 매핑한다.
 */

// Orange
internal val orange1 = Color(0xFFE97A3D)
internal val orange2 = Color(0xFFD75C3A)

// Red
internal val red1 = Color(0xFFD75C4A)

// Yellow
internal val yellow1 = Color(0xFFD7AF45)

// Green
internal val green1 = Color(0xFF3CAB7A)

// Blue
internal val blue1 = Color(0xFF5E91C9)

// Black (dark surfaces & deep text)
internal val black1 = Color(0xFF1A1B22)
internal val black2 = Color(0xFF26272F)
internal val black3 = Color(0xFF2F303A)
internal val black4 = Color(0xFF31323D)
internal val black5 = Color(0xFF2B2C36)

// Gray
internal val gray1 = Color(0xFF3D3E48)
internal val gray2 = Color(0xFF43444F)
internal val gray3 = Color(0xFF43444E)
internal val gray4 = Color(0xFF383942)
internal val gray5 = Color(0xFF9596A0)
internal val gray6 = Color(0xFFD2D2D6)

// White
internal val white1 = Color(0xFFF2F2F4)
