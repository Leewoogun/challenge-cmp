package com.lwg.challenge.remote.model.notification

import com.lwg.challenge.remote.model.BaseResponse
import com.lwg.challenge.remote.model.serializer.WireLocalDateTimeSerializer
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/** GET /api/v1/notifications — 이 앱에서 유일하게 커서 페이지네이션을 쓰는 목록. */
@Serializable
data class NotificationListResponse(
    val data: NotificationListData = NotificationListData(),
) : BaseResponse()

@Serializable
data class NotificationListData(
    val notifications: List<NotificationDto> = emptyList(),
    /**
     * 다음 쪽의 `cursor`. **`null` 이면 끝이다.**
     *
     * null 이어도 키는 항상 실린다(서버 Jackson 에 `@JsonInclude(NON_NULL)` 설정이 없다).
     */
    val nextCursor: Long? = null,
)

/**
 * 알림 1건.
 *
 * `title` 과 `isRead` 는 응답에 **없다** — 목록이 `body` 만 그리기로 했고(푸시용 헤드라인은 목록에서
 * 홀로 서지 못한다), 읽음 정책이 *"목록을 열면 전부"* 라 행별 읽음 상태라는 것이 존재하지 않는다.
 * 서버 테스트가 두 필드의 부재를 고정한다.
 */
@Serializable
data class NotificationDto(
    val notificationId: Long = 0L,
    /**
     * 🔴 **enum 이 아니라 String 이다.** 서버 enum 9종 중 5종만 발송 중이라(2026-09-05 기준)
     * 값 추가가 예정돼 있고, typed enum 은 새 타입 한 행이 **응답 전체**의 역직렬화를 깨뜨려
     * 알림 화면이 통째로 빈다.
     * `ActiveChallengeDto.status` 가 같은 판단의 선례다.
     */
    val type: String = "",
    /** 발송 시점에 서버가 박제한 문구. 🔴 앱이 타입을 보고 다시 조립하거나 덮어쓰지 않는다. */
    val body: String = "",
    /** 목적지 id. 참조 대상이 타입에 따라 다르고, 대상이 없는 타입이 붙을 수 있어 nullable 이다. */
    val referenceId: Long? = null,
    /**
     * 참조 대상이 아직 응답을 기다리는가. `true` 대기 / `false` 처리·만료·종료·삭제 /
     * `null` 이 타입에는 대기라는 개념이 없음.
     *
     * `null` 이어도 키는 항상 실린다 — [nextCursor] 와 같은 이유다.
     */
    val referencePending: Boolean? = null,
    /** `yyyy-MM-dd HH:mm:ss` (KST). 포맷이 어긋나면 null. */
    @Serializable(with = WireLocalDateTimeSerializer::class)
    val createdAt: LocalDateTime? = null,
)
