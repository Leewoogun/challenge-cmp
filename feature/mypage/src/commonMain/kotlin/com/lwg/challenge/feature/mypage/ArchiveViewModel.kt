package com.lwg.challenge.feature.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lwg.challenge.domain.usecase.GetChallengeArchiveUseCase
import com.lwg.challenge.feature.mypage.contract.ArchiveChallengeItemState
import com.lwg.challenge.feature.mypage.contract.ArchiveContent
import com.lwg.challenge.feature.mypage.contract.ArchiveMonthCursor
import com.lwg.challenge.feature.mypage.contract.ArchiveUiEffect
import com.lwg.challenge.feature.mypage.contract.ArchiveUiState
import com.lwg.challenge.utils.flow.TriggerStateFlow
import com.lwg.challenge.utils.flow.triggerStateIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.YearMonth
import org.koin.android.annotation.KoinViewModel

/**
 * 계약서 보관함 ViewModel. 한 번에 한 달치만 조회하고, 화살표로 달을 옮긴다.
 *
 * 파이프라인은 `selectedMonth(StateFlow) → flatMapLatest → 그 달의 조회 flow` 하나다. 달을 바꾸는
 * 것이 곧 새 조회이고, [flatMapLatest] 가 직전 달 구독을 끊는 것이 stale 응답에 대한 1차 방어다.
 *
 * `WhileSubscribed(0)` 이라 상세를 보고 돌아오면 재구독으로 보고 있던 달이 다시 조회된다 —
 * 보관함에는 갱신을 알리는 콜백이 없어 그게 유일한 갱신 경로다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@KoinViewModel
class ArchiveViewModel(
    private val getChallengeArchiveUseCase: GetChallengeArchiveUseCase,
) : ViewModel() {

    // 이 채널에는 **화면이 실패를 말할 자리가 없는 실패**가 흐른다. 두 가지다:
    // - 부분 실패. 화면의 주 내용이 성공분 목록이라 그 위에 스낵바를 겹쳐 띄운다 (design.md §2.5.4.1).
    // - 월 전환의 전체 실패. 이전 달을 그대로 유지하기로 해서 본문이 실패 카드로 바뀌지 않는다
    //   ([monthFlow]). 화면이 이전 달을 말하는 동안 실패를 말할 채널이 여기뿐이다.
    //
    // 첫 로딩의 전체 실패만 예외로 여기를 타지 않는다 — 그때는 본문 전체가 곧 실패 영역이라
    // 화면이 직접 말한다.
    //
    // 1차의 `viewModelScope.launch { emit }` 을 버퍼 + tryEmit 으로 바꿨다. 그 코루틴은 flatMapLatest
    // 취소를 타지 않아, 8월의 부분 실패 스낵바가 9월 화면 위에 뜰 수 있었다. tryEmit 은 정지하지 않아
    // 별도 코루틴으로 분리할 이유가 없고, 무엇보다 **나중에 발화할 보류된 emit 자체가 없다.**
    //
    // replay 는 0 을 유지한다 — 구독자가 붙기 전 이펙트가 버려지는 성질에 기존 테스트가 의존한다.
    private val _uiEffect = MutableSharedFlow<ArchiveUiEffect>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val uiEffect: SharedFlow<ArchiveUiEffect> = _uiEffect.asSharedFlow()

    // 둘 다 요청한 달이 아니라 **사용자·세션 전역 값**이고, 계약상 요청 월과 무관하게 매 응답 같은 값이
    // 온다. 달을 넘어가며 들고 있는 것이 옳다.
    //
    // firstArchivedMonth 는 과거 하한이 폐기된 뒤로 **이동 경계에 관여하지 않는다.** 그렇다고 월 값이
    // 안 쓰이는 것은 아니다 — 빈 상태 2종을 가르는 것 외에, 첫 기록보다 이전 달의 부제
    // ("첫 기록은 2026년 5월이에요")가 이 월을 그대로 출력한다 (ArchiveUiState.emptyMonthSubtitle).
    // 화면에 그대로 넘기되 이 값으로 `<` 를 막지는 않는다.
    //
    // 반대로 부분 실패 메시지는 그 달 1회 수집에만 유효해서 [monthFlow] 안의 지역 변수여야 한다 —
    // 여기 프로퍼티로 두면 재수집 때 직전 실패 메시지가 엉뚱한 스낵바로 샌다.
    // 수명이 다른 값은 두는 자리가 다르다.
    private var thisMonth: YearMonth? = null
    private var firstArchivedMonth: YearMonth? = null

    // null 은 "달을 모른다" 가 아니라 **"서버가 정한 이번 달"** 이다. 앱이 기기 시계로 첫 달을 정하지
    // 않기 때문에 첫 요청은 항상 파라미터 없이 나간다.
    private val selectedMonth = MutableStateFlow<YearMonth?>(null)

    val uiState: TriggerStateFlow<ArchiveUiState> = selectedMonth
        .flatMapLatest(::monthFlow)
        .triggerStateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(0),
            // 첫 로딩은 기록 유무를 아직 모른다. null 은 "기록이 없다" 와 같은 값이지만 이 순간에는
            // 어느 쪽으로도 화면이 갈리지 않는다 — 본문이 Loading 이라 빈 상태 분기에 닿지 않고,
            // month 도 null 이라 CTA 조건(이번 달일 것)이 애초에 성립하지 않는다.
            initialValue = ArchiveUiState(
                month = null,
                content = ArchiveContent.Loading,
                firstArchivedMonth = null,
            ),
        )

    /**
     * 한 달치 조회의 전 구간을 **완성된 화면 상태**로 흘린다.
     *
     * 헤더와 본문을 따로 흘리지 않는(`combine` 하지 않는) 이유: 따로 흘리면 화살표를 누른 순간
     * 헤더만 새 달로 바뀌고 본문은 아직 옛 달 카드인 **찢어진 중간 상태**가 한 프레임 보인다.
     * 이 flow 는 헤더와 본문을 언제나 **한 값에 담아** 보내므로 짝이 원자적으로만 바뀐다 —
     * 타이밍 방어가 아니라 구조적으로 불가능해진다.
     *
     * ## 월 전환은 응답이 올 때까지 화면을 건드리지 않는다
     *
     * 직전 판은 옛 화면을 100ms 만 붙들었다가 본문을 비웠는데(`ArchiveContent.Blank`),
     * **느린 응답에서는 그 빈 화면이 그대로 깜빡임이었다.** 붙드는 시간을 늘리는 것으로는 끝이 나지
     * 않는다 — 응답보다 짧으면 언제든 빈 화면이 나오기 때문이다. 그래서 비우는 것 자체를 없앴다.
     * 응답이 도착할 때까지 **이전 달이 온전히 남는다**: 라벨도 건수도 카드도 전부 이전 달이라
     * **한 화면이 두 달을 말하지 않는다.** 교체는 응답이 왔을 때 한 번에 일어난다.
     *
     * **이전 달을 유지하는 것은 혼합이 아니다.** 금지된 것은 `7월 라벨 + 8월 카드` 처럼 한 화면이
     * 두 달을 말하는 *혼합* 이고, 이전 달 전체는 **이미 참이었던 짝**이다. 헤더와 본문을 한 값으로
     * 보내는 구조가 그대로라 혼합은 여전히 표현조차 되지 않는다.
     *
     * 응답이 [LOADING_HINT_DELAY_MILLIS] 를 넘겨 늦으면 그때만 화면에 힌트를 얹는데, 그것도
     * 본문이 아니라 **건수 슬롯**이 진다 ([ArchiveUiState.countText]) — 바가 이미 비워 두던 자리라
     * **새 자리를 만들지 않고, 따라서 기하가 변하지 않는다.** 라벨과 목록은 그동안에도 이전 달이다.
     * 그래서 `ArchiveContent.Blank` 와 [ArchiveContent.Loading] 은 월 전환에서 더는 쓰이지 않는다.
     *
     * ## 전환 실패는 화면을 덮지 않는다
     *
     * 실패가 본문 전체를 덮어도 된다는 근거는 *"잃을 성공 데이터가 애초에 없다"* 였다
     * (design.md §2.5.4.1). **월 전환에서는 그 전제가 뒤집힌다** — 화면에는 이전 달의 성공한 목록이
     * 그대로 있고, 실패 카드로 덮으면 정확히 그 손실이 일어난다. 이전 달을 남기면 실패를 말할 자리가
     * 화면에 없으므로 **스낵바가 유일한 채널**이다. ⚠️ **첫 로딩 실패만 예외** — 그때는 화면에
     * 아무것도 없어 실패 카드가 맞고, 그 자리의 "다시 시도" 가 유일한 복구 수단이다.
     *
     * ## 대가
     *
     * 화살표를 눌러도 **월 라벨이 곧바로 움직이지 않는다.** 탭 피드백은 상태 전환이 아니라
     * **화살표 눌림 효과**가 진다 (`MonthArrowButton`). ⚠️ 응답이 느릴 때 연타하면 의도보다 멀리
     * 갈 수 있는데, 그때 답은 **[LOADING_HINT_DELAY_MILLIS] 를 낮추는 것이지 라벨을 먼저 움직이는
     * 것이 아니다** — 라벨만 앞서면 화면이 아직 사실이 아닌 것을 말하게 되고, 그게 방금 걷어낸
     * 혼합이다.
     *
     * ## 구현 메모
     *
     * `onStart`/`onEmpty` 대신 [channelFlow] 안에서 직접 보내는 이유: 힌트 타이머가 조회와
     * **경쟁**해야 하는데 `onStart` 에서 `delay` 하면 **요청 자체가 그만큼 늦어진다.**
     * 별도 코루틴으로 띄우고 응답이 오면 죽인다.
     */
    private fun monthFlow(requested: YearMonth?): Flow<ArchiveUiState> = channelFlow {
        val isFirstLoad = thisMonth == null

        // 전환 시작 시점의 화면. 응답이 올 때까지 이 값이 그대로 남고, 실패하면 여기로 되돌아온다.
        val previous = uiState.value

        if (isFirstLoad) send(stateOf(requested, ArchiveContent.Loading))

        val hint = if (isFirstLoad) {
            null
        } else {
            launch {
                delay(LOADING_HINT_DELAY_MILLIS)
                // 이전 달을 그대로 둔 채 건수 슬롯만 힌트로 바꾼다. 라벨도 목록도 건드리지 않는다.
                send(previous.copy(isTransitioning = true))
            }
        }

        // onError 하나가 전체 실패와 부분 실패 둘 다를 나른다. 둘을 가르는 것은 emit 이 뒤따르는지다 —
        // 뒤따르면 부분 실패라 성공분 위에 스낵바를 겹치고, 안 따르면 전체 실패라 아래에서 처리한다.
        var pendingMessage: String? = null
        var delivered = false

        getChallengeArchiveUseCase(month = requested, onError = { pendingMessage = it })
            .collect { archive ->
                // 2차 방어. 1차는 flatMapLatest 의 구독 취소지만, 연타로 요청이 겹치고 응답 순서가
                // 뒤집히면 **바는 6월인데 목록은 7월**이 될 수 있다. 그리기 직전 한 줄이라 비용이 0이다.
                //
                // 첫 요청은 requested 가 null 이라 검증을 건너뛴다 — 그 응답으로 이번 달을 배우는
                // 중이라 비교할 기준이 아직 없다.
                if (requested != null && archive.month != requested) return@collect

                // cancel() 이 아니라 cancelAndJoin() 인 이유: 힌트 코루틴이 이미 send 에 진입했다면
                // cancel 만으로는 그 send 가 Data 뒤에 도착하는 역전을 막지 못한다.
                hint?.cancelAndJoin()

                // 상한은 "이번 달" 인데, 파라미터 없이 부른 **첫 응답의 에코만** 그 값이다.
                // 이후 `?month=2026-05` 응답의 에코는 5월이라, 덮으면 상한이 과거로 내려간다.
                if (thisMonth == null) thisMonth = archive.month
                firstArchivedMonth = archive.firstArchivedMonth

                pendingMessage?.let { message ->
                    pendingMessage = null
                    _uiEffect.tryEmit(ArchiveUiEffect.ShowMessage(message))
                }

                delivered = true
                send(
                    ArchiveUiState(
                        month = cursorFor(archive.month),
                        content = ArchiveContent.Data(archive.histories.map(::ArchiveChallengeItemState)),
                        firstArchivedMonth = firstArchivedMonth,
                    ),
                )
            }

        hint?.cancelAndJoin()

        if (!delivered) {
            if (isFirstLoad) {
                // 실패를 값으로 만들지 않으면 상류가 아무것도 보내지 않은 채 끝나 본문이 로딩에 갇힌다.
                send(stateOf(requested, ArchiveContent.Failure))
            } else {
                _uiEffect.tryEmit(ArchiveUiEffect.ShowMessage(pendingMessage ?: TRANSITION_FAILURE_MESSAGE))
                // 힌트가 이미 떴다면 그것만 거둔다. 붙들고 있던 이전 달은 그대로 화면에 남는다.
                send(previous.copy(isTransitioning = false))
                revertSelectionTo(previous.month?.selected)
            }
        }
    }

    /**
     * 전환에 실패해 화면이 이전 달에 머문 뒤, [selectedMonth] 도 그 달로 되돌린다.
     *
     * 되돌리지 않으면 **화면과 선택이 어긋난다** — 화면은 7월인데 [selectedMonth] 만 6월로 앞서 있다.
     * 그 상태에서 사용자가 같은 방향(`<`)을 다시 누르면 [moveTo] 는 **화면의 커서**(7월)를 기준으로
     * 계산해 다시 6월을 쓰는데, [selectedMonth] 가 이미 6월이라 [MutableStateFlow] 가 같은 값을
     * 걸러 **아무 일도 일어나지 않는다.** 재시도 수단이 통째로 죽는다. 되돌려 놓으면 같은 탭이
     * 정상적인 새 조회가 된다.
     *
     * ⚠️ 되돌림이 [selectedMonth] 를 바꾸므로 이전 달을 한 번 더 조회한다. 화면상 변화가 없고
     * (같은 달) 실패 경로에서만 일어나므로 그대로 둔다.
     */
    private fun revertSelectionTo(month: YearMonth?) {
        if (month != null) selectedMonth.value = month
    }

    /**
     * 아직 응답이 없는 구간(로딩·실패)의 상태를 만든다. 본문만 갈아 끼우고 바는 그대로 둔다.
     *
     * 월 전환이 이전 달을 통째로 유지하게 되면서 **첫 로딩 경로에서만 불린다** — 전환 중에는
     * `previous.copy(...)` 가, 응답 뒤에는 완성된 상태가 그 자리를 대신한다. 그래서 실제로는
     * `requested ?: thisMonth` 가 언제나 null 로 풀려 바가 그려지지 않는데(첫 로딩은 둘 다 null 이다),
     * 그 계산을 남겨 두는 이유는 **첫 로딩 여부를 이 함수가 판단하지 않기 위해서**다. 그 판단은
     * [monthFlow] 한 곳에만 둔다.
     */
    private fun stateOf(requested: YearMonth?, content: ArchiveContent): ArchiveUiState =
        ArchiveUiState(
            month = (requested ?: thisMonth)?.let(::cursorFor),
            content = content,
            firstArchivedMonth = firstArchivedMonth,
        )

    private fun cursorFor(month: YearMonth): ArchiveMonthCursor? {
        val latest = thisMonth ?: return null
        return ArchiveMonthCursor(selected = month, latest = latest)
    }

    fun onPreviousMonthClick() = moveTo(ArchiveMonthCursor::previous)

    fun onNextMonthClick() = moveTo(ArchiveMonthCursor::next)

    /**
     * 경계 판단을 여기서 다시 하지 않는다([ArchiveMonthCursor.canGoNext] 를 재확인하지 않는다).
     * 커서가 경계에서 `this` 를 돌려주므로 **같은 객체가 오면 못 움직인 것**이고, 그 한 줄로 충분하다.
     * 경계 규칙의 사본이 두 곳에 생기면 한쪽만 고친 버그가 나온다.
     *
     * 과거 하한이 폐기돼 [ArchiveMonthCursor.previous] 는 이제 **항상 움직인다** — 이 가드는 사실상
     * [ArchiveMonthCursor.next] 전용이 됐다. 그래도 남겨 두는 이유는 위와 같다: 어느 방향이 막히는지를
     * 커서 바깥으로 복사하지 않는다.
     */
    private fun moveTo(move: (ArchiveMonthCursor) -> ArchiveMonthCursor) {
        val cursor = uiState.value.month ?: return
        val moved = move(cursor)
        if (moved != cursor) selectedMonth.value = moved.selected
    }

    /**
     * push 하위 화면이라 재진입이 재시도가 되지 못한다 — 뒤로 가는 순간 실패했다는 사실도 화면과 함께
     * 사라지므로, 실패를 보고 있는 그 자리에서 다시 조회할 수단이 필요하다.
     *
     * 이 버튼이 달린 실패 본문은 **첫 로딩 실패에서만 나오므로**([ArchiveContent.Failure]) 재수집은
     * 사실상 첫 조회를 다시 부른다. 월 전환 실패는 화면이 이전 달에 머무는 데다 [revertSelectionTo]
     * 로 선택도 그 달에 맞춰지므로, 재시도는 화살표를 다시 누르는 것이다.
     */
    fun retry() {
        uiState.restart()
    }

    private companion object {
        // 응답이 이만큼 늦으면 건수 슬롯에 힌트를 띄운다. 그 전에 오는 응답에는 화면에 아무 흔적도
        // 남지 않고 이전 달에서 새 달로 곧바로 갈린다 — 관측된 로컬 응답(3~49ms)이 전부 이쪽이다.
        const val LOADING_HINT_DELAY_MILLIS = 300L

        // 서버가 메시지를 주지 않은 전환 실패의 문구. 첫 로딩 실패 카드(ArchiveFailureScreen)와 같은
        // 말을 쓴다 — 같은 사실을 자리만 바꿔 말하는 것이라 문구까지 달라질 이유가 없다.
        const val TRANSITION_FAILURE_MESSAGE = "보관함을 불러오지 못했어요"
    }
}
