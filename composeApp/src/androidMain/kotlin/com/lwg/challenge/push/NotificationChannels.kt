package com.lwg.challenge.push

import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import com.lwg.challenge.R

/**
 * 채널 id 는 AndroidManifest 의 `default_notification_channel_id` meta-data 와 반드시 같아야 한다.
 * (백그라운드 수신 시 시스템이 그 meta-data 값으로 채널을 고른다)
 * 두 곳이 어긋날 수 없도록 양쪽 모두 같은 string 리소스를 참조한다.
 */
fun notificationChannelId(context: Context): String =
    context.getString(R.string.notification_channel_id)

/**
 * 채널 생성은 멱등하다. 같은 id 로 다시 호출해도 사용자가 바꾼 설정을 덮어쓰지 않으므로
 * 매 기동 호출해도 안전하다.
 */
fun createNotificationChannels(context: Context) {
    val channel = NotificationChannelCompat.Builder(
        notificationChannelId(context),
        NotificationManagerCompat.IMPORTANCE_HIGH,
    )
        .setName(context.getString(R.string.notification_channel_name))
        .setDescription(context.getString(R.string.notification_channel_description))
        .build()

    NotificationManagerCompat.from(context).createNotificationChannel(channel)
}
