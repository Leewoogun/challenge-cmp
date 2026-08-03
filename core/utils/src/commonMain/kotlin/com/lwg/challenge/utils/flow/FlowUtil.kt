package com.lwg.challenge.utils.flow

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * 현재 값이 [T] 일 때만 [action] 을 실행한다. 아니면 아무것도 하지 않는다.
 *
 * inline 이라 [action] 안에서 `return@withData` 로 가드를 걸 수 있다.
 * ```
 * fun submit() {
 *     uiState.withData<UiState.Data> { data ->
 *         if (!data.canSubmit) return@withData
 *         ...
 *     }
 * }
 * ```
 */
inline fun <reified T> StateFlow<*>.withData(action: (T) -> Unit) {
    val uiState = value
    if (uiState is T) {
        action(uiState)
    }
}

/**
 * 현재 값이 [T] 일 때만 [action] 으로 변형해 갱신한다. 아니면 값을 그대로 둔다.
 *
 * [State] 는 StateFlow 가 들고 있는 부모 타입, [T] 는 갱신 대상 하위 타입이다.
 * [T] 는 [action] 만으로 추론되지 않으므로 호출부에서 두 타입을 모두 적어야 한다.
 * ```
 * _uiState.updateWithData<UiState, UiState.Data> { it.copy(message = "hello") }
 * ```
 */
inline fun <State, reified T : State> MutableStateFlow<State>.updateWithData(action: (T) -> T) {
    update { state ->
        if (state is T) {
            action(state)
        } else {
            state
        }
    }
}

/** 현재 값이 null 이 아닐 때만 [action] 을 실행한다. */
inline fun <T : Any> StateFlow<T?>.withValue(action: (T) -> Unit) {
    value?.let(action)
}
