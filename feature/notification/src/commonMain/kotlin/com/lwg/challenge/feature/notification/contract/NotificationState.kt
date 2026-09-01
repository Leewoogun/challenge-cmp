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
    ) : NotificationUiState {

        /**
         * 날짜 그룹. design.md §3.6.
         *
         * 🔴 **쪽이 아니라 누적된 [items] 전체에서 파생한다.** 한 날짜가 페이지 경계에 걸칠 때
         * (1쪽 끝과 2쪽 시작이 둘 다 같은 날) 쪽마다 헤더를 붙이는 구현은 **같은 헤더를 두 번** 찍는다.
         *
         * get() 이 아니라 생성 시점 val 인 이유: 매 호출마다 새 List 가 나오면 이 값을 받는 하위
         * Composable 의 skipping 이 깨진다 (CLAUDE.md "UI State 파싱 규칙",
         * `RankingUiState.Data.podium` 선례).
         */
        val dateGroups: List<NotificationDateGroup> = items.groupByDateRun()
    }

    @Immutable
    data object Empty : NotificationUiState

    @Immutable
    data object Failure : NotificationUiState
}

/**
 * 날짜 헤더 1개와 그 아래 카드들. design.md §3.6.2.
 *
 * 🔴 서버 계약에 없는 개념이다 — 서버는 **쪽 단위로만 아는 주체**라 원천적으로 만들 수 없다.
 * 계약에 그룹 필드를 넣으면 서버가 모르는 것을 아는 척하게 된다.
 */
@Immutable
data class NotificationDateGroup(
    val label: String,
    val items: List<NotificationItemState>,
) {

    /**
     * `LazyColumn` 헤더 key. 라벨이 아니라 첫 항목 id 로 만든다 — 라벨에는 연도가 없어 같은 라벨의
     * 그룹이 둘 생길 수 있고, 그때 라벨 key 는 중복돼 목록이 터진다. 항목 key 는 `Long` 이라
     * 문자열인 이 값과 겹치지 않는다.
     */
    val headerKey: String get() = "notification-date-group-${items.first().notificationId}"
}

/**
 * 같은 날짜가 **연속으로 놓인 구간**만 한 그룹으로 묶는다.
 *
 * 🔴 정렬하지 않는다 — 서버가 준 최신순이 정본이라 앱이 순서를 다시 세우면 서버가 정렬 규칙을 바꾸는
 * 날 두 규칙이 조용히 어긋난다. 런으로 묶으면 쪽이 몇 번에 걸쳐 들어왔는지와 무관하게 결과가 같다.
 *
 * 라벨이 아니라 [NotificationItemState.createdDate] 로 가르는 이유는 라벨에 연도가 없어서다.
 */
