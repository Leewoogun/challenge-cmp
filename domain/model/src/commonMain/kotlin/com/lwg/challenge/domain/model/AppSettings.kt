package com.lwg.challenge.domain.model

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val language: String = "ko",
    val isNotificationEnabled: Boolean = true,
    val fontSize: Int = 14,
)
