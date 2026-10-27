package com.lwg.challenge.remote.api

import com.lwg.challenge.remote.model.notification.NotificationListResponse
import com.lwg.challenge.remote.model.notification.UnreadCountResponse
import com.lwg.challenge.remote.network.util.ApiResult
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Query

/**
 * 알림 목록 / 안 읽은 개수 / 전체 읽음 API.
 *
 * - 인증: Bearer JWT (네트워크 레이어 공통 헤더에서 주입).
 * - 서버는 항상 HTTP 200 + body.code 로 성공/에러 구분 (ADR-0002).
 */
interface NotificationApi {

    /**
     * GET /api/v1/notifications — 알림 목록. `id DESC`(= 최신순), 키 기반 페이지네이션.
     *
     * @param nextNotificationId 🔴 **직전 응답의 `nextNotificationId` 를 그대로 되돌려 보낸다.** null 이면 쿼리
     *   파라미터가 붙지 않아 맨 앞부터 온다 — 첫 호출이 파라미터 없이 성립하는 것이 설계이고, 앱은 키를
     *   **만들지 않는다**. 조건이 `id <= nextNotificationId`(포함 경계)라 마지막 항목 id 를 보내면 그 항목이
     *   다음 쪽에 한 번 더 온다. 존재하지 않는 id 를 보내도 에러가 아니다.
     * @param size `1..50`. 🔴 서버가 범위 밖을 조용히 clamp 하지 않고 code 700 으로 거부한다 —
     *   깎아 주면 앱이 요청한 개수를 받았다고 믿고 쪽을 잘못 이어 붙인다.
     */
    @GET("api/v1/notifications")
    suspend fun getNotifications(
        @Query("nextNotificationId") nextNotificationId: Long?,
        @Query("size") size: Int,
    ): ApiResult<NotificationListResponse>

    /** GET /api/v1/notifications/unread-count — 홈 벨 뱃지용 안 읽은 개수. */
    @GET("api/v1/notifications/unread-count")
    suspend fun getUnreadCount(): ApiResult<UnreadCountResponse>

    /**
     * POST /api/v1/notifications/read-all — 전부 읽음 처리. 요청 body 가 없고 멱등하다.
     *
     * 갱신 **후 실제로 센** 개수를 돌려준다([UnreadCountResponse]). 서버가 0 을 하드코딩하지 않는
     * 것과 앱이 0 을 가정하지 않는 것이 같은 이유다.
     */
    @POST("api/v1/notifications/read-all")
    suspend fun markAllAsRead(): ApiResult<UnreadCountResponse>
}
