package com.lwg.challenge.remote.model.user

import kotlinx.serialization.Serializable

@Serializable
data class FcmTokenRequest(
    val fcmToken: String,
)
