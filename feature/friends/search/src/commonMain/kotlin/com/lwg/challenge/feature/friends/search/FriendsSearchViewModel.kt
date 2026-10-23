package com.lwg.challenge.feature.friends.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lwg.challenge.domain.model.friend.Relation
import com.lwg.challenge.domain.model.friend.UserSearchResult
import com.lwg.challenge.domain.repository.FriendsRepository
import com.lwg.challenge.feature.friends.search.contract.FriendsSearchUiEffect
import com.lwg.challenge.feature.friends.search.contract.FriendsSearchUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@KoinViewModel
class FriendsSearchViewModel(
    private val friendsRepository: FriendsRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    // 요청 성공 시 그 행만 바꾸려고 검색 결과를 파이프라인 밖에 둔다.
    private val _searchResults = MutableStateFlow<List<UserSearchResult>>(emptyList())

    private val _requestingUserIds = MutableStateFlow<Set<Long>>(emptySet())

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
        combine(searchPhase, _searchResults, _requestingUserIds) { phase, results, requestingUserIds ->
            when (phase) {
                SearchPhase.Idle -> FriendsSearchUiState.Idle
                SearchPhase.Searching -> FriendsSearchUiState.Searching
                SearchPhase.Done -> if (results.isEmpty()) {
                    FriendsSearchUiState.Empty
                } else {
                    FriendsSearchUiState.Result(items = results, requestingUserIds = requestingUserIds)
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = FriendsSearchUiState.Idle,
        )

    private val _uiEffect = MutableSharedFlow<FriendsSearchUiEffect>()
    val uiEffect: SharedFlow<FriendsSearchUiEffect> = _uiEffect.asSharedFlow()

    fun search(text: String) {
        _query.update { text }
    }

    fun clickAction(item: UserSearchResult) {
        when (item.relation) {
            Relation.NONE, Relation.REJECTED -> sendRequest(item)
            Relation.REQUEST_RECEIVED -> acceptRequest(item)
            Relation.REQUEST_SENT, Relation.FRIEND -> Unit
        }
    }

    fun sendRequest(item: UserSearchResult) {
        mutateRow(item) {
            friendsRepository.sendRequest(
                receiverId = item.userId,
                onSuccess = { requestId ->
                    updateRow(item.userId) { it.copy(relation = Relation.REQUEST_SENT, pendingRequestId = requestId) }
                },
                onError = ::showMessage,
            )
        }
    }

    fun acceptRequest(item: UserSearchResult) {
        val requestId = item.pendingRequestId ?: return
        mutateRow(item) {
            friendsRepository.acceptRequest(
                requestId = requestId,
                onSuccess = { updateRow(item.userId) { it.copy(relation = Relation.FRIEND, pendingRequestId = null) } },
                onError = ::showMessage,
            )
        }
    }

    fun cancelRequest(item: UserSearchResult) {
        val requestId = item.pendingRequestId ?: return
        mutateRow(item) {
            friendsRepository.cancelRequest(
                requestId = requestId,
                onSuccess = { updateRow(item.userId) { it.copy(relation = Relation.NONE, pendingRequestId = null) } },
                onError = ::showMessage,
            )
        }
    }

    /** 응답이 올 때까지 그 행만 잠근다. 같은 행의 중복 요청도 여기서 막힌다. */
    private fun mutateRow(item: UserSearchResult, request: suspend () -> Unit) {
        if (item.userId in _requestingUserIds.value) return
        _requestingUserIds.update { it + item.userId }

        viewModelScope.launch {
            request()
            _requestingUserIds.update { it - item.userId }
        }
    }

    private fun updateRow(userId: Long, transform: (UserSearchResult) -> UserSearchResult) {
        _searchResults.update { list -> list.map { if (it.userId == userId) transform(it) else it } }
    }

    private fun showMessage(message: String) {
        viewModelScope.launch {
            _uiEffect.emit(FriendsSearchUiEffect.ShowMessage(message))
        }
    }

    private enum class SearchPhase { Idle, Searching, Done }

    companion object {
        internal const val SEARCH_DEBOUNCE_MS: Long = 300L
        internal const val MIN_QUERY_LENGTH: Int = 2
    }
}
