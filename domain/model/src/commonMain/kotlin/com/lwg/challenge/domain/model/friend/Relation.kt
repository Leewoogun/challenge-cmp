package com.lwg.challenge.domain.model.friend

/**
 * 검색 결과에서 "현재 사용자(me) ↔ target user" 관계를 표현하는 derived enum.
 *
 * 서버가 `friendships` row 를 보고 계산해서 응답에 박아 내려준다 — DB 컬럼 아님.
 *
 * | 값 | 조건 | UI 액션 |
 * |---|---|---|
 * | [NONE] | row 없음 | "친구 요청" 버튼 |
 * | [REQUEST_SENT] | 내가 보낸 PENDING | "요청 보냄" + "X 취소" |
 * | [REQUEST_RECEIVED] | 내가 받은 PENDING | "수락" 버튼 |
 * | [FRIEND] | 어느 방향이든 ACCEPTED | "이미 친구" 뱃지 |
 * | [REJECTED] | 내가 보낸 REJECTED | "다시 요청" 버튼 |
 *
 * BLOCKED 는 1차 미구현 — 검색 결과로 내려오지 않음.
 *
 * spec: docs/features/friends/spec-friend-add.md §4.3
 * api-contract: docs/features/friends/api-contract-friend-add.md §공통 enum / §1
 */
enum class Relation {
    NONE,
    REQUEST_SENT,
    REQUEST_RECEIVED,
    FRIEND,
    REJECTED,
    ;

    companion object {
        /**
         * 서버 문자열 → enum. 알 수 없는 값은 [NONE] 으로 fallback.
         * (Domain skill 규칙: enum 조회 실패 시 기본값 반환, 예외 throw 금지.)
         */
        fun fromCode(code: String): Relation =
            entries.find { it.name == code } ?: NONE
    }
}
