package com.lwg.challenge.remote.api

import com.lwg.challenge.remote.model.ranking.LoserRankingResponse
import com.lwg.challenge.remote.network.util.ApiResult
import de.jensklingenberg.ktorfit.http.GET

/**
 * 개돼지(패배) 랭킹 조회 API.
 *
 * - 인증: Bearer JWT (네트워크 레이어 공통 헤더에서 주입).
 * - 서버는 항상 HTTP 200 + body.code 로 성공/에러 구분 (ADR-0002).
 */
interface RankingApi {

    @GET("api/v1/rankings/losers")
    suspend fun getLoserRanking(): ApiResult<LoserRankingResponse>
}
