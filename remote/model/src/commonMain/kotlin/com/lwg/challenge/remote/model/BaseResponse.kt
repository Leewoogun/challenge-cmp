package com.lwg.challenge.remote.model

import kotlinx.serialization.Serializable

/**
 * 서버 공통 응답 envelope (ADR-0002).
 *
 * 항상 HTTP 200 을 유지하고, body 의 [code] 로 성공/에러를 구분한다.
 * - code == 200 : 성공
 * - code != 200 : 비즈니스 에러 — ApiResultConverterFactory 가 ApiResult.Failure.CustomError 로 변환.
 *
 * 구체 응답은 이 클래스를 상속해 `data: XxxData` 필드를 추가한다.
 */
@Serializable
open class BaseResponse(
    val error: Boolean = false,
    val code: Int = 0,
    val message: String = "",
)