private fun List<NotificationItemState>.groupByDateRun(): List<NotificationDateGroup> = buildList {
    var current = mutableListOf<NotificationItemState>()

    this@groupByDateRun.forEach { item ->
        if (current.isNotEmpty() && current.last().createdDate != item.createdDate) {
            add(NotificationDateGroup(current.first().dateGroupLabel, current.toList()))
            current = mutableListOf()
        }
        current.add(item)
    }

    if (current.isNotEmpty()) {
        add(NotificationDateGroup(current.first().dateGroupLabel, current.toList()))
    }
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

    /** 그룹핑 키. [dateGroupLabel] 은 연도를 담지 않아 키로 쓰면 1년 차이 나는 같은 날이 한 그룹이 된다. */
    val createdDate: LocalDate get() = model.createdAt.date

    /**
     * 날짜 그룹 헤더 문구. design.md §3.6.1.
     *
     * [timeText] 와 축을 나눠 든다 — **그룹이 달력을, 행이 그 날 안에서 언제를** 맡는다.
     */
    val dateGroupLabel: String get() = createdDate.toDateGroupLabel(now.date)

    /**
     * 행의 시각. 오늘이면 상대 표기("방금 전"·"N분 전"·"N시간 전"), 그 밖에는 `HH:mm`.
     *
     * 오늘이 아닌 항목까지 상대 표기를 쓰면 `어제` 헤더 아래 `어제` 행이 반복된다 — 날짜는 헤더가
     * 이미 말했다. 상대 표기가 [toElapsedKoreanString] 인 이유는 마감 카운트다운인
     * `toRelativeKoreanString` 과 방향이 반대라서다. 그쪽에 넘기면 전부 "마감" 이 된다.
     */
    val timeText: String get() = if (createdDate == now.date) {
        model.createdAt.toElapsedKoreanString(now)
    } else {
        model.createdAt.toClockTimeText()
    }

    /**
     * 탭 목적지. **목적지 자체는 푸시 수신과 같은 한 벌([PushEvent.of] + [toRoute])** 이다 — 사본을
     * 만들면 같은 알림이 어디서 눌렸는지에 따라 다른 화면으로 가는 날이 온다. 목록만 그 위에
     * [isTappableInList] 게이트를 얹는다.
     *
     * get() 이 아니라 생성 시점 val 인 이유: `Route.Challenge.Detail` 이 data class 라 매 호출마다
     * 새 인스턴스가 나오면 이 값을 받는 하위 Composable 의 skipping 이 깨진다
     * (CLAUDE.md "UI State 파싱 규칙").
     */
    val route: Route? = PushEvent.of(model.type, model.referenceId)
        ?.takeIf { it.isTappableInList(model.referencePending) }
        ?.toRoute()

    /**
     * 모르는 타입이거나 참조 대상이 없거나 목록 게이트에 걸리면 목적지가 없다.
     * 그래도 목록에서 숨기지는 않는다 — 사람은 [body] 를 읽을 수 있다.
     */
    val isTappable: Boolean get() = route != null
}

/**
 * **목록에서** 이 행을 탭할 수 있는가. 계약 §9.2.
 *
 * 목적지([toRoute])와는 다른 판정이고, 그래서 [toRoute] 를 건드리지 않는다 — 푸시 트레이는 방금
 * 도착한 사건을 여는 것이라 대상이 아직 살아 있지만, 목록에는 며칠 전 행이 그대로 남아 이미 끝난
 * 대상으로 보낼 수 있다. 푸시에 이 조건을 넣으면 막지 말아야 할 푸시까지 함께 막힌다.
 *
 * 판정은 두 단계다. **타입이 먼저 규칙을 정하고**, 규칙이 «조건부» 인 타입에서만 [referencePending]
 * 을 추가로 본다. 🔴 그래서 `null` 을 한쪽으로 접으면 안 된다 — 비탭으로 접으면 대기 개념이 없는
 * 상대 인증이 안 눌리고, 탭 가능으로 접으면 수락·거절이 눌린다. `null` 인 행은 1단계에서 이미
 * 결론이 나 2단계에 닿지 않는다.
 *
 * `else ->` 를 쓰지 않는다. 새 타입이 열리면 [toRoute](목적지)와 이 게이트(목록 탭)가 **양쪽 다**
 * 컴파일 에러를 내 둘 다 답하게 만든다 — `else` 는 그중 한쪽을 조용히 기본값으로 삼킨다.
 *
 * 앱이 모르는 타입은 여기 닿지 않는다. [PushEvent.of] 가 이미 null 이라 «안 됨» 이 공짜로 나온다.
 */
private fun PushEvent.isTappableInList(referencePending: Boolean?): Boolean = when (this) {
    // 수락·거절 액션이 홈 '받은 도전장' 섹션에만 있어, 이미 응답했거나 만료된 신청은 홈에 갈 자리가
    // 없다. 낡은 true 로 탭을 열면 사용자가 빈 자리에 닿는다.
    is PushEvent.ChallengeRequest -> referencePending == true
    // 상대 인증 사진은 챌린지가 끝난 뒤에도 상세에 남는다.
    is PushEvent.OpponentVerified -> true
    is PushEvent.ChallengeAccepted -> false
    is PushEvent.ChallengeRejected -> false
}
