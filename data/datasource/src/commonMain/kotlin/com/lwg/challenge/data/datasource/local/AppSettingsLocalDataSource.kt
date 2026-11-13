package com.lwg.challenge.data.datasource.local

import com.lwg.challenge.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface AppSettingsLocalDataSource {

    fun observeThemeMode(): Flow<ThemeMode>

    suspend fun saveThemeMode(themeMode: ThemeMode)
}
