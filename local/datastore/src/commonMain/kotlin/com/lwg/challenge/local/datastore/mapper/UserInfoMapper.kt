package com.lwg.challenge.local.datastore.mapper

import com.lwg.challenge.domain.model.user.UserInfo
import com.lwg.challenge.local.datastore.model.UserInfoPrefs

fun UserInfoPrefs.toDomain(): UserInfo = UserInfo(
    id = id,
    kakaoId = kakaoId,
    nickname = nickname,
    profileImageUrl = profileImageUrl,
)

fun UserInfo.toPrefs(): UserInfoPrefs = UserInfoPrefs(
    id = id,
    kakaoId = kakaoId,
    nickname = nickname,
    profileImageUrl = profileImageUrl,
    hasValue = true,
)
