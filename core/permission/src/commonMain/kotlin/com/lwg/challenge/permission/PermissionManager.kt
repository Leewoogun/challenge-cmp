package com.lwg.challenge.permission

import androidx.compose.runtime.Composable

/**
 * 런타임 권한 조회·요청 창구. ViewModel 이 주입받아 쓴다.
 *
 * 요청 결과를 예외가 아니라 [PermissionStatus] 로 돌려주므로, 호출부는 `when` 한 번으로
 * "진행 / 무시 / 설정 안내" 를 가른다. 다이얼로그가 필요하면 호출부가 ModalEffect 를 발행한다 —
 * 이 인터페이스는 UI 를 띄우지 않는다.
 *
 * ```
 * fun captureProof() {
 *     viewModelScope.launch {
 *         when (permissionManager.request(AppPermission.CAMERA)) {
 *             PermissionStatus.GRANTED -> _uiEffect.emit(OpenCamera)
 *             PermissionStatus.DENIED -> Unit
 *             PermissionStatus.DENIED_ALWAYS -> _modalEffect.update { PermissionSettings }
 *         }
 *     }
 * }
 * ```
 */
interface PermissionManager {

    /** 요청 없이 현재 상태만 읽는다. */
    suspend fun status(permission: AppPermission): PermissionStatus

    /**
     * 필요하면 시스템 프롬프트를 띄우고 결과를 돌려준다. 이미 허용돼 있으면 프롬프트 없이
     * [PermissionStatus.GRANTED] 로 즉시 끝난다.
     */
    suspend fun request(permission: AppPermission): PermissionStatus

    /** 앱 설정 화면을 연다. [PermissionStatus.DENIED_ALWAYS] 이후의 유일한 복구 경로다. */
    fun openAppSettings()

    /**
     * 이 컨트롤러에 현재 Activity 와 권한 결과 런처를 붙인다.
     *
     * 컨트롤러는 앱 수명 내내 사는 싱글톤이라 Activity 를 모르는데, Android 런타임 권한은
     * `ActivityResultLauncher` 로만 요청할 수 있다. [request] 는 이 둘이 채워질 때까지 suspend 하므로
     * **호출하지 않으면 권한 요청이 예외 없이 영원히 멈춘다.**
     *
     * Activity 가 새로 생길 때마다 다시 붙어야 해서 `@Composable` 이다 — `setContent { }` 등
     * 컴포지션 안에서 불러야 하며, `onCreate` 본문에서는 동작하지 않는다.
     *
     * iOS 권한 API 는 완료 핸들러로 바로 끝나 호스트가 필요 없다. 그래서 iOS 구현은 no-op 이고
     * 호출하지 않아도 된다 — 현재 `MainActivity` 에서만 부른다.
     */
    @Composable
    fun Bind()
}
