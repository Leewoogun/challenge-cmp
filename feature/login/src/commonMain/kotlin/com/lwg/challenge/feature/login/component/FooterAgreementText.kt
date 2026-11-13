package com.lwg.challenge.feature.login.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun FooterAgreementText() {
    val muted = ChallengeTheme.colorScheme.text.secondary
    val emphasized = ChallengeTheme.colorScheme.text.primary

    val text = buildAnnotatedString {
        withStyle(SpanStyle(color = muted)) {
            append("가입과 동시에 ")
        }
        withStyle(SpanStyle(color = emphasized, fontWeight = FontWeight.Bold)) {
            append("이용약관")
        }
        withStyle(SpanStyle(color = muted)) {
            append("·")
        }
        withStyle(SpanStyle(color = emphasized, fontWeight = FontWeight.Bold)) {
            append("개인정보처리방침")
        }
        withStyle(SpanStyle(color = muted)) {
            append("에 동의하며,\n친구의 도발에 응할 마음의 준비를 합니다.")
        }
    }

    Text(
        text = text,
        style = ChallengeTheme.typography.light12,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Preview
@Composable
private fun FooterAgreementTextPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            FooterAgreementText()
        }
    }
}
