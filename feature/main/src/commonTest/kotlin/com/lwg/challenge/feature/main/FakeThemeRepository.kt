package com.lwg.challenge.feature.main

import com.lwg.challenge.domain.model.ThemeMode
import com.lwg.challenge.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeThemeRepository(initial: ThemeMode = ThemeMode.DARK) : ThemeRepository {

    private val themeMode = MutableStateFlow(initial)

    override fun observeThemeMode(): Flow<ThemeMode> = themeMode

    override suspend fun saveThemeMode(themeMode: ThemeMode) {
        this.themeMode.update { themeMode }
    }
}
