package com.lwg.challenge.domain.repository

import com.lwg.challenge.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface ThemeRepository {

    fun observeThemeMode(): Flow<ThemeMode>

    suspend fun saveThemeMode(themeMode: ThemeMode)
}
