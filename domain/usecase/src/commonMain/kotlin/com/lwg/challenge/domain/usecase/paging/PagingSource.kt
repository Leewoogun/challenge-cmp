package com.lwg.challenge.domain.usecase.paging

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet

/**
 * 키 기반 페이지 누적. 다음 요청 키와 끝 여부는 서버 응답에서 꺼내고, 앱이 항목으로 키를 계산하지 않는다.
 *
 * 상태는 성공 응답이 왔을 때만 바뀐다 — 실패한 [nextPage] 는 같은 키로 다시 부를 수 있고, 실패한 [refresh] 는
 * 쌓인 쪽과 다음 키를 지우지 않는다. 보이는 쪽만 다시 받는 갱신은 두지 않는다(쓰는 화면이 진입마다 첫 쪽부터 다시 받는다).
 *
 * @param K 페이지 키. 첫 쪽은 `null` 로 요청한다
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PagingSource<K : Any, T> : Pageable {

    // 값이 페이지 키다. null 은 첫 쪽.
    private val requests = MutableSharedFlow<K?>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private val state = MutableStateFlow(PagingState<K, T>())

    // 응답을 기다리는 다음 쪽 키. 같은 키를 또 보내면 flatMapLatest 가 진행 중인 요청을 끊고 다시 보낸다.
    private var pendingNextKey: K? = null
    private var lastKey: Any? = UNINITIALIZED

    fun <R> flow(
        key: Any,
        fetch: (pageKey: K?) -> Flow<R>,
        getItems: (R) -> List<T>,
        getNextKey: (R) -> K?,
        getIsPageEnd: (R) -> Boolean,
        keySelector: ((T) -> Any)? = null,
    ): Flow<PagingResult<R, T>> {
        if (key != lastKey) {
            lastKey = key
            resetInternal()
        }

        return requests
            .flatMapLatest { pageKey ->
                fetch(pageKey).map { response ->
                    val updated = state.updateAndGet { current ->
                        current.withPage(
                            pageKey = pageKey,
                            items = getItems(response),
                            nextKey = getNextKey(response),
                            isPageEnd = getIsPageEnd(response),
                        )
                    }
                    PagingResult(
                        items = updated.flattenPages(keySelector),
                        response = response,
                        isPageEnd = updated.isPageEnd,
                    )
                }.onCompletion {
                    // 실패해도 풀어야 같은 키로 재시도할 수 있다.
                    if (pendingNextKey == pageKey) pendingNextKey = null
                }
            }
    }

    override fun nextPage() {
        val current = state.value
        if (!current.hasResponse || current.isPageEnd) return
        val nextKey = current.nextKey ?: return
        if (nextKey == pendingNextKey) return
        pendingNextKey = nextKey
        requests.tryEmit(nextKey)
    }

    override fun refresh() {
        requests.tryEmit(null)
    }

    private fun resetInternal() {
        state.update { PagingState() }
        requests.tryEmit(null)
    }

    private companion object {
        private object UNINITIALIZED
    }
}

data class PagingResult<R, T>(
    val items: List<T>,
    val response: R,
    val isPageEnd: Boolean,
)

private data class PagingState<K : Any, T>(
    val pages: Map<K?, List<T>> = emptyMap(),
    val nextKey: K? = null,
    val isPageEnd: Boolean = false,
) {

    val hasResponse: Boolean get() = pages.isNotEmpty()

    // 첫 쪽은 누적을 교체하고, 그 외 키는 제자리에 덮어써 같은 키 재전달이 멱등이다.
    fun withPage(pageKey: K?, items: List<T>, nextKey: K?, isPageEnd: Boolean): PagingState<K, T> = PagingState(
        pages = if (pageKey == null) mapOf<K?, List<T>>(null to items) else pages + (pageKey to items),
        nextKey = nextKey,
        isPageEnd = isPageEnd,
    )

    fun flattenPages(keySelector: ((T) -> Any)?): List<T> {
        val flattened = pages.values.flatten()
        return if (keySelector != null) flattened.distinctBy(keySelector) else flattened
    }
}
