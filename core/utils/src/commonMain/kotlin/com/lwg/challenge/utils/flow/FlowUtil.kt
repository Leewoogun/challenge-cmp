package com.lwg.challenge.utils.flow

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

inline fun <reified T> StateFlow<*>.withData(action: (T) -> Unit) {
    val uiState = value
    if (uiState is T) {
        action(uiState)
    }
}

inline fun <State, reified T : State> MutableStateFlow<State>.updateWithData(action: (T) -> T) {
    update { state ->
        if (state is T) {
            action(state)
        } else {
            state
        }
    }
}

inline fun <T : Any> StateFlow<T?>.withValue(action: (T) -> Unit) {
    value?.let(action)
}
