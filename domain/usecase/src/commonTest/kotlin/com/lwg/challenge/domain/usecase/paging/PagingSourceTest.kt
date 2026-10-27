package com.lwg.challenge.domain.usecase.paging

import app.cash.turbine.test
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * 모든 픽스처는 `마지막 항목 id ≠ 다음 키 ≠ 마지막 항목 id - 1` 이다(예: 항목 [9, 8] → 다음 키 5).
 * 앱이 항목으로 키를 만드는 두 가지 잘못된 구현(마지막 id, 마지막 id - 1)이 모두 깨지게 하려는 것이다.
 */
class PagingSourceTest {

    private data class Item(val id: Long, val label: String = "")

    private data class Page(val items: List<Item>, val nextId: Long?, val isPageEnd: Boolean)

    private val pagingSource = PagingSource<Long, Item>()

    // null 이 실패다. 실제 Repository 처럼 실패는 emit 없이 끝난다.
    private val outcomes = ArrayDeque<Page?>()
    private val requestedKeys = mutableListOf<Long?>()

    private fun enqueuePage(ids: List<Long>, nextId: Long?, isPageEnd: Boolean = nextId == null) {
        outcomes += Page(ids.map { Item(it) }, nextId, isPageEnd)
    }

    private fun enqueueFailure() {
        outcomes += null
    }

    private fun fetch(pageKey: Long?): Flow<Page> = flow {
        requestedKeys += pageKey
        outcomes.removeFirst()?.let { emit(it) }
    }

    private fun pagingFlow(key: Any = Unit, keySelector: ((Item) -> Any)? = null) = pagingSource.flow(
        key = key,
        fetch = ::fetch,
        getItems = { it.items },
        getNextKey = { it.nextId },
        getIsPageEnd = { it.isPageEnd },
        keySelector = keySelector,
    )

    private fun PagingResult<Page, Item>.ids(): List<Long> = items.map { it.id }

    @Test
    fun `다음 쪽 요청 키는 마지막 항목 id 가 아니라 직전 응답이 준 다음 키다`() = runTest {
        enqueuePage(listOf(9L, 8L), nextId = 5L)
        enqueuePage(listOf(5L, 4L), nextId = null)

        pagingFlow().test {
            assertEquals(listOf(9L, 8L), awaitItem().ids())

            pagingSource.nextPage()

            val second = awaitItem()
            assertEquals(listOf(null, 5L), requestedKeys)
            assertEquals(listOf(9L, 8L, 5L, 4L), second.ids())
            assertTrue(second.isPageEnd)
        }
    }

    /** 원본의 `newItems.isEmpty()` 추정과 구별된다 — 항목이 꽉 찬 쪽이어도 서버가 끝이라면 끝이다. */
    @Test
    fun `isPageEnd 면 항목이 있는 쪽이어도 nextPage 가 요청을 내지 않는다`() = runTest {
        enqueuePage(listOf(9L, 8L), nextId = null)

        pagingFlow().test {
            assertTrue(awaitItem().isPageEnd)

            pagingSource.nextPage()

            expectNoEvents()
            assertEquals(listOf<Long?>(null), requestedKeys)
        }
    }

    /** 멈춤 판단은 다음 키의 유무가 아니라 isPageEnd 다. */
    @Test
    fun `isPageEnd 면 다음 키가 실려 있어도 nextPage 가 요청을 내지 않는다`() = runTest {
        enqueuePage(listOf(9L, 8L), nextId = 5L, isPageEnd = true)

        pagingFlow().test {
            awaitItem()

            pagingSource.nextPage()

            expectNoEvents()
            assertEquals(listOf<Long?>(null), requestedKeys)
        }
    }

    @Test
    fun `첫 응답이 오기 전에는 nextPage 가 요청을 내지 않는다`() = runTest {
        enqueueFailure()

        pagingFlow().test {
            pagingSource.nextPage()

            expectNoEvents()
            assertEquals(listOf<Long?>(null), requestedKeys)
        }
    }

    @Test
    fun `refresh 하면 키 null 로 첫 쪽을 다시 요청하고 누적이 그 쪽으로 교체된다`() = runTest {
        enqueuePage(listOf(9L, 8L), nextId = 5L)
        enqueuePage(listOf(5L, 4L), nextId = 2L)
        enqueuePage(listOf(12L, 11L), nextId = 9L)
        enqueuePage(listOf(9L, 8L), nextId = null)

        pagingFlow().test {
            awaitItem()
            pagingSource.nextPage()
            awaitItem()

            pagingSource.refresh()

            assertEquals(listOf(12L, 11L), awaitItem().ids())

            // 다음 키도 새 첫 쪽의 값으로 바뀌었다.
            pagingSource.nextPage()

            assertEquals(listOf(12L, 11L, 9L, 8L), awaitItem().ids())
            assertEquals(listOf(null, 5L, null, 9L), requestedKeys)
        }
    }

