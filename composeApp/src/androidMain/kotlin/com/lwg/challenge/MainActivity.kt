package com.lwg.challenge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.lwg.challenge.feature.main.MainRoute
import com.lwg.challenge.permission.PermissionManager
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            // 권한 컨트롤러는 Koin 싱글톤이라 Activity 를 모른다. 여기서 이 Activity 와
            // 권한 결과 런처를 붙여준다 — 안 하면 권한 요청이 런처를 기다리며 영원히 멈춘다.
            // Activity 가 새로 생길 때마다 다시 붙여야 해서 setContent 안에 둔다.
            koinInject<PermissionManager>().Bind()

            MainRoute(onFinishApp = ::finish)
        }
    }
}
