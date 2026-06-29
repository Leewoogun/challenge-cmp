package com.lwg.challenge.remote.model.user

import com.lwg.challenge.remote.model.BaseResponse
import kotlinx.serialization.Serializable

@Serializable
data class UserInfoResponse(
    val data: UserInfoData = UserInfoData(),
) : BaseResponse()

@Serializable
data class UserInfoData(
    val id: Long = 0L,
    val kakaoId: Long = 0L,
    val nickname: String = "",
    val profileImageUrl: String? = null,
)
