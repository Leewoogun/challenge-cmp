package com.lwg.challenge.feature.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lwg.challenge.domain.usecase.GetChallengeHistoriesUseCase
import com.lwg.challenge.feature.mypage.contract.ArchiveUiEffect
import com.lwg.challenge.feature.mypage.contract.ArchiveUiState
import com.lwg.challenge.feature.mypage.contract.toArchiveMonths
import com.lwg.challenge.utils.flow.TriggerStateFlow
import com.lwg.challenge.utils.flow.triggerStateIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class ArchiveViewModel(
    getChallengeHistoriesUseCase: GetChallengeHistoriesUseCase,
) : ViewModel() {

    // 이 채널로 흐르는 것은 부분 실패뿐이다. 부분 실패는 화면의 주 내용이 성공분 목록이라 화면이
    // 실패를 말할 자리가 없어 스낵바를 겹쳐 띄우고, 전체 실패는 실패 영역이 곧 화면 전체라
    // 스낵바 없이 화면이 직접 말한다 (design.md §2.5.4.1).
    private val _uiEffect = MutableSharedFlow<ArchiveUiEffect>()
    val uiEffect: SharedFlow<ArchiveUiEffect> = _uiEffect.asSharedFlow()

    // 월 그룹핑을 여기서 확정한다. UiState 의 get() 으로 미루면 매 호출마다 새 List 가 나와
    // 하위 Composable 의 skipping 이 깨진다 (CLAUDE.md "UI State 파싱 규칙").
    //
    // onError 를 그대로 실패 신호로 쓰지 않는 이유: 이 콜백 하나가 전체 실패와 부분 실패(항목 일부만
    // 매핑에 실패하고 나머지는 그대로 온다) 둘 다를 나른다. 둘을 가르는 것은 emit 이 뒤따르는지다 —
    // 뒤따르면 부분 실패라 메시지를 스낵바로 흘리고, 안 따르면 전체 실패라 보류한 채 [onEmpty] 의
    // Failure 와 함께 버려진다.
    //
    // 보류 변수를 flow { } 안에 두는 이유: 수집마다 새로 만들어져야 [retry] 의 재수집에서
    // 직전 수집이 남긴 메시지가 엉뚱한 스낵바로 새어 나오지 않는다.
    private val historiesFlow: Flow<ArchiveUiState> = flow {
        var pendingFailureMessage: String? = null
        getChallengeHistoriesUseCase(onError = { pendingFailureMessage = it })
            .collect { histories ->
                pendingFailureMessage?.let { message ->
                    pendingFailureMessage = null
                    showMessage(message)
                }
                emit(ArchiveUiState.Data(histories.toArchiveMonths()))
            }
    }

    /**
     * 실패를 값으로 만들지 않으면 상류가 아무것도 emit 하지 않은 채 끝나 화면이 `Loading` 에 갇힌다
     * (첫 진입엔 유지할 직전 값도 없어 무한 스피너가 된다).
     *
     * `onStart` 가 `onEmpty` 바깥이라야 한다 — 안쪽에 두면 `Loading` emit 이 "비어있지 않음" 으로
     * 세어져 `Failure` 가 영원히 나오지 않는다. 바깥이라 [retry] 의 재수집마다 `Loading` 부터 다시 간다.
     *
     * WhileSubscribed(0) 이라 상세를 보고 돌아오면 재구독으로 목록이 다시 조회된다 —
     * 보관함에는 갱신을 알리는 콜백이 없어 그게 유일한 갱신 경로다.
     */
    val uiState: TriggerStateFlow<ArchiveUiState> =
        historiesFlow
            .onEmpty { emit(ArchiveUiState.Failure) }
            .onStart { emit(ArchiveUiState.Loading) }
            .triggerStateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(0),
                initialValue = ArchiveUiState.Loading,
            )

    /**
     * push 하위 화면이라 재진입이 재시도가 되지 못한다 — 뒤로 가는 순간 실패했다는 사실도 화면과 함께
     * 사라지므로, 실패를 보고 있는 그 자리에서 다시 조회할 수단이 필요하다.
     */
    fun retry() {
        uiState.restart()
    }

    // 별도 코루틴으로 내보낸다 — 여기서 직접 suspend 하면 구독자가 붙기 전에는 목록 파이프라인까지
    // 함께 멈춰, 스낵바를 못 띄우는 것을 넘어 화면이 Loading 에 갇힌다.
    private fun showMessage(message: String) {
        viewModelScope.launch {
            _uiEffect.emit(ArchiveUiEffect.ShowMessage(message))
        }
    }
}
