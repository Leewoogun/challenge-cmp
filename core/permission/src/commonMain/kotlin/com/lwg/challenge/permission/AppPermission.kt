package com.lwg.challenge.permission

/**
 * 이 앱이 요구하는 런타임 권한.
 *
 * moko 의 `Permission` 을 그대로 노출하지 않는 이유: 권한 라이브러리는 언제든 갈아끼울 수 있어야
 * 하고, 호출부(ViewModel)가 특정 라이브러리 타입을 알 이유가 없다.
 *
 * 새 권한을 추가할 때는 이 enum 과 `PermissionManagerImpl` 의 매핑, 그리고 `build.gradle.kts` 의
 * `moko-permissions-*` 아티팩트 세 곳을 함께 손본다.
 */
enum class AppPermission {

    /** 푸시 알림. Android 13 미만에서는 런타임 권한이 없어 항상 허용으로 취급된다. */
    NOTIFICATION,

    /** 챌린지 인증 사진 촬영. */
    CAMERA,
}

/**
 * 권한 상태.
 *
 * [DENIED] 와 [DENIED_ALWAYS] 를 나누는 이유가 이 타입의 존재 이유다 — 전자는 다시 물어볼 수
 * 있지만 후자는 시스템 프롬프트가 더 뜨지 않아 설정 앱으로 안내하는 수밖에 없다.
 * 특히 iOS 는 프롬프트가 앱 생애 1회뿐이라 한 번 거부되면 곧바로 [DENIED_ALWAYS] 가 된다.
 */
enum class PermissionStatus {

    GRANTED,

    /** 이번엔 거부됐지만 다시 물어볼 수 있다. */
    DENIED,

    /** 시스템 프롬프트가 더 이상 뜨지 않는다. 설정 앱으로 보내야 한다. */
    DENIED_ALWAYS,
}
