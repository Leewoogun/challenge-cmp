import SwiftUI
import ComposeApp
import KakaoSDKCommon
import KakaoSDKAuth

@main
struct iOSApp: App {

    init() {
        initKakaoSdk()
        KakaoLoginSetupKt.registerKakaoLoginHandler(handler: KakaoLoginHelperImpl())
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    if AuthApi.isKakaoTalkLoginUrl(url) {
                        _ = AuthController.handleOpenUrl(url: url)
                    }
                }
        }
    }

    private func initKakaoSdk() {
        guard let key = Bundle.main.object(forInfoDictionaryKey: "KAKAO_NATIVE_APP_KEY") as? String,
              !key.isEmpty else {
            print("[iOSApp] KAKAO_NATIVE_APP_KEY가 비어 있습니다. Config.xcconfig에 값을 채워주세요.")
            return
        }
        KakaoSDK.initSDK(appKey: key)
    }
}
