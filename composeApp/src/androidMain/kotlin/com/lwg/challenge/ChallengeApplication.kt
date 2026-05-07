package com.lwg.challenge

import android.app.Application
import android.util.Log
import com.kakao.sdk.common.KakaoSdk
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ChallengeApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        initKakaoSdk()

        startKoin(
            cookingAppDeclaration {
                androidContext(this@ChallengeApplication)
            },
        )
    }

    /**
     * Kakao SDK 초기화. NATIVE_APP_KEY 가 비어 있으면 SDK 호출 시점에 친화적 에러로 fallback 되므로
     * (KakaoLoginProvider 의 Failure 매핑) 빌드 자체는 통과시키고 경고만 남긴다.
     */
    private fun initKakaoSdk() {
        val key = BuildKonfig.KAKAO_NATIVE_APP_KEY
        if (key.isBlank()) {
            Log.w(
                TAG,
                "KAKAO_NATIVE_APP_KEY 가 비어 있습니다. " +
                    "local.properties 의 kakao_native_app_key 에 카카오 개발자 콘솔의 NATIVE APP KEY 를 채워주세요.",
            )
            return
        }
        KakaoSdk.init(this, key)
    }

    private companion object {
        const val TAG = "ChallengeApplication"
    }
}
