package com.lwg.challenge.feature.challenge.detail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.feature.challenge.detail.contract.VERIFICATION_REJECTED_PREFIX

/**
 * AI 가 인증을 인정하지 않은 사유 한 줄.
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
        color = ChallengeTheme.colorScheme.text.secondary,
    )
}

@ThemePreviews
@Composable
private fun RejectionReasonTextPreview() {
    RejectionReasonTextPreviewContainer {
        RejectionReasonText(
            text = "${VERIFICATION_REJECTED_PREFIX}책상 위 커피잔 사진으로, 미션 수행 증거로 보기 어렵습니다.",
        )
    }
}

/** 긴 사유 — 잘리지 않고 여러 줄로 흐르는지 확인하는 자리다. */
@ThemePreviews
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
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(20.dp),
        ) {
            content()
        }
    }
}
