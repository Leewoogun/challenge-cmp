package com.lwg.challenge.permission

/**
 * 이 앱이 요구하는 런타임 권한.
 * 새 권한을 추가할 때는 이 enum 과 `PermissionManagerImpl` 의 매핑, 그리고 `build.gradle.kts` 의
 * `moko-permissions-*` 아티팩트 세 곳을 함께 손본다.
 */
enum class AppPermission {

    /** 푸시 알림. Android 13 미만에서는 런타임 권한이 없어 항상 허용으로 취급된다. */
    NOTIFICATION,

    /** 챌린지 인증 사진 촬영. */
    CAMERA,
}

enum class PermissionStatus {

    GRANTED,

    /** 이번엔 거부됐지만 다시 물어볼 수 있다. */
    DENIED,

    /** 시스템 프롬프트가 더 이상 뜨지 않는다. 설정 앱으로 보내야 한다. */
    DENIED_ALWAYS,
}
