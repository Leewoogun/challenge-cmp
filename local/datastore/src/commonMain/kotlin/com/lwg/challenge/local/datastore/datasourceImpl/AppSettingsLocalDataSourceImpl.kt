package com.lwg.challenge.local.datastore.datasourceImpl

import com.lwg.challenge.data.datasource.local.AppSettingsLocalDataSource
import com.lwg.challenge.domain.model.ThemeMode
import com.lwg.challenge.local.datastore.datastore.AppSettingsDataStore
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single(binds = [AppSettingsLocalDataSource::class])
internal class AppSettingsLocalDataSourceImpl(
    private val appSettingsDataStore: AppSettingsDataStore,
) : AppSettingsLocalDataSource {

    override fun observeThemeMode(): Flow<ThemeMode> = appSettingsDataStore.observeThemeMode()

    override suspend fun saveThemeMode(themeMode: ThemeMode) {
        appSettingsDataStore.saveThemeMode(themeMode)
    }
}
