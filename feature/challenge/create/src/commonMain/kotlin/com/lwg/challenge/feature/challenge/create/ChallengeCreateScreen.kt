package com.lwg.challenge.feature.challenge.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.challenge.DeadlineType
import com.lwg.challenge.domain.model.friend.Friend
import com.lwg.challenge.domain.model.signature.Signature
import com.lwg.challenge.domain.model.signature.SignaturePoint
import com.lwg.challenge.domain.model.signature.SignatureStroke
import com.lwg.challenge.feature.challenge.create.component.FriendPickStep
import com.lwg.challenge.feature.challenge.create.component.MissionInputStep
import com.lwg.challenge.feature.challenge.create.component.OathStep
import com.lwg.challenge.feature.challenge.create.contract.ChallengeCreateStep
import com.lwg.challenge.feature.challenge.create.contract.ChallengeCreateUiState
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.ui.tooling.preview.Preview

/** 챌린지 생성 위저드 본문. 단계에 따라 상대 선택 / 미션 입력 / 영혼의 맹세를 렌더한다. */
@Composable
internal fun ChallengeCreateScreen(
    uiState: ChallengeCreateUiState.Data,
    onSelectFriend: (Long) -> Unit,
    onClickAddFriend: () -> Unit,
    onRetryLoadFriends: () -> Unit,
    onMyMissionChange: (String) -> Unit,
    onBetContentChange: (String) -> Unit,
    onDeadlineChange: (DeadlineType) -> Unit,
    onNext: () -> Unit,
    onSignatureChange: (Signature) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState.step) {
        ChallengeCreateStep.FRIEND_PICK -> FriendPickStep(
            uiState = uiState,
            onSelectFriend = onSelectFriend,
            onClickAddFriend = onClickAddFriend,
            onRetryLoadFriends = onRetryLoadFriends,
            modifier = modifier,
        )

        ChallengeCreateStep.MISSION_INPUT -> MissionInputStep(
            uiState = uiState,
            onMyMissionChange = onMyMissionChange,
            onBetContentChange = onBetContentChange,
            onDeadlineChange = onDeadlineChange,
            onNext = onNext,
            modifier = modifier,
        )

        ChallengeCreateStep.OATH -> OathStep(
            uiState = uiState,
            onSignatureChange = onSignatureChange,
            onSubmit = onSubmit,
            modifier = modifier,
        )
    }
}

@Preview
@Composable
private fun ChallengeCreateScreenFriendPickPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier.background(ChallengeTheme.colorScheme.background)
        ) {
            ChallengeCreateScreen(
                uiState = previewState(
                    friends = listOf(previewFriend(1, "우건"), previewFriend(2, "도전왕")),
                ),
                onSelectFriend = {},
                onClickAddFriend = {},
                onRetryLoadFriends = {},
                onMyMissionChange = {},
                onBetContentChange = {},
                onDeadlineChange = {},
                onNext = {},
                onSignatureChange = {},
                onSubmit = {},
            )
        }
    }
}

@Preview
@Composable
private fun ChallengeCreateScreenFriendPickEmptyPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            ChallengeCreateScreen(
                uiState = previewState(friends = emptyList()),
                onSelectFriend = {},
                onClickAddFriend = {},
                onRetryLoadFriends = {},
                onMyMissionChange = {},
                onBetContentChange = {},
                onDeadlineChange = {},
                onNext = {},
                onSignatureChange = {},
                onSubmit = {},
            )
        }
    }
}

@Preview
@Composable
private fun ChallengeCreateScreenFriendPickLoadFailedPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            ChallengeCreateScreen(
                uiState = previewState(friends = emptyList(), loadFailed = true),
                onSelectFriend = {},
                onClickAddFriend = {},
                onRetryLoadFriends = {},
                onMyMissionChange = {},
                onBetContentChange = {},
                onDeadlineChange = {},
                onNext = {},
                onSignatureChange = {},
                onSubmit = {},
            )
        }
    }
}

