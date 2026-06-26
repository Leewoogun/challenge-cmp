package com.lwg.challenge.feature.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lwg.challenge.domain.model.friend.Relation
import com.lwg.challenge.domain.model.friend.UserSearchResult
import com.lwg.challenge.domain.repository.FriendsRepository
import com.lwg.challenge.feature.friends.contract.FriendSearchItemState
import com.lwg.challenge.feature.friends.contract.FriendsSearchUiState
import com.lwg.challenge.feature.friends.contract.FriendsUiEffect
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

/**
 * 친구 검색 화면 ViewModel.
 *
 * - `query` StateFlow → debounce 300ms → trim → min 2자 가드 → `searchUsers` flatMapLatest.
 * - 결과는 [_searchResults] StateFlow 로 저장 (낙관적 갱신 가능하도록).
 * - [uiState] 는 검색 단계([searchPhase]) + [_searchResults] 를 combine 해서 4-state 로 매핑.
 *
 * spec: docs/features/friends/spec-friend-add.md §6.5
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@KoinViewModel
class FriendsSearchViewModel(
    private val friendsRepository: FriendsRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    /** 검색 결과 (raw) — 낙관적 갱신은 이 StateFlow 를 직접 업데이트. */
    private val _searchResults = MutableStateFlow<List<UserSearchResult>>(emptyList())

    /** 검색 단계: query 입력 길이 / debounce 후 호출 상태. */
    private val searchPhase: StateFlow<SearchPhase> =
        _query
            .debounce(SEARCH_DEBOUNCE_MS)
            .map { it.trim() }
            .distinctUntilChanged()
            .flatMapLatest { q ->
                if (q.length < MIN_QUERY_LENGTH) {
                    _searchResults.update { emptyList() }
                    flowOf(SearchPhase.Idle)
                } else {
                    flow {
                        emit(SearchPhase.Searching)
                        friendsRepository
                            .searchUsers(q, onError = ::showMessage)
                            .collect { list ->
                                _searchResults.update { list }
                                emit(SearchPhase.Done)
                            }
                    }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = SearchPhase.Idle,
            )

    val uiState: StateFlow<FriendsSearchUiState> =
        combine(searchPhase, _searchResults) { phase, results ->
            when (phase) {
                SearchPhase.Idle -> FriendsSearchUiState.Idle
                SearchPhase.Searching -> FriendsSearchUiState.Searching
                SearchPhase.Done -> if (results.isEmpty()) {
                    FriendsSearchUiState.Empty
                } else {
                    FriendsSearchUiState.Result(results.map(::FriendSearchItemState))
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = FriendsSearchUiState.Idle,
        )

    private val _uiEffect = MutableSharedFlow<FriendsUiEffect>()
    val uiEffect: SharedFlow<FriendsUiEffect> = _uiEffect

    /** 입력 변경 — debounce 가 자동 처리. */
    fun search(text: String) {
        _query.update { text }
    }

    /** Relation.NONE / REJECTED 클릭 → 친구 요청 보내기. */
    fun sendRequest(item: FriendSearchItemState) {
        mutateRelation(
            item = item,
            optimisticRelation = Relation.REQUEST_SENT,
            optimisticPendingRequestId = null,
        ) { onSuccess, onError ->
            friendsRepository.sendRequest(item.userId, onSuccess, onError)
        }
    }

    /** Relation.REQUEST_RECEIVED 클릭 → 수락. */
    fun acceptRequest(item: FriendSearchItemState) {
        val requestId = item.pendingRequestId ?: return
        mutateRelation(
            item = item,
            optimisticRelation = Relation.FRIEND,
            optimisticPendingRequestId = null,
        ) { onSuccess, onError ->
            friendsRepository.acceptRequest(requestId, onSuccess, onError)
        }
    }

    /** Relation.REQUEST_SENT 의 "취소" 버튼 클릭. */
    fun cancelRequest(item: FriendSearchItemState) {
        val requestId = item.pendingRequestId ?: return
        mutateRelation(
            item = item,
            optimisticRelation = Relation.NONE,
            optimisticPendingRequestId = null,
        ) { onSuccess, onError ->
            friendsRepository.cancelRequest(requestId, onSuccess, onError)
        }
    }

    /**
     * 단일 row 의 relation 을 낙관적으로 변경하고, 실패 시 **1행 단위 역연산** 으로 롤백한다.
     *
     * 전체 리스트 스냅샷을 통째 복원하면 동시 다른 row 의 mutation 까지 덮어쓰는
     * race 가 발생하므로, 반드시 대상 row 의 이전 relation/pendingRequestId 만
     * 되돌린다.
     */
    private fun mutateRelation(
        item: FriendSearchItemState,
        optimisticRelation: Relation,
        optimisticPendingRequestId: Long?,
        action: suspend (onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
    ) {
        val before = _searchResults.value.firstOrNull { it.userId == item.userId } ?: return

        _searchResults.update { list ->
            list.map {
                if (it.userId == before.userId) {
                    it.copy(
                        relation = optimisticRelation,
                        pendingRequestId = optimisticPendingRequestId,
                    )
                } else {
                    it
                }
            }
        }

        viewModelScope.launch {
            action(
                {},
                { message ->
                    _searchResults.update { list ->
                        list.map {
                            if (it.userId == before.userId) {
                                it.copy(
                                    relation = before.relation,
                                    pendingRequestId = before.pendingRequestId,
                                )
                            } else {
                                it
                            }
                        }
                    }
                    showMessage(message)
                },
            )
        }
    }

    private fun showMessage(message: String) {
        viewModelScope.launch {
            _uiEffect.emit(FriendsUiEffect.ShowMessage(message))
        }
    }

    private enum class SearchPhase { Idle, Searching, Done }

    companion object {
        internal const val SEARCH_DEBOUNCE_MS: Long = 300L
        internal const val MIN_QUERY_LENGTH: Int = 2
    }
}