    @Test
    fun `nextPage 가 실패한 뒤 다시 부르면 같은 키로 재요청한다`() = runTest {
        enqueuePage(listOf(9L, 8L), nextId = 5L)
        enqueueFailure()
        enqueuePage(listOf(5L, 4L), nextId = null)

        pagingFlow().test {
            awaitItem()

            pagingSource.nextPage()
            expectNoEvents()

            pagingSource.nextPage()

            assertEquals(listOf(9L, 8L, 5L, 4L), awaitItem().ids())
            assertEquals(listOf(null, 5L, 5L), requestedKeys)
        }
    }

    // 끝 근처에서 스크롤을 연달아 멈추면 같은 키가 또 온다. 그대로 보내면 진행 중 요청을 끊고 같은 요청을 다시 낸다.
    @Test
    fun `응답을 기다리는 동안 nextPage 를 다시 불러도 같은 키를 다시 요청하지 않는다`() = runTest {
        enqueuePage(listOf(9L, 8L), nextId = 5L)
        enqueuePage(listOf(5L, 4L), nextId = null)
        val pending = CompletableDeferred<Unit>()
        // 요청이 나간 순간을 기록하고 응답만 붙잡아 둬야, 끊기고 다시 나간 요청도 기록에 남는다.
        val gatedFetch: (Long?) -> Flow<Page> = { pageKey ->
            flow {
                requestedKeys += pageKey
                if (pageKey != null) pending.await()
                outcomes.removeFirst()?.let { emit(it) }
            }
        }

        pagingSource.flow(
            key = Unit,
            fetch = gatedFetch,
            getItems = { it.items },
            getNextKey = { it.nextId },
            getIsPageEnd = { it.isPageEnd },
        ).test {
            awaitItem()

            pagingSource.nextPage()
            pagingSource.nextPage()
            pending.complete(Unit)

            assertEquals(listOf(9L, 8L, 5L, 4L), awaitItem().ids())
            assertEquals(listOf(null, 5L), requestedKeys)
        }
    }

    @Test
    fun `refresh 가 실패해도 쌓인 쪽과 다음 키가 유지된다`() = runTest {
        enqueuePage(listOf(9L, 8L), nextId = 5L)
        enqueuePage(listOf(5L, 4L), nextId = 2L)
        enqueueFailure()
        enqueuePage(listOf(2L), nextId = null)

        pagingFlow().test {
            awaitItem()
            pagingSource.nextPage()
            awaitItem()

            pagingSource.refresh()
            expectNoEvents()

            pagingSource.nextPage()

            val result = awaitItem()
            assertEquals(listOf(9L, 8L, 5L, 4L, 2L), result.ids())
            assertEquals(listOf(null, 5L, null, 2L), requestedKeys)
            assertTrue(result.isPageEnd)
        }
    }

    @Test
    fun `key 가 바뀌면 쌓인 쪽을 버리고 첫 쪽부터 다시 받는다`() = runTest {
        enqueuePage(listOf(9L, 8L), nextId = 5L)
        enqueuePage(listOf(5L, 4L), nextId = 2L)
        enqueuePage(listOf(30L, 29L), nextId = 26L)

        pagingFlow(key = "전체").test {
            awaitItem()
            pagingSource.nextPage()
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        pagingFlow(key = "안 읽음").test {
            val result = awaitItem()
            assertEquals(listOf(30L, 29L), result.ids())
            assertFalse(result.isPageEnd)
            assertEquals(listOf(null, 5L, null), requestedKeys)
        }
    }

    /** 응답 사이에 새 항목이 끼면 같은 항목이 두 쪽에 걸쳐 올 수 있다. 먼저 받은 쪽을 남긴다. */
    @Test
    fun `keySelector 가 있으면 쪽 사이에 겹친 항목을 한 번만 담는다`() = runTest {
        enqueuePage(listOf(9L, 8L), nextId = 5L)
        outcomes += Page(listOf(Item(8L, label = "중복"), Item(5L)), nextId = null, isPageEnd = true)

        pagingFlow(keySelector = { it.id }).test {
            awaitItem()
            pagingSource.nextPage()

            val items = awaitItem().items
            assertEquals(listOf(9L, 8L, 5L), items.map { it.id })
            assertEquals("", items[1].label)
        }
    }
}
