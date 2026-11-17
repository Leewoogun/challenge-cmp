package com.lwg.challenge.feature.friends.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lwg.challenge.domain.model.friend.FriendRecord
import com.lwg.challenge.domain.repository.FriendsRepository
import com.lwg.challenge.domain.usecase.FriendChallengesResult
import com.lwg.challenge.domain.usecase.GetFriendChallengesUseCase
import com.lwg.challenge.feature.friends.record.contract.FriendRecordUiEffect
import com.lwg.challenge.feature.friends.record.contract.FriendRecordUiState
import com.lwg.challenge.utils.datetime.nowKst
import com.lwg.challenge.utils.flow.TriggerStateFlow
import com.lwg.challenge.utils.flow.triggerStateIn
import com.lwg.challenge.utils.flow.valueAs
import com.lwg.challenge.utils.flow.withData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam

/** 친구 전적. 스코어·진행 중과 지난 첫 쪽을 한 단위로 받고, 지난 목록의 누적과 다음 키는 [GetFriendChallengesUseCase] 가 쥔다. */
@KoinViewModel
class FriendRecordViewModel(
    @InjectedParam private val friendId: Long,
    private val friendsRepository: FriendsRepository,
    private val getFriendChallengesUseCase: GetFriendChallengesUseCase,
) : ViewModel() {

    private val _uiEffect = MutableSharedFlow<FriendRecordUiEffect>()
    val uiEffect: SharedFlow<FriendRecordUiEffect> = _uiEffect.asSharedFlow()

    val uiState: TriggerStateFlow<FriendRecordUiState> = recordFlow()
        .triggerStateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(0),
            initialValue = FriendRecordUiState.Loading,
        )

    fun nextPage() {
        getFriendChallengesUseCase.nextPage()
    }

    fun retry() {
        uiState.restart()
    }

    private fun recordFlow(): Flow<FriendRecordUiState> = channelFlow {
        uiState.withData<FriendRecordUiState.Error> { send(FriendRecordUiState.Loading) }

        getFriendChallengesUseCase.refresh()

        val onError: (String) -> Unit = { message ->
            if (uiState.valueAs<FriendRecordUiState.Data>() != null) {
                showMessage(message)
            } else {
                trySend(FriendRecordUiState.Error(message))
            }
        }

        combine(
            friendsRepository.getFriendRecord(friendId = friendId, onError = onError),
            getFriendChallengesUseCase(friendId = friendId, onError = onError),
        ) { record, challenges ->
            toUiState(record, challenges)
        }.collect(::send)
    }

    private fun toUiState(record: FriendRecord, challenges: FriendChallengesResult): FriendRecordUiState =
        if (challenges.challenges.isEmpty() && record.ongoing.isEmpty()) {
            FriendRecordUiState.Empty(friend = record.friend)
        } else {
            FriendRecordUiState.Data(
                record = record,
                pastChallenges = challenges.challenges,
                isPageEnd = challenges.isPageEnd,
                now = nowKst(),
            )
        }

    private fun showMessage(message: String) {
        viewModelScope.launch {
            _uiEffect.emit(FriendRecordUiEffect.ShowMessage(message))
        }
    }
}
