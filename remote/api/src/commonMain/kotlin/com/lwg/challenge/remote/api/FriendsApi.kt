package com.lwg.challenge.remote.api

import com.lwg.challenge.remote.model.friend.AcceptFriendRequestResponse
import com.lwg.challenge.remote.model.friend.CancelFriendRequestResponse
import com.lwg.challenge.remote.model.friend.FriendChallengesResponse
import com.lwg.challenge.remote.model.friend.FriendRecordResponse
import com.lwg.challenge.remote.model.friend.FriendsResponse
import com.lwg.challenge.remote.model.friend.ReceivedFriendRequestsResponse
import com.lwg.challenge.remote.model.friend.RejectFriendRequestResponse
import com.lwg.challenge.remote.model.friend.SendFriendRequestBody
import com.lwg.challenge.remote.model.friend.SendFriendRequestResponse
import com.lwg.challenge.remote.model.friend.UserSearchResponse
import com.lwg.challenge.remote.network.util.ApiResult
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query

/**
 * 친구 시스템 API (검색 / 요청 / 수락 / 거절 / 취소 / 목록 / 받은 요청).
 */
interface FriendsApi {

    /** GET /api/v1/users/search?nickname={q} — 닉네임 contains 검색 (LIMIT 20). min 2자. */
    @GET("api/v1/users/search")
    suspend fun searchUsers(
        @Query("nickname") nickname: String,
    ): ApiResult<UserSearchResponse>

    /** POST /api/v1/friends/requests — 친구 요청 보내기 (신규 INSERT 또는 REJECTED→PENDING UPDATE). */
    @POST("api/v1/friends/requests")
    suspend fun sendRequest(
        @Body body: SendFriendRequestBody,
    ): ApiResult<SendFriendRequestResponse>

    /** POST /api/v1/friends/requests/{id}/accept — 받은 PENDING 요청 수락. */
    @POST("api/v1/friends/requests/{id}/accept")
    suspend fun acceptRequest(
        @Path("id") requestId: Long,
    ): ApiResult<AcceptFriendRequestResponse>

    /** POST /api/v1/friends/requests/{id}/reject — 받은 PENDING 요청 거절. row 보존. */
    @POST("api/v1/friends/requests/{id}/reject")
    suspend fun rejectRequest(
        @Path("id") requestId: Long,
    ): ApiResult<RejectFriendRequestResponse>

    /** DELETE /api/v1/friends/requests/{id} — 내가 보낸 PENDING 요청 취소 (물리 삭제). */
    @DELETE("api/v1/friends/requests/{id}")
    suspend fun cancelRequest(
        @Path("id") requestId: Long,
    ): ApiResult<CancelFriendRequestResponse>

    /** GET /api/v1/friends — 친구 목록 (ACCEPTED). `accepted_at DESC`. */
    @GET("api/v1/friends")
    suspend fun getFriendList(): ApiResult<FriendsResponse>

    /** GET /api/v1/friends/requests/received — 받은 요청 목록 (PENDING). `created_at DESC`. */
    @GET("api/v1/friends/requests/received")
    suspend fun getReceivedRequestList(): ApiResult<ReceivedFriendRequestsResponse>

    /** GET /api/v1/friends/{friendId}/record — 이 친구와의 스코어 + 진행 중 챌린지(`deadline ASC`). */
    @GET("api/v1/friends/{friendId}/record")
    suspend fun getFriendRecord(
        @Path("friendId") friendId: Long,
    ): ApiResult<FriendRecordResponse>

    /**
     * GET /api/v1/friends/{friendId}/challenges — 이 친구와의 지난 챌린지. `id DESC`, 키 기반 페이지네이션.
     *
     * @param nextChallengeId 직전 응답의 `nextChallengeId` 를 그대로 보낸다. 포함 경계(`id <= `)라 마지막 항목 id 를 보내면 그 항목이 중복된다
     * @param size `1..50`. 범위 밖은 서버가 깎지 않고 code 700 으로 거부한다
     */
    @GET("api/v1/friends/{friendId}/challenges")
    suspend fun getFriendChallenges(
        @Path("friendId") friendId: Long,
        @Query("nextChallengeId") nextChallengeId: Long?,
        @Query("size") size: Int,
    ): ApiResult<FriendChallengesResponse>
}
