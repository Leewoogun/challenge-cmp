package com.lwg.challenge.remote.model.user

import com.lwg.challenge.remote.model.BaseResponse
import kotlinx.serialization.Serializable

/**
 * DELETE /api/v1/users/me
 *
 * `data` 필드를 선언하지 않는다 — 이 엔드포인트는 `error`/`code`/`message` 3필드짜리
 * BaseResponse 를 그대로 반환하므로 `data` 키 자체가 응답에 없다. 키가 없는 상황에서는
 * nullable 로 선언해도 기본값이 없으면 MissingFieldException 이 난다.
 */
@Serializable
class WithdrawResponse : BaseResponse()
