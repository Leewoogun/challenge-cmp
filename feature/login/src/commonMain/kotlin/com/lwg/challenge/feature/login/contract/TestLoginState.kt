package com.lwg.challenge.feature.login.contract

import androidx.compose.runtime.Immutable

internal const val TEST_LOGIN_UNSUPPORTED_MESSAGE = "이 서버는 테스트 로그인을 지원하지 않아요"

/**
 * 테스트 로그인(debug 전용) 섹션의 UI 상태.
 *
 * @property isVisible `isDebug` 결과. release 에서는 false 라 섹션 자체가 렌더되지 않는다.
 * @property isUnavailable 서버가 테스트 로그인 미지원(404/401)을 한 번이라도 응답했는가.
 */
@Immutable
data class TestLoginUiState(
    val isVisible: Boolean = false,
    val isUnavailable: Boolean = false,
) {
    val isEnabled: Boolean get() = isVisible && !isUnavailable
    val caption: String? get() = if (isUnavailable) TEST_LOGIN_UNSUPPORTED_MESSAGE else null
}

/** 테스트 계정 번호. 서버 닉네임 `테스터{no}` 와 라벨을 일치시킨다. */
enum class TestAccount(val no: Int) {
    TESTER_1(1), TESTER_2(2), TESTER_3(3);

    val label: String get() = "테스터$no"
}
