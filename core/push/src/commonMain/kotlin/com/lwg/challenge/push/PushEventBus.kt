package com.lwg.challenge.push

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** 알림 탭으로 들어온 딥링크를 인증이 끝날 때까지 보관한다. 구독 전에 emit 돼도 남도록 StateFlow 다. */
class PushEventBus {

    private val _pending = MutableStateFlow<PushEvent?>(null)
    val pending: StateFlow<PushEvent?> = _pending.asStateFlow()

    fun emit(event: PushEvent) = _pending.update { event }

    fun consume() = _pending.update { null }
}
