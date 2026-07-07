package com.lwg.challenge.feature.friends.list.kakao

import android.content.Context
import android.content.Intent
import com.kakao.sdk.share.ShareClient
import com.lwg.challenge.feature.friends.list.BuildKonfig
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Android 카카오톡 공유(KakaoLink) 진입점.
 *
 * 카카오 콘솔 [메시지 템플릿 도구] 에서 만든 커스텀 템플릿을 [ShareClient.shareCustom] 으로 전송한다.
 * 템플릿 ID 는 local.properties 의 `kakao_invite_template_id` → [BuildKonfig.KAKAO_INVITE_TEMPLATE_ID] 주입.
 * 템플릿의 유저 아규먼트 `nickname` 에 초대한 사람 닉네임을 채운다.
 *
 * 주입되는 [context] 는 application context(Koin `androidContext`) 이므로,
 * 반환된 공유 인텐트는 [Intent.FLAG_ACTIVITY_NEW_TASK] 를 붙여 실행한다.
 */
internal class AndroidKakaoInviter(
    private val context: Context,
) : KakaoInviter {

    override suspend fun sendInvite(inviterNickname: String): Result<Unit> =
        suspendCancellableCoroutine { cont ->
            val templateId = BuildKonfig.KAKAO_INVITE_TEMPLATE_ID.toLongOrNull()
            if (templateId == null) {
                cont.resume(
                    Result.failure(
                        IllegalStateException(
                            "kakao_invite_template_id 가 local.properties 에 설정되지 않았습니다.",
                        ),
                    ),
                )
                return@suspendCancellableCoroutine
            }

            // 카카오톡 설치 시 공유 시트로 진입, 미설치/오류 시 실패 → 상위에서 스낵바.
            ShareClient.instance.shareCustom(
                context = context,
                templateId = templateId,
                templateArgs = mapOf(TEMPLATE_ARG_NICKNAME to inviterNickname),
            ) { sharingResult, error ->
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

    companion object {
        // 콘솔 템플릿의 유저 아규먼트 키와 일치해야 한다.
        private const val TEMPLATE_ARG_NICKNAME = "nickname"
    }
}
