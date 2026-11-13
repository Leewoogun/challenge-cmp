package com.lwg.challenge.designsystem.preview

import androidx.compose.ui.tooling.preview.Preview

// android.content.res.Configuration 은 commonMain 에서 못 쓰므로 값을 직접 적는다 (UI_MODE_TYPE_NORMAL | UI_MODE_NIGHT_*).
private const val UI_MODE_LIGHT = 0x11
private const val UI_MODE_DARK = 0x21

/**
 * 라이트 / 다크 두 테마로 Preview 를 그린다.
 *
 * `ChallengeTheme { }` 의 기본값이 `isSystemInDarkTheme()` 이라 `uiMode` 에 따라 테마가 바뀐다.
 */
@Preview(name = "1. Light", uiMode = UI_MODE_LIGHT, showBackground = true, backgroundColor = 0xFFF4F4F6)
@Preview(name = "2. Dark", uiMode = UI_MODE_DARK, showBackground = true, backgroundColor = 0xFF26272F)
annotation class ThemePreviews
