package com.lwg.challenge.domain.usecase

import com.lwg.challenge.domain.model.ThemeMode
import com.lwg.challenge.domain.repository.ThemeRepository

class UpdateThemeModeUseCase(
    private val themeRepository: ThemeRepository,
) {

    suspend operator fun invoke(themeMode: ThemeMode) {
        themeRepository.saveThemeMode(themeMode)
    }
}