@Preview
@Composable
private fun ChallengeCreateScreenMissionInputPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            ChallengeCreateScreen(
                uiState = previewState(
                    step = ChallengeCreateStep.MISSION_INPUT,
                    friends = listOf(previewFriend(1, "우건")),
                    selectedFriendId = 1L,
                ),
                onSelectFriend = {},
                onClickAddFriend = {},
                onRetryLoadFriends = {},
                onMyMissionChange = {},
                onBetContentChange = {},
                onDeadlineChange = {},
                onNext = {},
                onSignatureChange = {},
                onSubmit = {},
            )
        }
    }
}

@Preview
@Composable
private fun ChallengeCreateScreenMissionInputFilledPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            ChallengeCreateScreen(
                uiState = previewState(
                    step = ChallengeCreateStep.MISSION_INPUT,
                    friends = listOf(previewFriend(1, "우건")),
                    selectedFriendId = 1L,
                    myMission = "오늘 운동 1시간 하기",
                    betContent = "커피 사기",
                ),
                onSelectFriend = {},
                onClickAddFriend = {},
                onRetryLoadFriends = {},
                onMyMissionChange = {},
                onBetContentChange = {},
                onDeadlineChange = {},
                onNext = {},
                onSignatureChange = {},
                onSubmit = {},
            )
        }
    }
}

@Preview
@Composable
private fun ChallengeCreateScreenOathPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            ChallengeCreateScreen(
                uiState = previewState(
                    step = ChallengeCreateStep.OATH,
                    friends = listOf(previewFriend(1, "우건")),
                    selectedFriendId = 1L,
                    myMission = "오늘 운동 1시간 하기",
                    betContent = "커피 사기",
                    signature = previewSignature(),
                ),
                onSelectFriend = {},
                onClickAddFriend = {},
                onRetryLoadFriends = {},
                onMyMissionChange = {},
                onBetContentChange = {},
                onDeadlineChange = {},
                onNext = {},
                onSignatureChange = {},
                onSubmit = {},
            )
        }
    }
}

@Preview
@Composable
private fun ChallengeCreateScreenSubmittingPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            ChallengeCreateScreen(
                uiState = previewState(
                    step = ChallengeCreateStep.OATH,
                    friends = listOf(previewFriend(1, "우건")),
                    selectedFriendId = 1L,
                    myMission = "오늘 운동 1시간 하기",
                    betContent = "커피 사기",
                    signature = previewSignature(),
                    isSubmitting = true,
                ),
                onSelectFriend = {},
                onClickAddFriend = {},
                onRetryLoadFriends = {},
                onMyMissionChange = {},
                onBetContentChange = {},
                onDeadlineChange = {},
                onNext = {},
                onSignatureChange = {},
                onSubmit = {},
            )
        }
    }
}

private fun previewFriend(id: Long, nickname: String) = Friend(
    userId = id,
    nickname = nickname,
    profileImageUrl = null,
    since = LocalDateTime.parse("2026-01-01T00:00:00"),
)

private fun previewSignature() = Signature(
    strokes = listOf(
        SignatureStroke(
            listOf(
                SignaturePoint(120, 620),
                SignaturePoint(300, 360),
                SignaturePoint(480, 640),
                SignaturePoint(700, 400),
            ),
        ),
    ),
)

private fun previewState(
    step: ChallengeCreateStep = ChallengeCreateStep.FRIEND_PICK,
    friends: List<Friend> = emptyList(),
    selectedFriendId: Long? = null,
    myMission: String = "",
    betContent: String = "",
    signature: Signature = Signature.Empty,
    isSubmitting: Boolean = false,
    loadFailed: Boolean = false,
) = ChallengeCreateUiState.Data(
    step = step,
    friends = friends,
    selectedFriendId = selectedFriendId,
    myMission = myMission,
    betContent = betContent,
    deadlineType = DeadlineType.TODAY,
    todayDeadlineHint = "7/28 24:00",
    tomorrowDeadlineHint = "7/29 24:00",
    signature = signature,
    isSubmitting = isSubmitting,
    loadFailed = loadFailed,
)
