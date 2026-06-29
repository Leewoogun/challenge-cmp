package com.lwg.challenge.local.datastore.model

import kotlinx.serialization.Serializable

@Serializable
data class UserInfoPrefs(
    val id: Long = 0L,
    val kakaoId: Long = 0L,
    val nickname: String = "",
    val profileImageUrl: String? = null,
    // 캐시 존재 여부 sentinel — 기본값(id=0)과 "id=0인 실제 사용자"를 구분
    val hasValue: Boolean = false,
)
