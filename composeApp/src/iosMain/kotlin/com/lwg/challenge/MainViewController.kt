package com.lwg.challenge

import androidx.compose.ui.window.ComposeUIViewController
import com.lwg.challenge.feature.main.MainRoute
import org.koin.compose.KoinApplication

fun MainViewController() = ComposeUIViewController {
    KoinApplication(
        application = cookingAppDeclaration(),
    ) {
        MainRoute()
    }
}
