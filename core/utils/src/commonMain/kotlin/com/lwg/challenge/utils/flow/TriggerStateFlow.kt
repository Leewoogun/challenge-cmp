package com.lwg.challenge.utils.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingCommand
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn

/** [restart] 로 상류를 처음부터 다시 수집시킬 수 있는 [StateFlow]. */
interface TriggerStateFlow<out T> : StateFlow<T> {

    /** 상류 파이프라인을 재수집한다. 재수집이 끝날 때까지 [value] 는 직전 값을 유지한다. */
    fun restart()
}

/**
 * [stateIn] 과 동일하되 [TriggerStateFlow.restart] 로 상류 재수집을 강제할 수 있다.
 *
 * 재구독으로 갱신하는 방식이라, 상류가 재구독만으로는 초기화되지 않는 자체 상태(누적 페이지 캐시,
 * replay 가 붙은 SharedFlow 등)를 들고 있으면 의도한 갱신이 일어나지 않는다. 그런 경우엔 그 상태를
 * 소유한 쪽이 갱신 API 를 직접 노출해야 한다.
 *
 * 갱신에 인자가 필요하면([restart] 는 인자가 없다) 상류를 `MutableStateFlow<Query> + flatMapLatest`
 * 로 구성할 것. 두 방식은 함께 써도 된다.
 */
fun <T> Flow<T>.triggerStateIn(
    scope: CoroutineScope,
    started: SharingStarted,
    initialValue: T,
): TriggerStateFlow<T> {
    val restartable = RestartableSharingStarted(started)
    val stateFlow = stateIn(scope, restartable, initialValue)
    return object : TriggerStateFlow<T>, StateFlow<T> by stateFlow {
        override fun restart() = restartable.restart()
    }
}

/**
 * 구독자 수로만 결정되던 [SharingCommand] 스트림에 수동 명령 채널을 [merge] 로 끼워 넣는다.
 * STOP → START 를 연달아 보내면 `stateIn` 이 "전원 구독 해제 후 재구독" 으로 받아 상류를 다시 수집한다.
 *
 * STOP_AND_RESET_REPLAY_CACHE 가 아닌 STOP 이라 재수집 동안 마지막 값이 살아있다(initialValue 로 깜빡이지 않음).
 * 구독자가 0일 때 호출하면 구독자 없이도 상류가 시작되므로, 화면이 떠 있을 때만 호출할 것.
 */
private class RestartableSharingStarted(
    private val delegate: SharingStarted,
) : SharingStarted {

    // STOP + START 를 한 번에 담아야 하므로 2.
    private val restartCommands = MutableSharedFlow<SharingCommand>(extraBufferCapacity = 2)

    override fun command(subscriptionCount: StateFlow<Int>): Flow<SharingCommand> =
        merge(restartCommands, delegate.command(subscriptionCount))

    fun restart() {
        restartCommands.tryEmit(SharingCommand.STOP)
        restartCommands.tryEmit(SharingCommand.START)
    }
}
