package com.lwg.challenge.ui.components.card

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.lwg.challenge.domain.model.challenge.ChallengeCardInfo

@Stable
interface ChallengeCardScope {
    val cardInfo: ChallengeCardInfo
}

@Composable
fun rememberChallengeCardScope(cardInfo: ChallengeCardInfo): ChallengeCardScope {
    return remember(cardInfo) {
        object : ChallengeCardScope {
            override val cardInfo: ChallengeCardInfo = cardInfo
        }
    }
}
