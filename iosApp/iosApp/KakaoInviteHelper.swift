import Foundation
import UIKit
import ComposeApp
import KakaoSDKShare

/// 카카오톡 공유(친구 초대) iOS 구현.
///
/// Android(`AndroidKakaoInviter`)와 동일하게 콘솔 커스텀 템플릿(`shareCustom`)을 사용하며,
/// 템플릿 유저 아규먼트 `nickname` 키도 동일하게 맞춘다.
final class KakaoInviteHelperImpl: NSObject, KakaoInviteHandler {

    func invite(
        inviterNickname: String,
        onSuccess: @escaping () -> Void,
        onError: @escaping (String) -> Void
    ) {
        guard let templateId = Self.templateId else {
            onError("KAKAO_INVITE_TEMPLATE_ID가 설정되지 않았습니다. Secrets.xcconfig를 확인해주세요.")
            return
        }

        let templateArgs = ["nickname": inviterNickname]

        guard ShareApi.isKakaoTalkSharingAvailable() else {
            shareViaWeb(templateId: templateId, templateArgs: templateArgs, onSuccess: onSuccess, onError: onError)
            return
        }

        ShareApi.shared.shareCustom(
            templateId: templateId,
            templateArgs: templateArgs
        ) { sharingResult, error in
            DispatchQueue.main.async {
                if let error {
                    onError(error.localizedDescription)
                } else if let sharingResult {
                    UIApplication.shared.open(sharingResult.url)
                    onSuccess()
                } else {
                    onError("카카오톡 공유에 실패했습니다.")
                }
            }
        }
    }

    /// 카카오톡 미설치(시뮬레이터 포함) 시 브라우저 웹 공유로 폴백.
    private func shareViaWeb(
        templateId: Int64,
        templateArgs: [String: String],
        onSuccess: @escaping () -> Void,
        onError: @escaping (String) -> Void
    ) {
        guard let url = ShareApi.shared.makeCustomUrl(templateId: templateId, templateArgs: templateArgs) else {
            onError("카카오톡이 설치되어 있지 않고, 웹 공유 URL 생성에도 실패했습니다.")
            return
        }
        DispatchQueue.main.async {
            UIApplication.shared.open(url)
            onSuccess()
        }
    }

    private static var templateId: Int64? {
        guard let raw = Bundle.main.object(forInfoDictionaryKey: "KAKAO_INVITE_TEMPLATE_ID") as? String,
              let id = Int64(raw) else {
            return nil
        }
        return id
    }
}
