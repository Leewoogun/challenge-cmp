package com.lwg.challenge.feature.login

import com.lwg.challenge.feature.login.kakao.KakaoAuthClient
import com.lwg.challenge.feature.login.kakao.KakaoAuthResult

/**
 * 테스트용 Fake KakaoAuthClient. 미리 [result] 를 세팅해두면 [requestAccessToken] 이 그대로 반환.
 */
class FakeKakaoAuthClient : KakaoAuthClient {

    var result: KakaoAuthResult = KakaoAuthResult.Success(accessToken = "fake-kakao-access-token")

    override suspend fun requestAccessToken(): KakaoAuthResult = result
}
