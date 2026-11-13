package com.lwg.challenge.local.datastore.datastore

import com.lwg.challenge.domain.model.AppSettings
import com.lwg.challenge.domain.model.ThemeMode
import com.lwg.challenge.local.datastore.createDataStore
import com.lwg.challenge.local.datastore.model.AppSettingsPrefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
internal class AppSettingsDataStore {

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

    fun observeThemeMode(): Flow<ThemeMode> {
        return dataStore.data.map { it.themeMode.toThemeMode() }
    }

    suspend fun saveThemeMode(themeMode: ThemeMode) {
        dataStore.updateData { it.copy(themeMode = themeMode.name) }
    }

    suspend fun reset() {
        dataStore.updateData { AppSettingsPrefs() }
    }
}

private fun AppSettingsPrefs.toAppSettings() = AppSettings(
    themeMode = themeMode.toThemeMode(),
    language = language,
    isNotificationEnabled = isNotificationEnabled,
    fontSize = fontSize,
)

private fun AppSettings.toPrefs() = AppSettingsPrefs(
    themeMode = themeMode.name,
    language = language,
    isNotificationEnabled = isNotificationEnabled,
    fontSize = fontSize,
)

// 저장된 값이 enum 에서 사라졌어도 앱이 깨지지 않게 기본값으로 떨어뜨린다.
private fun String.toThemeMode(): ThemeMode =
    ThemeMode.entries.find { it.name == this } ?: ThemeMode.DARK
