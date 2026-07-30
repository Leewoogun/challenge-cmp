package com.lwg.challenge.remote.api

import com.lwg.challenge.remote.model.challenge.AcceptChallengeBody
import com.lwg.challenge.remote.model.challenge.AcceptChallengeResponse
import com.lwg.challenge.remote.model.challenge.CancelChallengeResponse
import com.lwg.challenge.remote.model.challenge.ChallengeCreateBody
import com.lwg.challenge.remote.model.challenge.ChallengeCreateResponse
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
