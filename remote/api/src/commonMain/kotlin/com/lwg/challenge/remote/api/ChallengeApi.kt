package com.lwg.challenge.remote.api

import com.lwg.challenge.remote.model.challenge.AcceptChallengeBody
import com.lwg.challenge.remote.model.challenge.AcceptChallengeResponse
import com.lwg.challenge.remote.model.challenge.CancelChallengeResponse
import com.lwg.challenge.remote.model.challenge.ChallengeCreateBody
import com.lwg.challenge.remote.model.challenge.ChallengeCreateResponse
import com.lwg.challenge.remote.model.challenge.ChallengeDetailResponse
import com.lwg.challenge.remote.model.challenge.ChallengeHistoryResponse
import com.lwg.challenge.remote.model.challenge.ForfeitChallengeResponse
import com.lwg.challenge.remote.model.challenge.ReceivedChallengesResponse
import com.lwg.challenge.remote.model.challenge.RejectChallengeResponse
import com.lwg.challenge.remote.network.util.ApiResult
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query

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
     * GET /api/v1/challenges/history — 계약서 보관함 한 달치 (`COMPLETED`). `challengeDate DESC`.
     *
     * [month] 를 생략하면 **서버 KST 기준 이번 달**이 온다. 앱이 기기 시계로 첫 달을 만들지 않게 하려는
     * 것이 목적이다 — 기기 타임존이 KST 가 아니면 상한이 한 달 어긋난다.
     *
     * @param month `yyyy-MM`, **zero-padding 필수** (`2026-3` 은 서버가 code 700 으로 거부한다).
     *   null 이면 쿼리 파라미터가 아예 붙지 않아 위의 "이번 달" 이 된다.
     *   미래 달·형식 오류는 code 700 + HTTP 200 이라 기존 실패 경로로 흐른다.
     */
    @GET("api/v1/challenges/history")
    suspend fun getChallengeHistories(
        @Query("month") month: String?,
    ): ApiResult<ChallengeHistoryResponse>

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

    /**
     * POST /api/v1/challenges/{id}/forfeit — 기권. 즉시 COMPLETED 확정, 상대 승. 본문 없음.
     *
     * 기권자는 JWT 주체다 — `userId` 를 body/query 로 받지 않는다. 받으면 남의 챌린지를 기권시킬
     * 문을 여는 것이다.
     */
    @POST("api/v1/challenges/{id}/forfeit")
    suspend fun forfeitChallenge(
        @Path("id") challengeId: Long,
    ): ApiResult<ForfeitChallengeResponse>

    /** DELETE /api/v1/challenges/{id} — 내가 보낸 PENDING 도전장 취소 (물리 삭제). */
    @DELETE("api/v1/challenges/{id}")
    suspend fun cancelChallenge(
        @Path("id") challengeId: Long,
    ): ApiResult<CancelChallengeResponse>
}
