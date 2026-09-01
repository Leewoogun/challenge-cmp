package com.lwg.challenge.feature.notification.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.lwg.challenge.domain.model.notification.Notification
import com.lwg.challenge.navigation.Route
import com.lwg.challenge.push.PushEvent
import com.lwg.challenge.push.toRoute
import com.lwg.challenge.utils.datetime.toElapsedKoreanString
import kotlinx.datetime.LocalDateTime

/**
 * 알림 목록 화면 상태. design.md §6 상태 매트릭스.
 *
 * [Empty] 와 [Failure] 를 한 상태로 뭉개지 않는다 — *"알림이 없다"* 와 *"알림을 못 불러왔다"* 는
 * 사용자가 취할 행동이 정반대다(전자는 나가면 되고, 후자는 다시 시도해야 한다). 실패를 스낵바로만
 * 알리는 일반 규칙의 예외인 이유는 첫 조회 실패에는 **남길 목록 자체가 없어서**다 — 스낵바가 사라지면
 * 복구 수단이 함께 사라진다. `ArchiveContent.Failure` 가 같은 자리에서 내린 판단이다.
 */
@Stable
sealed interface NotificationUiState {

    @Immutable
    data object Loading : NotificationUiState

    @Immutable
    data class Data(
        val items: List<NotificationItemState>,
        val append: NotificationAppendState = NotificationAppendState.Idle,
    ) : NotificationUiState

    @Immutable
    data object Empty : NotificationUiState

    @Immutable
    data object Failure : NotificationUiState
}

/**
 * 목록 **마지막 한 슬롯**의 상태. design.md §6.3.
 *
 * 다음 쪽 조회는 이 슬롯만 쓴다 — 전체 화면 로딩·전체 화면 실패로 올리면 읽던 목록이 사라지고
 * 스크롤 위치를 잃는다. 첫 조회 실패만 [NotificationUiState.Failure] 로 화면을 덮는데, 그때는
 * 비울 목록이 애초에 없다.
 *
 * [Idle] 이 *"대기"* 와 *"끝 도달"* 을 겸한다. 둘 다 화면에 아무것도 그리지 않으므로 가를 이유가
 * 없고, 무엇보다 *"여기까지"* 문구를 놓을 자리를 만들지 않는다 — 사용자가 안 찾던 경계를 알려주는
 * 것이다. 더 받을 것이 남았는지는 커서를 쥔 ViewModel 만 알고, 화면은 마지막 항목에 닿았다고
 * 알리기만 한다.
 */
@Stable
sealed interface NotificationAppendState {

    @Immutable
    data object Idle : NotificationAppendState

    @Immutable
    data object Loading : NotificationAppendState

    @Immutable
    data object Failed : NotificationAppendState
}

/**
 * 알림 1건 UI State.
 *
 * `now` 는 ViewModel 의 매핑 시점에 1회 측정해 전 항목이 같은 값을 쓴다 — 항목마다 시계를 읽으면
 * 한 목록 안에서 기준 시각이 어긋나고 테스트가 불가능해진다 (`ReceivedChallengeItemState` 선례).
 * 화면이 살아 있는 동안 `"3분 전"` 이 `"4분 전"` 으로 바뀌지 않아도 된다 — 마감 카운트다운과 달리
 * 경과 시간은 긴박하지 않다.
 */
@Immutable
data class NotificationItemState(
    private val model: Notification,
    private val now: LocalDateTime,
) {

    val notificationId: Long get() = model.notificationId
    val type: String get() = model.type
    val body: String get() = model.body

    /** 마감 카운트다운인 `toRelativeKoreanString` 과 방향이 반대다. 그쪽에 넘기면 전부 "마감" 이 된다. */
    val elapsedText: String get() = model.createdAt.toElapsedKoreanString(now)

    /**
     * 탭 목적지. 푸시 수신과 같은 한 벌([PushEvent.of] + [toRoute])을 쓴다 — 사본을 만들면 같은
     * 알림이 어디서 눌렸는지에 따라 다른 화면으로 가는 날이 온다.
     *
     * get() 이 아니라 생성 시점 val 인 이유: `Route.Challenge.Detail` 이 data class 라 매 호출마다
     * 새 인스턴스가 나오면 이 값을 받는 하위 Composable 의 skipping 이 깨진다
     * (CLAUDE.md "UI State 파싱 규칙").
     */
    val route: Route? = PushEvent.of(model.type, model.referenceId)?.toRoute()

    /** 모르는 타입이거나 참조 대상이 없으면 목적지를 특정할 수 없다. 그래도 목록에서 숨기지는 않는다. */
    val isTappable: Boolean get() = route != null
}
