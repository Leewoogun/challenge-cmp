package com.lwg.challenge.navigation

import androidx.compose.runtime.compositionLocalOf

interface MainAction {
    fun showSnackBar(message: String)
    fun finishApp()
}

val LocalMainAction = compositionLocalOf<MainAction> {
    error("No MainAction provided")
}
