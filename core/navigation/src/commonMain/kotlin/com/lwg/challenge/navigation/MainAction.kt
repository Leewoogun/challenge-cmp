package com.lwg.challenge.navigation

import androidx.compose.runtime.compositionLocalOf

interface MainAction {
    fun showSnackBar(message: String)
    fun finishApp()

    /** 토큰·캐시 정리 후 로그인 화면으로 이동. 세션 만료 처리와 같은 경로를 탄다. */
    fun logout()
}

val LocalMainAction = compositionLocalOf<MainAction> {
    error("No MainAction provided")
}
