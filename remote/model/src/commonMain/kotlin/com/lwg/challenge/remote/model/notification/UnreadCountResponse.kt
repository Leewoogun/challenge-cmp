package com.lwg.challenge.remote.model.notification

import com.lwg.challenge.remote.model.BaseResponse
import kotlinx.serialization.Serializable

/**
 * GET /api/v1/notifications/unread-count 와 POST /api/v1/notifications/read-all 의 **공통** 응답.
 *
 * ```
 * { "error": false, "code": 200, "message": "", "data": { "unreadCount": 3 } }
 * ```
 *
 * 둘이 답하는 것이 *"지금 안 읽은 게 몇 개냐"* 하나라 서버가 shape 을 통일했다. 앱도 뱃지 갱신
 * 경로를 한 벌만 갖는다 — read-all 을 불렀으니 0 이라는 **규칙**을 앱에 두지 않고 언제나 서버가
 * 준 값 하나만 본다. 나중에 범위 지정 read-all 이 생겨도 그 사본이 틀릴 일이 없다.
 */
@Serializable
data class UnreadCountResponse(
    val data: UnreadCountData = UnreadCountData(),
) : BaseResponse()

@Serializable
data class UnreadCountData(
    val unreadCount: Int = 0,
)
