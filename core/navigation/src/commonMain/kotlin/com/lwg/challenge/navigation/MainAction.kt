package com.lwg.challenge.navigation

import androidx.compose.runtime.compositionLocalOf

interface MainAction {
    fun showSnackBar(message: String)
    fun finishApp()

    /** 토큰·캐시 정리 후 로그인 화면으로 이동. 세션 만료 처리와 같은 경로를 탄다. */
    fun logout()

    /**
     * 탈퇴가 이미 성공한 뒤에 부른다 — 서버 호출 없이 로그인 화면으로만 보낸다.
     * [logout] 과 목적지가 같아 같은 경로를 재사용한다.
     */
    fun withdrawn()
}

val LocalMainAction = compositionLocalOf<MainAction> {
    error("No MainAction provided")
}
