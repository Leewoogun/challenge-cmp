package com.lwg.challenge.feature.notification.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.lwg.challenge.domain.model.notification.Notification
import com.lwg.challenge.navigation.Route
import com.lwg.challenge.push.PushEvent
import com.lwg.challenge.push.toRoute
import com.lwg.challenge.utils.datetime.toClockTimeText
import com.lwg.challenge.utils.datetime.toDateGroupLabel
import com.lwg.challenge.utils.datetime.toElapsedKoreanString
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

@Stable
sealed interface NotificationUiState {

    @Immutable
    data object Loading : NotificationUiState

    /**
     * @property now 첫 쪽을 받을 때 1회 잰 기준 시각. 쪽이 추가돼도 유지해 한 목록의 경과 시간을 한 시계로 계산한다.
     * @property append 목록 마지막 슬롯(다음 쪽 로딩·실패) 상태.
     */
    @Immutable
    data class Data(
        val notifications: List<Notification>,
        val now: LocalDateTime,
        val append: NotificationAppendState = NotificationAppendState.Idle,
    ) : NotificationUiState {

        // 쪽이 아니라 누적 목록에서 묶어야 페이지 경계에 걸친 날짜에 헤더가 두 번 찍히지 않는다. 새 List 라 get() 대신 val.
        val dateGroups: List<NotificationDateGroup> = notifications.groupByDateRun(today = now.date)

        /** 오늘이면 경과 시간, 아니면 `HH:mm`. 날짜는 그룹 헤더가 이미 말한다. */
        fun timeTextOf(notification: Notification): String =
            if (notification.createdAt.date == now.date) {
                notification.createdAt.toElapsedKoreanString(now)
            } else {
                notification.createdAt.toClockTimeText()
            }

        /** 목록에서 탭했을 때 갈 곳. 목적지는 푸시와 같은 [toRoute] 를 쓰고, 목록만 [isTappableInList] 게이트를 얹는다. */
        fun routeOf(notification: Notification): Route? =
            PushEvent.of(notification.type, notification.referenceId)
                ?.takeIf { it.isTappableInList(notification.referencePending) }
                ?.toRoute()
    }

    /** 0건과 실패는 사용자가 할 일이 달라(나가기 vs 다시 시도) 한 상태로 합치지 않는다. */
    @Immutable
    data object Empty : NotificationUiState

    /** 첫 조회 실패. 남길 목록이 없어 스낵바 대신 화면이 재시도 수단을 준다. */
    @Immutable
    data object Error : NotificationUiState
}

@Immutable
data class NotificationDateGroup(
    val label: String,
    val notifications: List<Notification>,
) {

    // 라벨엔 연도가 없어 같은 라벨 그룹이 둘 생길 수 있다. 라벨로 key 를 만들면 LazyColumn 이 중복 key 로 터진다.
    val headerKey: String get() = "notification-date-group-${notifications.first().notificationId}"
}

/** 같은 날짜가 연속으로 놓인 구간만 묶는다. 서버가 준 최신순을 다시 정렬하지 않는다. */
private fun List<Notification>.groupByDateRun(today: LocalDate): List<NotificationDateGroup> = buildList {
    var current = mutableListOf<Notification>()

    fun flush() {
        if (current.isEmpty()) return
        add(NotificationDateGroup(current.first().createdAt.date.toDateGroupLabel(today), current.toList()))
        current = mutableListOf()
    }

    this@groupByDateRun.forEach { notification ->
        if (current.isNotEmpty() && current.last().createdAt.date != notification.createdAt.date) flush()
        current.add(notification)
    }
    flush()
}

/** 다음 쪽 조회는 마지막 한 슬롯만 쓴다. 전체 화면으로 올리면 읽던 목록과 스크롤 위치를 잃는다. */
@Stable
sealed interface NotificationAppendState {

    /** 대기와 끝 도달을 겸한다. 둘 다 아무것도 그리지 않는다. */
    @Immutable
    data object Idle : NotificationAppendState

    @Immutable
    data object Loading : NotificationAppendState

    @Immutable
    data object Failed : NotificationAppendState
}

/**
 * 목록에서 이 행을 탭할 수 있는가. 푸시는 방금 온 사건이라 막지 않지만, 목록엔 이미 끝난 대상의 행이 남는다.
 * `else` 를 쓰지 않아 새 타입이 생기면 [toRoute] 와 여기 양쪽이 컴파일 에러로 답을 요구한다.
 */
private fun PushEvent.isTappableInList(referencePending: Boolean?): Boolean = when (this) {
    // 수락·거절은 홈 '받은 도전장' 에만 있어 이미 응답했거나 만료된 신청은 갈 자리가 없다.
    is PushEvent.ChallengeRequest -> referencePending == true
    // 상대 인증 사진은 챌린지가 끝난 뒤에도 상세에 남는다.
    is PushEvent.OpponentVerified -> true
    is PushEvent.ChallengeAccepted -> false
    is PushEvent.ChallengeRejected -> false
    // 목록에서 이동하지 않는 것이 사용자 결정이라 referencePending 에 매달지 않고 타입에서 닫는다.
    is PushEvent.ChallengeForfeited -> false
}
