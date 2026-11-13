package com.lwg.challenge.data.repository

import com.lwg.challenge.data.datasource.local.AppSettingsLocalDataSource
import com.lwg.challenge.domain.model.ThemeMode
import com.lwg.challenge.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single(binds = [ThemeRepository::class])
class ThemeRepositoryImpl(
    private val appSettingsLocalDataSource: AppSettingsLocalDataSource,
) : ThemeRepository {

    override fun observeThemeMode(): Flow<ThemeMode> = appSettingsLocalDataSource.observeThemeMode()

    override suspend fun saveThemeMode(themeMode: ThemeMode) {
        appSettingsLocalDataSource.saveThemeMode(themeMode)
    }
}
