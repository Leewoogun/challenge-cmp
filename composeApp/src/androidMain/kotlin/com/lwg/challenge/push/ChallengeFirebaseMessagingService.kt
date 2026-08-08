package com.lwg.challenge.push

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.lwg.challenge.MainActivity
import com.lwg.challenge.R
import com.lwg.challenge.domain.repository.FcmTokenRepository
import com.lwg.challenge.domain.usecase.GetStoredTokensUseCase
import com.lwg.challenge.utils.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ChallengeFirebaseMessagingService : FirebaseMessagingService(), KoinComponent {

    private val fcmTokenRepository: FcmTokenRepository by inject()
    private val getStoredTokensUseCase: GetStoredTokensUseCase by inject()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    /**
     * 앱 프로세스가 죽어 있는 동안에도 호출될 수 있지만, Android 는 Service 생성 전에 Application 을
     * 항상 먼저 생성하므로 ChallengeApplication.onCreate 의 startKoin 은 이 시점에 이미 끝나 있다.
     * 따라서 by inject() 가 안전하다.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        scope.launch {
            if (!getStoredTokensUseCase().isValid) return@launch

            fcmTokenRepository.updateFcmToken(
                fcmToken = token,
                onSuccess = {},
                onError = { message -> Logger.w("FCM 토큰 갱신 등록 실패: $message") },
            )
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Logger.i(
            "FCM 수신: messageId=${message.messageId}, from=${message.from}, " +
                "notification=${message.notification?.let { "title=${it.title}, body=${it.body}" }}, " +
                "data=${message.data}",
        )

        val notification = message.notification ?: run {
            return
        }

        showNotification(
            title = notification.title.orEmpty(),
            body = notification.body.orEmpty(),
            type = message.data[KEY_TYPE],
            // FCM data 는 값이 전부 문자열로 도착한다. 서버가 숫자로 보내도 여기선 "7" 이다.
            challengeId = message.data[KEY_CHALLENGE_ID]?.toLongOrNull(),
        )
    }

    private fun showNotification(title: String, body: String, type: String?, challengeId: Long?) {
        val notificationId = buildNotificationId(challengeId)

        val notification = NotificationCompat.Builder(this, notificationChannelId(this))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentIntent(notificationId, type, challengeId))
            .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(this).notify(notificationId, notification)
    }

    /**
     * 딥링크 라우팅은 이번 범위 밖이다. type/challengeId 를 extra 로 실어만 두고 소비하지 않으므로
     * MainActivity 는 평소대로 splash → 자동 로그인 → 홈 순서로 열린다.
     * 후속 작업이 이 extra 를 읽는 것만으로 상세 진입을 붙일 수 있다.
     */
    private fun contentIntent(
        notificationId: Int,
        type: String?,
        challengeId: Long?,
    ): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            type?.let { putExtra(EXTRA_NOTIFICATION_TYPE, it) }
            challengeId?.let { putExtra(EXTRA_CHALLENGE_ID, it) }
        }

        return PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            // FLAG_IMMUTABLE 미지정 시 Android 12+ 에서 크래시한다.
            // UPDATE_CURRENT 가 없으면 같은 requestCode 재사용 시 예전 extra 가 그대로 남는다.
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    /**
     * 같은 도전장 알림은 겹쳐 쌓이지 않도록 id 를 재사용하고, 식별자가 없는 알림은
     * 매번 다른 id 를 줘서 여러 건이 함께 남게 한다.
     */
    private fun buildNotificationId(challengeId: Long?): Int =
        challengeId?.toInt() ?: (System.currentTimeMillis() and Int.MAX_VALUE.toLong()).toInt()

    companion object {
        const val EXTRA_NOTIFICATION_TYPE = "extra_notification_type"
        const val EXTRA_CHALLENGE_ID = "extra_challenge_id"

        private const val KEY_TYPE = "type"
        private const val KEY_CHALLENGE_ID = "challengeId"
    }
}
