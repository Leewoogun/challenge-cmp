package com.lwg.challenge.domain.model

data class AppSettings(
    val isDarkMode: Boolean = false,
    val language: String = "ko",
    val isNotificationEnabled: Boolean = true,
    val fontSize: Int = 14,
)
