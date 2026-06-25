package com.lwg.challenge.domain.repository

import com.lwg.challenge.domain.model.friend.Friend
import com.lwg.challenge.domain.model.friend.FriendRequest
import com.lwg.challenge.domain.model.friend.UserSearchResult
import kotlinx.coroutines.flow.Flow

/**
 * 친구 시스템 Repository.
 *
 * 메모리 규칙 (Repository 표준 패턴 — spec §6.2):
 * - 검색 / 목록 = `Flow<T>` + `onError: (Throwable) -> Unit` 콜백
 * - mutation = `suspend` + kotlin stdlib `Result<Unit>`
 * - 401 처리는 RepositoryImpl 내부에서 `AuthEventBus.emit(Unauthorized)` (홈 화면 패턴 동일)
 * - 도메인 sealed Result 만들지 말 것 (stdlib Result 는 OK)
 *
 * spec: docs/features/friends/spec-friend-add.md §6
 * api-contract: docs/features/friends/api-contract-friend-add.md
 */
interface FriendsRepository {

    /**
     * 닉네임 contains 검색 (LIMIT 20). 최소 2자 입력은 호출자가 보장.
     * 본인 제외, ACTIVE 유저만.
     */
    fun searchUsers(
        nickname: String,
        onError: (Throwable) -> Unit,
    ): Flow<List<UserSearchResult>>

    /** 친구 목록 (ACCEPTED). `accepted_at DESC` 정렬. */
    fun listFriends(
        onError: (Throwable) -> Unit,
    ): Flow<List<Friend>>

    /** 받은 친구 요청 목록 (PENDING). `created_at DESC` 정렬. */
    fun listReceivedRequests(
        onError: (Throwable) -> Unit,
    ): Flow<List<FriendRequest>>

    /**
     * 친구 요청 보내기. 신규 INSERT 또는 동일 방향 REJECTED row → PENDING UPDATE.
     * 실패 시 `Result.failure` (메시지는 throwable.message).
     */
    suspend fun sendRequest(receiverId: Long): Result<Unit>

    /** 받은 PENDING 요청 수락. */
    suspend fun acceptRequest(requestId: Long): Result<Unit>

    /** 받은 PENDING 요청 거절 (row 보존). */
    suspend fun rejectRequest(requestId: Long): Result<Unit>

    /** 내가 보낸 PENDING 요청 취소 (물리 삭제). */
    suspend fun cancelRequest(requestId: Long): Result<Unit>
}
