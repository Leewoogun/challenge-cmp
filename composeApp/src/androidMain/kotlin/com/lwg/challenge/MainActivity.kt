package com.lwg.challenge

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.lwg.challenge.feature.main.MainRoute
import com.lwg.challenge.permission.PermissionManager
import com.lwg.challenge.push.PushEvent
import com.lwg.challenge.push.PushEventBus
import org.koin.compose.koinInject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MainActivity : ComponentActivity(), KoinComponent {

    private val pushEventBus: PushEventBus by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        handlePushIntent(intent)

        setContent {
            ChallengeApp {
                koinInject<PermissionManager>().Bind()

                MainRoute(onFinishApp = ::finish)
            }
        }
    }

    /** `launchMode=singleTask` 라 앱이 살아 있을 때 알림을 누르면 `onCreate` 가 아니라 여기로 온다. */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handlePushIntent(intent)
    }

    /** FCM 이 만드는 런처 인텐트와 앱이 만드는 contentIntent 가 같은 키·타입이라 파서는 한 벌이면 된다. */
    private fun handlePushIntent(intent: Intent?) {
        val extras = intent?.extras ?: return
        val data = extras.keySet()
            .mapNotNull { key -> extras.getString(key)?.let { key to it } }
            .toMap()

        val event = PushEvent.from(data) ?: return

        // 화면 회전으로 Activity 가 재생성되면 같은 intent 를 다시 읽어 재이동한다. 즉시 지운다.
        PushEvent.KEYS.forEach(intent::removeExtra)

        pushEventBus.emit(event)
    }
}
