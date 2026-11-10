# Challenge

친구끼리 걸었던 내기를 **기록으로 남기는** 앱.

말로만 "야 내기하자" 하고 끝나버리는 게 싫어서 만들었습니다.
서로 미션과 내기를 걸고, 계약서에 서명하고, 결과가 전적으로 쌓입니다.

Kotlin Multiplatform + Compose Multiplatform 기반 (Android / iOS).

---

## 기술 스택

| 영역 | 사용 기술 |
|---|---|
| 플랫폼 | Kotlin Multiplatform (Android, iOS) |
| UI | Compose Multiplatform |
| 화면 이동 | Navigation 3 |
| DI | Koin (KSP annotation) |
| 네트워크 | Ktor + Ktorfit |
| 직렬화 | kotlinx.serialization |
| 로컬 저장 | DataStore, Room |
| 로그인 | Kakao SDK |
| 푸시 | Firebase Cloud Messaging |
| 권한 | moko-permissions |
| 로깅 | Kermit |
| 테스트 | kotlin-test, Turbine, Coroutines Test, Roborazzi |
| 빌드 | Gradle convention plugin (`build-logic`), detekt |

---

## 프로젝트 구조

Clean Architecture 기반 멀티 모듈. 의존성은 항상 `Feature/Data → Domain` 한 방향입니다.

```
:composeApp          앱 엔트리포인트 (Android / iOS)

:domain:model        도메인 모델 (pure Kotlin)
:domain:repository   Repository 인터페이스
:domain:usecase      UseCase

:remote:network      Ktor 클라이언트, ApiResult, 토큰 갱신
:remote:api          Ktorfit API 인터페이스
:remote:model        DTO
:remote:mapper       DTO ↔ Domain 매퍼
:remote:datasource   RemoteDataSource 구현체

:data:datasource     DataSource 인터페이스 (도메인 모델 반환)
:data:repositoryImpl Repository 구현체

:local:datastore     DataStore (토큰, 유저 정보, 앱 설정)
:local:database      Room DB

:core:designsystem   테마, 디자인 토큰, 공용 컴포넌트
:core:ui             여러 feature 가 공유하는 UI
:core:navigation     Route 정의
:core:utils          로깅, 공통 유틸
:core:push           FCM 토큰, 푸시 이벤트 버스
:core:permission     플랫폼별 권한 요청

:feature:splash      스플래시 / 자동 로그인
:feature:login       카카오 로그인
:feature:main        BottomNav + Navigation 코디네이터
:feature:home        홈 (전적 / 진행 중 챌린지 / 받은 도전장)
:feature:friends:*   친구 목록, 친구 검색
:feature:challenge:* 챌린지 생성, 상세, 맹세
:feature:ranking     랭킹
:feature:mypage:*    마이페이지, 계약서 보관함
```

데이터 흐름:

```
ViewModel → UseCase → Repository → DataSource(Remote/Local) → Api / DataStore
```

Repository 는 Api 를 직접 주입받지 않습니다. DTO 매핑과 `ApiResult` 처리는 전부 RemoteDataSource 책임입니다.

---

## 구현된 기능

### 인증
- 카카오 로그인
- 테스트 계정 로그인 (debug 전용)
- 토큰 저장 및 자동 로그인, 만료 시 자동 갱신
- 로그아웃

### 친구
- 닉네임으로 유저 검색
- 친구 요청 보내기 / 취소
- 받은 요청 수락 / 거절
- 친구 목록

### 챌린지
- 챌린지 신청 (상대 선택, 내 미션, 상대 미션, 내기, 마감 시각)
- 받은 도전장 목록
- 수락 (맹세 화면에서 내 미션 입력 + 서명 후 시작) / 거절 / 취소
- 챌린지 상세 — 계약서 + 양측 서명 확인
- 진행 중 챌린지 목록

### 홈
- 누적 전적 (승 / 패 / 무 / 연승)
- 진행 중 챌린지 카드
- 받은 도전장

### 푸시
- FCM 토큰 등록 (로그인 직후 / 토큰 갱신 시)
- 알림 권한 요청 (Android / iOS 공통)
- 챌린지 신청·수락·거절 알림
- 알림 탭 시 해당 화면으로 이동

---

## 추가 구현이 필요한 기능

### 우선순위 높음
- **챌린지 인증** — 도메인에 `VerificationStatus`(PENDING / VERIFIED / FAILED) 만 있고 인증 API·화면이 없습니다. 사진 인증 업로드, 상대 인증 확인 플로우 필요
- **챌린지 결과 처리** — 마감 후 승패 확정과 결과 화면
- **랭킹 화면** — 현재 `PlaceholderScreen` 스텁
- **마이페이지** — 현재 로그아웃 버튼만 있는 스텁. 프로필, 내 전적, 설정 필요
- **알림 목록 화면** — Route(`Route.Notifications`) 만 있고 화면은 스텁

### 우선순위 중간
- **iOS 푸시** — `FcmTokenProviderIos` 가 no-op. APNs 인증키(유료 Apple Developer Program) 발급 후 연동 필요
- **챌린지 히스토리** — 끝난 챌린지 다시 보기
- **테스트 보강** — 현재 `Signature`, `PushEvent` 정도만 커버

---

## 빌드

```bash
# 공통 코드 컴파일 확인
./gradlew :composeApp:compileCommonMainKotlinMetadata

# Android
./gradlew :composeApp:assembleDebug

# 특정 모듈만
./gradlew :feature:home:compileDebugKotlinAndroid
```

iOS 는 `iosApp/iosApp.xcodeproj` 를 Xcode 로 열어 실행합니다.

### 필요한 로컬 설정

`local.properties` (VCS 미포함):

```properties
kakao_native_app_key = "..."
kakao_invite_template_id = "..."
```

#### API 서버

`core/utils` 의 `Constants.BASE_URL` 로 관리합니다. debug 빌드는 로컬 개발 서버, release 빌드는 운영 서버 `https://api.woo-challenge.shop/` 를 봅니다. 로컬 서버 주소는 `local.properties` 에 적고, 없으면 debug 빌드도 운영 서버를 봅니다. 평문 HTTP 는 Android debug 빌드에서만 허용됩니다.

```properties
challenge_api_base_url_android = "http://<server>:8080/"
challenge_api_base_url_ios = "http://<server>:8080/"
```

Android 푸시는 `composeApp/google-services.json` 이 필요합니다.
