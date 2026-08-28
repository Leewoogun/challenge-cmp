package com.lwg.challenge.domain.model

/**
 * 로그아웃이 시작된 이유. 서버에 알릴지 말지가 여기서 갈린다.
 *
 * @property USER 사용자가 직접 로그아웃하거나 계정을 전환했다. 서버 세션을 끊어야 한다.
 * @property SESSION_EXPIRED 토큰 갱신이 이미 실패해 세션이 끝났다. 서버에 알릴 것이 없다.
 * @property WITHDRAWN 회원탈퇴가 성공했다. 계정 자체가 사라져 서버에 끊을 세션이 없다.
 */
enum class LogoutReason {
    USER,

    // SESSION_EXPIRED 와 합치지 않는다. 둘 다 지금은 "서버를 부르지 않는다" 로 같은 처리를 받지만
    // 원인이 다르다 — 만료는 복구(재로그인)되고 탈퇴는 계정이 없어진다. 처리가 갈릴 자리다.
    SESSION_EXPIRED,
    WITHDRAWN,
}
