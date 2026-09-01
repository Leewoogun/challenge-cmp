package com.lwg.challenge

import androidx.compose.runtime.Composable
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.lwg.challenge.feature.friends.list.di.platformInviteModule
import com.lwg.challenge.di.AppModule
import com.lwg.challenge.feature.challenge.create.di.ChallengeCreateModule
import com.lwg.challenge.feature.challenge.detail.di.ChallengeDetailModule
import com.lwg.challenge.feature.challenge.oath.di.OathModule
import com.lwg.challenge.feature.challenge.verify.di.VerifyModule
import com.lwg.challenge.feature.friends.list.di.FriendsModule
import com.lwg.challenge.feature.friends.search.di.FriendsSearchModule
import com.lwg.challenge.feature.home.di.HomeModule
import com.lwg.challenge.feature.login.di.LoginModule
import com.lwg.challenge.feature.login.di.platformLoginModule
import com.lwg.challenge.feature.main.di.MainModule
import com.lwg.challenge.feature.mypage.di.MyPageModule
import com.lwg.challenge.feature.notification.di.NotificationModule
import com.lwg.challenge.feature.ranking.di.RankingModule
import com.lwg.challenge.feature.splash.di.SplashModule
import com.lwg.challenge.permission.di.platformPermissionModule
import com.lwg.challenge.push.di.platformPushModule
import com.lwg.challenge.push.di.pushModule
import io.ktor.client.HttpClient
import org.koin.compose.koinInject
import org.koin.core.KoinApplication
import org.koin.dsl.KoinAppDeclaration
import org.koin.ksp.generated.module

internal fun challengeAppDeclaration(
    additionalDeclaration: KoinApplication.() -> Unit = {},
): KoinAppDeclaration = {
    modules(
        AppModule().module,
        MainModule().module,
        HomeModule().module,
        LoginModule().module,
        platformLoginModule,
        platformInviteModule,
        platformPushModule,
        pushModule,
        platformPermissionModule,
        SplashModule().module,
        FriendsModule().module,
        FriendsSearchModule().module,
        RankingModule().module,
        MyPageModule().module,
        NotificationModule().module,
        ChallengeCreateModule().module,
        OathModule().module,
        ChallengeDetailModule().module,
        VerifyModule().module,
    )
    additionalDeclaration()
}

/**
 * 플랫폼 진입점(MainActivity / MainViewController)이 화면을 그리기 전에 한 번 감싸는 지점.
 *
 * Coil 싱글턴은 첫 이미지 로드보다 앞서 세워져야 해서 여기서 설정한다. Android 는
 * `ChallengeApplication.onCreate` 의 `startKoin`, iOS 는 `KoinApplication { }` 이 이 Composable 보다
 * 항상 먼저라 [koinInject] 시점은 안전하다.
 */
@Composable
internal fun ChallengeApp(content: @Composable () -> Unit) {
    // 챌린지 인증 사진은 우리 서버의 JWT 보호 경로(GET /api/v1/challenges/{id}/photos/{party}) 라
    // Bearer 가 필요하고, 이 클라이언트의 Auth(bearer) 가 401 이면 토큰을 갱신해 재시도까지 해 준다.
    // 이미지 전용 클라이언트를 새로 만들면 그 갱신 경로를 잃는다.
    // (사진이 서명 URL 이던 예전 계약에선 인증 헤더를 붙이면 안 돼서 전용 클라이언트가 있었지만,
    //  계약이 JWT 로 바뀌며 제거했다. 되살리지 말 것.)
    val httpClient = koinInject<HttpClient>()

    // 같은 사진 URL 의 응답이 404(상대 미인증) → 200(인증 완료) 으로 바뀌는 자리라 404 가 캐시에
    // 남으면 상대가 인증한 뒤에도 사진이 안 뜬다. Coil 3 의 NetworkFetcher 는 2xx 가 아니면 디스크
    // 캐시에 쓰기 전에 HttpException 을 던지므로 오류 응답이 캐시에 남지 않는데, 이 성질에 기대고 있다.
    // 오류 응답까지 저장하는 커스텀 CacheStrategy 를 "성능" 이유로 끼워 넣지 말 것.
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components {
                add(KtorNetworkFetcherFactory(httpClient = httpClient))
            }
            .build()
    }

    content()
}
