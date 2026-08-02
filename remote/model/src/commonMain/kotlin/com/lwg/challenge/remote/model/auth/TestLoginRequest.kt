package com.lwg.challenge.remote.model.auth

import kotlinx.serialization.Serializable

/**
 * POST /api/v1/auth/test-login 요청 (debug 전용 개발 도구).
 *
 * @property testUserNo 테스트 계정 번호 (1~3). 서버가 소유한 닉네임은 `테스터{testUserNo}`.
 */
@Serializable
data class TestLoginRequest(
    val testUserNo: Int,
)
