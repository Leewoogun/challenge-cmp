package com.lwg.challenge.local.datastore.datasource

import com.lwg.challenge.local.datastore.createDataStore
import com.lwg.challenge.local.datastore.model.AppSettingsPrefs
import com.lwg.challenge.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
internal class AppSettingsLocalDataSource {

    private val dataStore = createDataStore(
        serializer = AppSettingsPrefs.serializer(),
        defaultValue = AppSettingsPrefs(),
        fileName = "app_settings",
    )

    fun observeSettings(): Flow<AppSettings> {
        return dataStore.data.map { it.toAppSettings() }
    }

    suspend fun updateSettings(settings: AppSettings) {
        dataStore.updateData { settings.toPrefs() }
    }

    suspend fun reset() {
        dataStore.updateData { AppSettingsPrefs() }
    }
}

private fun AppSettingsPrefs.toAppSettings() = AppSettings(
    isDarkMode = isDarkMode,
    language = language,
    isNotificationEnabled = isNotificationEnabled,
    fontSize = fontSize,
)

private fun AppSettings.toPrefs() = AppSettingsPrefs(
    isDarkMode = isDarkMode,
    language = language,
    isNotificationEnabled = isNotificationEnabled,
    fontSize = fontSize,
)
