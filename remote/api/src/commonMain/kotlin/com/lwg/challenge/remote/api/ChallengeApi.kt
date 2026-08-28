package com.lwg.challenge.remote.api

import com.lwg.challenge.remote.model.challenge.AcceptChallengeBody
import com.lwg.challenge.remote.model.challenge.AcceptChallengeResponse
import com.lwg.challenge.remote.model.challenge.CancelChallengeResponse
import com.lwg.challenge.remote.model.challenge.ChallengeCreateBody
import com.lwg.challenge.remote.model.challenge.ChallengeCreateResponse
import com.lwg.challenge.remote.model.challenge.ChallengeDetailResponse
import com.lwg.challenge.remote.model.challenge.ChallengeHistoryResponse
import com.lwg.challenge.remote.model.challenge.ReceivedChallengesResponse
import com.lwg.challenge.remote.model.challenge.RejectChallengeResponse
import com.lwg.challenge.remote.network.util.ApiResult
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path

/**
 * 챌린지 신청 / 응답 API (신청 / 받은 도전장 / 수락 / 거절 / 취소).
 *
 * 진행 중 챌린지 조회는 [ActiveChallengeApi] 담당.
 */
interface ChallengeApi {

    /** POST /api/v1/challenges — 챌린지 신청 (PENDING 생성). 마감 시각 환산은 서버가 한다. */
    @POST("api/v1/challenges")
    suspend fun createChallenge(
        @Body body: ChallengeCreateBody,
    ): ApiResult<ChallengeCreateResponse>

    /** GET /api/v1/challenges/{id} — 챌린지 상세 + 계약서 + 양측 서명. */
    @GET("api/v1/challenges/{id}")
    suspend fun getChallengeDetail(
        @Path("id") challengeId: Long,
    ): ApiResult<ChallengeDetailResponse>

    /**
     * GET /api/v1/challenges/history — 계약서 보관함 목록 (`COMPLETED` 전체). `challengeDate DESC`.
     *
     * 파라미터가 없다. 월 필터·페이지네이션 둘 다 계약에서 빼기로 확정했고, 월 그룹핑은 앱이 한다
     * (api-contract §1) — 서버가 그룹을 나눠 주면 응답 shape 에 화면 구조가 박힌다.
     */
    @GET("api/v1/challenges/history")
    suspend fun getChallengeHistories(): ApiResult<ChallengeHistoryResponse>

    /** GET /api/v1/challenges/received — 받은 도전장 목록 (PENDING, 마감 미경과). `created_at DESC`. */
    @GET("api/v1/challenges/received")
    suspend fun getReceivedChallenges(): ApiResult<ReceivedChallengesResponse>

    /** POST /api/v1/challenges/{id}/accept — 받은 도전장 수락 + 내 미션 입력 → IN_PROGRESS. */
    @POST("api/v1/challenges/{id}/accept")
    suspend fun acceptChallenge(
        @Path("id") challengeId: Long,
        @Body body: AcceptChallengeBody,
    ): ApiResult<AcceptChallengeResponse>

    /** POST /api/v1/challenges/{id}/reject — 받은 도전장 거절 → REJECTED. row 보존. */
    @POST("api/v1/challenges/{id}/reject")
    suspend fun rejectChallenge(
        @Path("id") challengeId: Long,
    ): ApiResult<RejectChallengeResponse>

    /** DELETE /api/v1/challenges/{id} — 내가 보낸 PENDING 도전장 취소 (물리 삭제). */
    @DELETE("api/v1/challenges/{id}")
    suspend fun cancelChallenge(
        @Path("id") challengeId: Long,
    ): ApiResult<CancelChallengeResponse>
}
