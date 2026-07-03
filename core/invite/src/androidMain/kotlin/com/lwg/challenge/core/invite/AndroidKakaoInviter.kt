package com.lwg.challenge.core.invite

import android.content.Context
import android.content.Intent
import com.kakao.sdk.share.ShareClient
import com.kakao.sdk.template.model.Link
import com.kakao.sdk.template.model.TextTemplate
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Android 카카오톡 공유(KakaoLink) 진입점.
 *
 * SDK Default [TextTemplate] 을 직접 만들어 [ShareClient.shareDefault] 로 전송한다
 * (콘솔 커스텀 템플릿/templateId 방식 아님).
 *
 * 주입되는 [context] 는 application context(Koin `androidContext`) 이므로,
 * 반환된 공유 인텐트는 [Intent.FLAG_ACTIVITY_NEW_TASK] 를 붙여 실행한다.
 */
internal class AndroidKakaoInviter(
    private val context: Context,
) : KakaoInviter {

    override suspend fun sendInvite(inviterNickname: String): Result<Unit> =
        suspendCancellableCoroutine { cont ->
            val template = TextTemplate(
                text = "맹세?",
                link = Link(
                    mobileWebUrl = "https://developers.kakao.com",
                    androidExecutionParams = mapOf("open" to "true"),
                    iosExecutionParams = mapOf("open" to "true"),
                ),
                buttonTitle = "challenge 앱 열기",
            )

            // 카카오톡 설치 시 공유 시트로 진입, 미설치/오류 시 실패 → 상위에서 스낵바.
            ShareClient.instance.shareDefault(context, template) { sharingResult, error ->
                when {
                    error != null -> if (cont.isActive) cont.resume(Result.failure(error))
                    sharingResult != null -> {
                        context.startActivity(
                            sharingResult.intent.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) },
                        )
                        if (cont.isActive) cont.resume(Result.success(Unit))
                    }
                    else -> if (cont.isActive) {
                        cont.resume(Result.failure(IllegalStateException("empty sharing result")))
                    }
                }
            }
        }
}
