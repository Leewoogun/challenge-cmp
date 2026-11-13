package com.lwg.challenge.domain.usecase

import com.lwg.challenge.domain.model.ThemeMode
import com.lwg.challenge.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow

class ObserveThemeModeUseCase(
    private val themeRepository: ThemeRepository,
) {

    operator fun invoke(): Flow<ThemeMode> = themeRepository.observeThemeMode()
}
