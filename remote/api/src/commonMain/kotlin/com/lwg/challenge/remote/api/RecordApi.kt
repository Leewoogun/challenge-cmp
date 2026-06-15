package com.lwg.challenge.remote.api

import com.lwg.challenge.remote.model.record.RecordResponse
import com.lwg.challenge.remote.network.util.ApiResult
import de.jensklingenberg.ktorfit.http.GET

/**
 * 내 누적 전적(record) 조회 API.
 *
 * - 인증: Bearer JWT (네트워크 레이어 공통 헤더에서 주입).
 * - 서버는 항상 HTTP 200 + body.code 로 성공/에러 구분 (ADR-0002).
 */
interface RecordApi {

    @GET("api/v1/record")
    suspend fun getMyRecord(): ApiResult<RecordResponse>
}
