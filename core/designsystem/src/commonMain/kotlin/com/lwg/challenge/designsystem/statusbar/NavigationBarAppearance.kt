package com.lwg.challenge.designsystem.statusbar

import androidx.compose.runtime.Composable

/** 하단 내비게이션 바 아이콘 색을 앱 테마에 맞춘다. 시스템 다크 모드와 앱 테마가 다를 수 있어서다. */
@Composable
expect fun SetNavigationBarAppearance(darkTheme: Boolean)
