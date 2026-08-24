package com.lwg.challenge.remote.api

import com.lwg.challenge.remote.model.verification.ChallengeVerificationsResponse
import com.lwg.challenge.remote.model.verification.SubmitVerificationResponse
import com.lwg.challenge.remote.network.util.ApiResult
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Header
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import io.ktor.client.request.forms.MultiPartFormDataContent

/**
 * 챌린지 인증 API (제출 / 양측 현황 조회).
 *
 * 사진 업로드는 [submitVerification] 한 번으로 끝난다 — 별도 발급/업로드 단계가 없다.
 */
interface VerificationApi {

    /**
     * POST /api/v1/challenges/{id}/verification — JPEG 를 multipart 로 제출해 내 인증 확정 → VERIFIED.
     *
     * [contentType] 을 명시적으로 넘기는 이유: 기본 HttpClient 의 `DefaultRequest` 가
     * `Content-Type: application/json` 을 싣는데, Ktor 는 요청 헤더의 Content-Type 을
     * body(OutgoingContent)의 contentType 보다 우선한다. 그대로 두면 multipart 의 `boundary` 가
     * 헤더에서 사라져 서버가 파싱하지 못한다. 그래서 호출부가
     * [MultiPartFormDataContent.contentType](boundary 포함)을 직접 실어 기본값을 이긴다.
     */
    @POST("api/v1/challenges/{id}/verification")
    suspend fun submitVerification(
        @Path("id") challengeId: Long,
        // HttpHeaders.ContentType 은 const 가 아니라 어노테이션 인자로 쓸 수 없다
        @Header("Content-Type") contentType: String,
        @Body body: MultiPartFormDataContent,
    ): ApiResult<SubmitVerificationResponse>

    /** GET /api/v1/challenges/{id}/verifications — 양측 인증 상태 + 만료 있는 읽기 URL. */
    @GET("api/v1/challenges/{id}/verifications")
    suspend fun getVerifications(
        @Path("id") challengeId: Long,
    ): ApiResult<ChallengeVerificationsResponse>
}
