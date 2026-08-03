package com.lwg.challenge.feature.challenge.create.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.feature.challenge.create.contract.ChallengeCreateStep
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 위저드 진행 표시. design.md §1.1 — 단계 수만큼 칸을 그리고 도달한 칸을 primary 로 채운다.
 *
 * 3단계(상대 선택 / 미션 입력 / 영혼의 맹세)라 3칸이 된다. 칸 수를 상수로 박지 않고
 * [ChallengeCreateStep] 개수를 따르므로 단계가 늘거나 줄어도 도달 불가 칸이 생기지 않는다.
 */
@Composable
internal fun WizardProgressBar(
    step: ChallengeCreateStep,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ChallengeCreateStep.entries.forEachIndexed { index, _ ->
            val reached = index <= step.ordinal

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(
                        if (reached) {
                            ChallengeTheme.colorScheme.primary
                        } else {
                            ChallengeTheme.colorScheme.secondary
                        },
                    ),
            )
        }
    }
}

@Preview
@Composable
private fun WizardProgressBarFriendPickPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            WizardProgressBar(step = ChallengeCreateStep.FRIEND_PICK)
        }
    }
}

@Preview
@Composable
private fun WizardProgressBarMissionInputPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            WizardProgressBar(step = ChallengeCreateStep.MISSION_INPUT)
        }
    }
}

@Preview
@Composable
private fun WizardProgressBarOathPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            WizardProgressBar(step = ChallengeCreateStep.OATH)
        }
    }
}
