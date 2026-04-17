package com.lwg.challenge.local.datastore.model

import kotlinx.serialization.Serializable

@Serializable
internal data class AppSettingsPrefs(
    val isDarkMode: Boolean = false,
    val language: String = "ko",
    val isNotificationEnabled: Boolean = true,
    val fontSize: Int = 14,
)
