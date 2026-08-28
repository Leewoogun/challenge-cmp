package com.lwg.challenge.domain.model

import com.lwg.challenge.domain.model.user.UserInfo

/**
 * @property record `null` 이면 전적 조회 실패다. `UserRecord.Empty` 와 구분해야 한다 —
 *   0 승 0 패는 신규 사용자라는 사실 주장이라 실패 자리에 쓰면 거짓말이 된다 (design.md §1.7.3).
 */
data class MyPageData(
    val record: UserRecord?,
    val userInfo: UserInfo? = null,
)
