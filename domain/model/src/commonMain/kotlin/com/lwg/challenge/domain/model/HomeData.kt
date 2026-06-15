package com.lwg.challenge.domain.model

data class HomeData(
    val record: UserRecord,
    val challenges: List<ActiveChallenge>,
)
