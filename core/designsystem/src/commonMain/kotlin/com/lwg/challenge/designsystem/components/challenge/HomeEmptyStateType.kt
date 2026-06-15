package com.lwg.challenge.designsystem.components.challenge

/**
 * [HomeEmptyState] 빈 상태 분기.
 *
 * - [FIRST_USER] : 가입 직후 전적 0 + 챌린지 0 — "이제 시작" 톤.
 * - [NO_ACTIVE_CHALLENGE] : 누적 전적 있음, 진행 중 챌린지만 0 — "다시 시작" 톤.
 */
enum class HomeEmptyStateType {
    FIRST_USER,
    NO_ACTIVE_CHALLENGE,
}
