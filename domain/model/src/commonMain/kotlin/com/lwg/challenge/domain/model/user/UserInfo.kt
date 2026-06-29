package com.lwg.challenge.domain.model.user

data class UserInfo(
    val id: Long,
    val kakaoId: Long,
    val nickname: String,
    val profileImageUrl: String?,
)
