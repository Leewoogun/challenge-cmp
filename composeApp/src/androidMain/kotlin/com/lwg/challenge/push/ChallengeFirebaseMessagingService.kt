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
            data = message.data,
        )
    }

    private fun showNotification(title: String, body: String, data: Map<String, String>) {
        // FCM data 는 값이 전부 문자열로 도착한다. 서버가 숫자로 보내도 여기선 "7" 이다.
        val notificationId = buildNotificationId(data[PushEvent.KEY_CHALLENGE_ID]?.toLongOrNull())

        val notification = NotificationCompat.Builder(this, notificationChannelId(this))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentIntent(notificationId, data))
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

    /** data 를 키·타입 그대로 실어 FCM 이 백그라운드에서 만드는 런처 인텐트와 extras 모양을 맞춘다. */
    private fun contentIntent(
        notificationId: Int,
        data: Map<String, String>,
    ): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            data.forEach { (key, value) -> putExtra(key, value) }
        }

        return PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private fun buildNotificationId(challengeId: Long?): Int =
        challengeId?.toInt() ?: (System.currentTimeMillis() and Int.MAX_VALUE.toLong()).toInt()
}
