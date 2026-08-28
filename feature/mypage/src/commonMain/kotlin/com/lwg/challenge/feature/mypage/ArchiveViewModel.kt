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

    // 이 채널로 흐르는 것은 부분 실패뿐이다. 부분 실패는 화면의 주 내용이 성공분 목록이라 화면이
    // 실패를 말할 자리가 없어 스낵바를 겹쳐 띄우고, 전체 실패는 실패 영역이 곧 본문 전체라
    // 스낵바 없이 화면이 직접 말한다 (design.md §2.5.4.1).
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
            initialValue = ArchiveUiState(month = null, content = ArchiveContent.Loading),
        )

    /**
     * 한 달치 조회의 전 구간을 **완성된 화면 상태**로 흘린다.
     *
     * 헤더와 본문을 따로 흘리지 않는(`combine` 하지 않는) 이유: 따로 흘리면 화살표를 누른 순간
     * 헤더만 새 달로 바뀌고 본문은 아직 옛 달 카드인 **찢어진 중간 상태**가 한 프레임 보인다.
     * 이 flow 가 `(새 달, Blank)` 를 **정지 없이 먼저** 보내므로 짝이 원자적으로만 바뀐다 —
     * 타이밍 방어가 아니라 구조적으로 불가능해진다.
     *
     * `onStart`/`onEmpty` 대신 [channelFlow] 안에서 직접 보내는 이유: 지연 스피너가 로딩과
     * **경쟁**해야 하는데(응답이 300ms 안에 오면 스피너를 아예 띄우지 않는다) `onStart` 에서
     * `delay` 하면 **요청 자체가 300ms 늦어진다.** 별도 코루틴으로 띄우고 응답이 오면 죽인다.
     */
    private fun monthFlow(requested: YearMonth?): Flow<ArchiveUiState> = channelFlow {
        val isFirstLoad = thisMonth == null
        send(stateOf(requested, if (isFirstLoad) ArchiveContent.Loading else ArchiveContent.Blank))

        val spinner = if (isFirstLoad) {
            null
        } else {
            launch {
                delay(SPINNER_DELAY_MILLIS)
                send(stateOf(requested, ArchiveContent.Loading))
            }
        }

        // onError 하나가 전체 실패와 부분 실패 둘 다를 나른다. 둘을 가르는 것은 emit 이 뒤따르는지다 —
        // 뒤따르면 부분 실패라 메시지를 스낵바로 흘리고, 안 따르면 전체 실패라 보류한 채 버려진다.
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

                // cancel() 이 아니라 cancelAndJoin() 인 이유: 스피너가 이미 send 에 진입했다면
                // cancel 만으로는 그 send 가 Data 뒤에 도착하는 역전을 막지 못한다.
                spinner?.cancelAndJoin()

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
                    ),
                )
            }

        spinner?.cancelAndJoin()
        // 실패를 값으로 만들지 않으면 상류가 아무것도 보내지 않은 채 끝나 본문이 로딩에 갇힌다.
        if (!delivered) send(stateOf(requested, ArchiveContent.Failure))
    }

    /**
     * 아직 응답이 없는 구간(로딩·비움·실패)의 상태. 본문만 갈아 끼우고 바는 그대로 둔다.
     *
     * `requested ?: thisMonth` 인 이유: `requested == null` 은 *"이번 달"* 이라는 뜻이지
     * *"달을 모른다"* 가 아니다. 첫 응답 뒤에는 그 달이 [thisMonth] 로 확정돼 있으므로, 재시도
     * (사용자가 화살표를 한 번도 안 누른 상태) 중에도 **월 이동 바가 사라지지 않는다.**
     * 진짜로 모르는 것은 둘 다 null 인 첫 로딩뿐이고, 그때만 바가 없다.
     */
    private fun stateOf(requested: YearMonth?, content: ArchiveContent): ArchiveUiState =
        ArchiveUiState(month = (requested ?: thisMonth)?.let(::cursorFor), content = content)

    private fun cursorFor(month: YearMonth): ArchiveMonthCursor? {
        val latest = thisMonth ?: return null
        return ArchiveMonthCursor(selected = month, latest = latest, earliest = firstArchivedMonth)
    }

    fun onPreviousMonthClick() = moveTo(ArchiveMonthCursor::previous)

    fun onNextMonthClick() = moveTo(ArchiveMonthCursor::next)

    /**
     * 경계 판단을 여기서 다시 하지 않는다([ArchiveMonthCursor.canGoPrevious] 를 재확인하지 않는다).
     * 커서가 경계에서 `this` 를 돌려주므로 **같은 객체가 오면 못 움직인 것**이고, 그 한 줄로 충분하다.
     * 경계 규칙의 사본이 두 곳에 생기면 한쪽만 고친 버그가 나온다.
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
     * 재수집은 [selectedMonth] 의 현재 값으로 다시 시작되므로 **보고 있던 달**을 다시 부른다.
     * 5월을 보다 실패했으면 이번 달이 아니라 5월이다.
     */
    fun retry() {
        uiState.restart()
    }

    private companion object {
        // 이 값보다 빨리 오는 응답에는 스피너를 아예 띄우지 않는다.
        const val SPINNER_DELAY_MILLIS = 300L
    }
}
