package com.lwg.challenge.feature.mypage.mypage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lwg.challenge.designsystem.components.ChallengeScaffold
import com.lwg.challenge.designsystem.components.ChallengeTopBar
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.feature.mypage.mypage.contract.MyPageModalEffect
import com.lwg.challenge.feature.mypage.mypage.contract.MyPageUiEffect
import com.lwg.challenge.feature.mypage.mypage.contract.MyPageUiState
import com.lwg.challenge.navigation.LocalMainAction
import com.lwg.challenge.navigation.LocalNavigateAction
import com.lwg.challenge.navigation.Route
import com.lwg.challenge.ui.components.ConfirmDialog
import org.koin.compose.viewmodel.koinViewModel

private const val WITHDRAW_TITLE = "정말 탈퇴하시겠어요?"

// 두 번째 문단이 빠지면 사용자가 "내 기록이 전부 지워진다" 로 오해한 채 탈퇴한다 —
// 동의한 내용과 실제가 달라진다 (§1.6).
private const val WITHDRAW_MESSAGE = "계정 정보와 인증 사진이 삭제되고 되돌릴 수 없어요.\n\n" +
    "지난 챌린지의 계약서와 결과는 상대방의 기록이라 남아요 — 상대 화면에서 나는 '탈퇴한 사용자' 로 보여요."

@Composable
fun MyPageRoute(
    viewModel: MyPageViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val modalEffect by viewModel.modalEffect.collectAsStateWithLifecycle()
    val mainAction = LocalMainAction.current
    val navigator = LocalNavigateAction.current

    MyPageContent(
        uiState = uiState,
        onArchiveClick = { navigator.navigateTo(Route.MyPage.Archive) },
        onLogoutClick = mainAction::logout,
        onWithdrawClick = viewModel::requestWithdraw,
    )

    MyPageModalContent(
        modalEffect = modalEffect,
        onConfirmWithdraw = viewModel::withdraw,
        onDismiss = viewModel::dismiss,
    )

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is MyPageUiEffect.ShowMessage -> mainAction.showSnackBar(effect.message)
                // 로컬 정리는 WithdrawUseCase 가 끝냈다. 여기서는 이동만 태운다.
                MyPageUiEffect.Withdrawn -> mainAction.withdrawn()
            }
        }
    }
}

@Composable
private fun MyPageContent(
    uiState: MyPageUiState,
    onArchiveClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onWithdrawClick: () -> Unit,
) {
    ChallengeScaffold(
        statusBarColor = ChallengeTheme.colorScheme.surface,
        topBar = {
            // 기본 titleStyle 이 bold18 이라 탭 헤더 값(bold20)을 명시로 넘긴다. design.md §1.1
            ChallengeTopBar(
                title = "마이페이지",
                titleStyle = ChallengeTheme.typography.bold20,
            )
        },
    ) {
        when (uiState) {
            is MyPageUiState.Loading -> MyPageLoadingScreen()

            is MyPageUiState.Data -> MyPageScreen(
                uiState = uiState,
                onArchiveClick = onArchiveClick,
                onLogoutClick = onLogoutClick,
                onWithdrawClick = onWithdrawClick,
            )
        }
    }
}

@Composable
private fun MyPageModalContent(
    modalEffect: MyPageModalEffect,
    onConfirmWithdraw: () -> Unit,
    onDismiss: () -> Unit,
) {
    when (modalEffect) {
        MyPageModalEffect.Hidden -> Unit

        MyPageModalEffect.ConfirmWithdraw -> ConfirmDialog(
            title = WITHDRAW_TITLE,
            message = WITHDRAW_MESSAGE,
            confirmLabel = "탈퇴하기",
            onConfirm = onConfirmWithdraw,
            onDismiss = onDismiss,
            destructive = true,
        )
    }
}
