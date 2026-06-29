package com.lwg.challenge.remote.mapper

import com.lwg.challenge.domain.model.user.UserInfo
import com.lwg.challenge.remote.model.user.UserInfoResponse

fun UserInfoResponse.toUserInfo(): UserInfo = UserInfo(
    id = data.id,
    kakaoId = data.kakaoId,
    nickname = data.nickname,
    profileImageUrl = data.profileImageUrl,
)
