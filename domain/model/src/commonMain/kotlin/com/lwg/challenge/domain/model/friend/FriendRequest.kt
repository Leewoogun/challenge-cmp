@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.lwg.challenge.domain.model.friend

import kotlin.time.Instant

/**
 * 받은 친구 요청 1건 (PENDING).
 *
 * 친구 화면 메인 상단 인라인 섹션 + 배지에 사용.
 * [requestId] 는 수락 / 거절 path 로 사용 (`friendships.id`).
 *
 * api-contract: docs/features/friends/api-contract-friend-add.md §7
 *
 * @property requestId `friendships.id` (PENDING row)
 * @property fromUser 요청 보낸 사용자
 * @property requestedAt 요청 시각 (friendships.created_at)
 */
data class FriendRequest(
    val requestId: Long,
    val fromUser: UserBrief,
    val requestedAt: Instant,
)

/**
 * 친구 요청 발신자의 최소 프로필 (id, nickname, profileImageUrl).
 */
data class UserBrief(
    val userId: Long,
    val nickname: String,
    val profileImageUrl: String?,
)
