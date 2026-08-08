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
            koinInject<PermissionManager>().Bind()

            MainRoute(onFinishApp = ::finish)
        }
    }
}
