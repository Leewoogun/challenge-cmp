import Foundation
import ComposeApp
import KakaoSDKUser
import KakaoSDKCommon

final class KakaoLoginHelperImpl: NSObject, KakaoLoginHandler {

    func login(
        onSuccess: @escaping (String) -> Void,
        onError: @escaping (String) -> Void,
        onCancelled: @escaping () -> Void
    ) {
        if UserApi.isKakaoTalkLoginAvailable() {
            UserApi.shared.loginWithKakaoTalk { [weak self] oauthToken, error in
                if let error = error {
                    if self?.isCancelledError(error) == true {
                        onCancelled()
                        return
                    }
                    self?.loginWithAccount(
                        onSuccess: onSuccess,
                        onError: onError,
                        onCancelled: onCancelled
                    )
                } else if let token = oauthToken?.accessToken {
                    onSuccess(token)
                } else {
                    onError("카카오 로그인에 실패했습니다. 잠시 후 다시 시도해주세요.")
                }
            }
        } else {
            loginWithAccount(
                onSuccess: onSuccess,
                onError: onError,
                onCancelled: onCancelled
            )
        }
    }

    private func loginWithAccount(
        onSuccess: @escaping (String) -> Void,
        onError: @escaping (String) -> Void,
        onCancelled: @escaping () -> Void
    ) {
        UserApi.shared.loginWithKakaoAccount { oauthToken, error in
            if let error = error {
                if self.isCancelledError(error) {
                    onCancelled()
                } else {
                    onError(error.localizedDescription)
                }
            } else if let token = oauthToken?.accessToken {
                onSuccess(token)
            } else {
                onError("카카오 로그인에 실패했습니다. 잠시 후 다시 시도해주세요.")
            }
        }
    }

    private func isCancelledError(_ error: Error) -> Bool {
        guard let sdkError = error as? SdkError else { return false }
        if sdkError.isClientFailed {
            return sdkError.getClientError().reason == .Cancelled
        }
        return false
    }
}
