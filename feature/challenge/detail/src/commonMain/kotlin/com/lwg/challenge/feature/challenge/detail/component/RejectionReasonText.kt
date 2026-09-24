package com.lwg.challenge.feature.challenge.detail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.feature.challenge.detail.contract.VERIFICATION_REJECTED_PREFIX
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * AI 가 인증을 인정하지 않은 사유 한 줄. 인증 사진 아래에 붙는다.
 *
 * 접두어까지 붙은 완성 문구를 받는다 — 조립은
 * [com.lwg.challenge.feature.challenge.detail.contract.ChallengeDetailUiState.Data.myAiReasonText]
 * 가 끝낸다. 내 카드와 상대 카드가 같은 문장을 그려야 하는데 여기서 조립하면 호출부마다 갈릴 수 있다.
 *
 * `error` 색을 쓰지 않는다: 같은 카드의
 * [com.lwg.challenge.ui.components.VerificationStatusPill] 이 이미 그 색으로 "실패" 를 말하고 있어,
 * 사유까지 같은 색이면 한 카드 안에서 둘이 경쟁한다. 사유는 판정이 아니라 판정에 붙는 설명이라
 * 위계가 한 단계 아래다 — `VsHeaderCard` 의 기권 안내 한 줄이 헤드라인의 톤 색을 버린 것과 같은 판단.
 *
 * `maxLines` 로 자르지 않는다: 이 문구는 사용자가 왜 졌는지 알 수 있는 유일한 설명이고 이의제기
 * 절차가 없다. 길면 줄바꿈되게 둔다.
 */
@Composable
internal fun RejectionReasonText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        style = ChallengeTheme.typography.medium12,
        color = ChallengeTheme.colorScheme.onSurfaceVariant,
    )
}

@Preview
@Composable
private fun RejectionReasonTextPreview() {
    RejectionReasonTextPreviewContainer {
        RejectionReasonText(
            text = "${VERIFICATION_REJECTED_PREFIX}책상 위 커피잔 사진으로, 미션 수행 증거로 보기 어렵습니다.",
        )
    }
}

/** 긴 사유 — 잘리지 않고 여러 줄로 흐르는지 확인하는 자리다. */
@Preview
@Composable
private fun RejectionReasonTextLongPreview() {
    RejectionReasonTextPreviewContainer {
        RejectionReasonText(
            text = "${VERIFICATION_REJECTED_PREFIX}실내에서 찍힌 책상 위 커피잔 사진으로 보이며, " +
                "미션으로 약속한 야외 달리기를 수행했다고 볼 만한 장면이나 기록이 사진 어디에서도 확인되지 않습니다.",
        )
    }
}

@Composable
private fun RejectionReasonTextPreviewContainer(content: @Composable () -> Unit) {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            content()
        }
    }
}
