package com.lwg.challenge.domain.model

import com.lwg.challenge.domain.model.challenge.ReceivedChallenge
import com.lwg.challenge.domain.model.user.UserInfo

data class HomeData(
    val record: UserRecord,
    val challenges: List<ActiveChallenge>,
    val userInfo: UserInfo? = null,
    val receivedChallenges: List<ReceivedChallenge> = emptyList(),
)
