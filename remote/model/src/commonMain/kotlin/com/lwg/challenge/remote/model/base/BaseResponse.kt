package com.lwg.challenge.remote.model.base

import kotlinx.serialization.Serializable

/**
 * 서버 공통 응답 래퍼. ADR-0002 참조.
 *
 * 항상 HTTP 200 을 유지하고, body 의 [code] 로 성공/에러를 구분한다.
 * - code == 200 : 성공
 * - code == 700/701/702/703/705 : 비즈니스 에러 (UI 처리 구분)
 * - code == 401 : 토큰 만료
 *
 * 구체 응답은 이 클래스를 상속해 `data: XxxData` 필드를 추가한다.
 */
@Serializable
open class BaseResponse(
    val error: Boolean = false,
    val code: Int = 0,
    val message: String = "",
)
