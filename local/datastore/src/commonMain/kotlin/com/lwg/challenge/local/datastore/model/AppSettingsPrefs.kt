package com.lwg.challenge.local.datastore.model

import com.lwg.challenge.domain.model.ThemeMode
import kotlinx.serialization.Serializable

@Serializable
internal data class AppSettingsPrefs(
    val themeMode: String = ThemeMode.DARK.name,
    val language: String = "ko",
    val isNotificationEnabled: Boolean = true,
    val fontSize: Int = 14,
)
