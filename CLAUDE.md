# Claude Code 개발 가이드

Kotlin Multiplatform (KMP) + Compose Multiplatform 기반 애플리케이션. 작업 요청 시 매칭되는 Skill이 있으면 **반드시 해당 Skill을 먼저 호출**한다.

## Skill 매핑 테이블

| 사용자 요청 키워드 / 상황 | Skill | 설명 |
|---|---|---|
| "전체 구현", "새 기능 추가", "처음부터 끝까지" | `/full-feature` | Domain→Data→Feature→Navigation 전 레이어 순차 생성 |
| "Domain Layer", "Model/UseCase/Repository 인터페이스" | `/domain` | 순수 Kotlin Model, Repository Interface, UseCase 생성 |
| "API 연동", "Data Layer", "DTO/Ktorfit/RemoteDataSource" | `/data-remote` | DTO, Ktorfit API, Mapper, RemoteDataSource, RepositoryImpl, Koin 등록 |
| "로컬 저장소", "DataStore", "Settings" | `/data-local` | DataStore 기반 LocalDataSource 및 Koin 등록 |
| "Feature 모듈", "화면 만들어줘" (Domain/Data 존재 시) | `/feature` | UiState, Effect, ViewModel, Screen, Navigation 생성 |
| "ViewModel 구현", "Flow 파이프라인", "페이지네이션" | `/viewmodel` | StateFlow 파이프라인, 상태 관리 패턴 |
| "Navigation 연결", "화면 연결", "Route 추가" | `/navigation` | Route 정의, MainScreen NavDisplay 등록, build.gradle 의존성 수정 |
| Figma URL + "UI 만들어줘", "코드로 변환" | `/figma-ui` | Figma 디자인 분석 → Compose UI 코드 변환 + 디자인 시스템 매핑 |
| "UseCase 테스트" | `/test-usecase` | Fake/Mock + Turbine + Coroutines Test 기반 UseCase 테스트 |
| "ViewModel 테스트" | `/test-viewmodel` | StateFlow/SharedFlow 상태 변화 테스트 |
| Compose UI 코드 작성하는 모든 상황 | `design-system` | **자동 적용** - 디자인 시스템 색상/타이포/컴포넌트 규칙 참조 |
| "버그 수정", "에러", "크래시", "오류 수정", "문제 해결" | `/bugfix` | 이슈 분석 → 원인 탐색 → 영향 범위 파악 → 수정 → 빌드 검증 |
| "기존 기능 수정", "필드 추가/삭제", "API 변경 반영", "화면 수정" | `/modify-feature` | 영향 레이어 분석 → 변경 계획 → 레이어별 수정 → 경계면 검증 |
| "코드 정리", "리팩토링", "중복 제거", "코드 개선" | `simplify` | 변경 코드의 재사용성·품질·효율성 리뷰 후 수정 |
| "QA", "통합 검증", "경계면 검사" | Agent(`qa-integration`) | 레이어 간 경계면 교차 비교 (DTO↔Mapper↔Domain, Koin 등록 등) |
| "코드 리뷰", "ViewModel 리뷰", "Flow 리뷰" | Agent(`review-viewmodel`) | ViewModel Flow 파이프라인 중복 API 호출·예외처리 누락 리뷰 |

### Skill 선택 우선순위

1. `/full-feature` → 2. `/modify-feature` → 3. `/bugfix` → 4. 레이어별 Skill (`/domain`, `/data-remote`, `/data-local`, `/feature`) → 5. `/viewmodel` → 6. `/navigation` → 7. `/figma-ui` → 8. 테스트 Skill

## 프로젝트 아키텍처

### 기술 스택
- **Kotlin Multiplatform** (Android + iOS)
- **Compose Multiplatform** (UI)
- **Koin** (DI - KSP annotation 기반)
- **Ktor + Ktorfit** (네트워크)
- **Navigation 3** (`NavDisplay`, `mutableStateListOf<Route>`)
- **kotlinx.serialization** (직렬화)

### 모듈 구조

```
:composeApp                    # 메인 앱 (Android/iOS 엔트리포인트)
:core:designsystem             # UI 컴포넌트, 테마, 디자인 토큰
:core:ui                       # 공통 UI 헬퍼
:core:utils                    # 로깅(Kermit), 공통 유틸, AuthEventBus 등
:core:navigation               # Route 정의 (sealed interface)
:domain:model                  # 도메인 Model (pure Kotlin)
:domain:repository             # Repository 인터페이스 (pure Kotlin)
:domain:usecase                # UseCase (pure Kotlin)
:remote:network                # Ktor 클라이언트, ApiResult, TokenProvider
:remote:api                    # Ktorfit API 인터페이스
:remote:model                  # DTO + Serializable
:remote:mapper                 # DTO ↔ Domain 매퍼
:data:repositoryImpl           # Repository 구현체 (Remote + Local 결합)
:local:datastore               # DataStore 기반 LocalDataSource
:feature:main                  # 메인 Navigation 코디네이터 (BottomNav)
:feature:home                  # 홈 화면
:feature:{name}                # 각 기능별 Feature 모듈
```

> 이전 가이드의 `:core:domain` / `:core:network` / `:core:data` 단일 모듈은 ADR-0003에 따라
> `:domain:*` / `:remote:*` / `:data:*` 다중 모듈로 세분화됨.

### Clean Architecture 의존성 규칙
- ✅ Feature → Domain, Data → Domain
- ❌ Domain → Data, Domain → Feature (절대 금지)

### KMP 소스셋 구조
- `commonMain` — 공통 비즈니스 로직, UI, DI 모듈 정의
- `androidMain` — Android 전용 구현 (expect/actual)
- `iosMain` — iOS 전용 구현 (expect/actual)

## 프로젝트 컨벤션

### DI (Koin Annotations)
```kotlin
// ViewModel
@KoinViewModel
class SomeViewModel(
    private val someRepository: SomeRepository,
) : ViewModel() { }

// Repository 구현체
@Single
class SomeRepositoryImpl(
    private val api: SomeApi,
) : SomeRepository { }

// 모듈 정의 (각 feature/core 모듈별)
@Module
@ComponentScan("com.lwg.challenge.feature.home")
class HomeModule
```

### StateFlow 상태 관리
- `MutableStateFlow` + `.update { }` 패턴 (`.value =` 금지 - race condition)
- 파생 Flow 파이프라인: `combine`, `flatMapLatest`, `map` → `stateIn`

### Flow 수집 규칙
- ViewModel: `viewModelScope.launch { flow.collect { } }` 사용, `.onEach { }.launchIn()` 금지
- Composable: `collectAsStateWithLifecycle()` 사용

### UI State 파싱 규칙
- Composable에서 문자열 포맷팅/조건 판별/파생 계산 금지
- 모든 표시용 데이터는 State/ItemState의 `get()` 프로퍼티로 제공

```kotlin
// ✅ ItemState에서 get()으로 파싱
@Immutable
data class SomeItemState(private val model: SomeDomainModel) {
    val dateText: String get() = "${model.date.substring(0, 4)}-${model.date.substring(4, 6)}"
    val isError: Boolean get() = model.statusCode == "50"
}
// ❌ Composable에서 직접 파싱
```

### Compose Preview 규칙 (필수)
- 새 `@Composable` UI를 작성/수정하면 **반드시 같은 파일 하단에 `@Preview` 를 함께 작성한다** (Screen / Component / designsystem 공용 컴포넌트 모두 해당)
- `private @Composable fun {Name}Preview()` 형태, `ChallengeTheme { }` 래핑 + `ChallengeTheme.colorScheme.background` 배경
- 상태 분기(Loading/Data/Empty, 선택/비선택 등)가 있으면 분기별 Preview 작성
- import 는 `org.jetbrains.compose.ui.tooling.preview.Preview` (KMP) 사용
- 세부 규칙은 `design-system` 스킬의 "Preview 필수 규칙" 참조

### 함수 네이밍 규칙
- **Composable 콜백**: `on` 접두어 (`onTabSelected`, `onSearchClick`)
- **ViewModel 함수**: `on` 없음 (`selectTab`, `search`)

### Navigation 3 패턴
```kotlin
// Route 정의 (core/navigation)
sealed interface Route {
    @Serializable
    sealed interface HomeRoute : Route {
        @Serializable
        data object Main : HomeRoute
    }
}

// MainScreen에서 NavDisplay로 라우팅
val backStack = remember { mutableStateListOf<Route>(Route.HomeRoute.Main) }
NavDisplay(backStack = backStack, ...) { ... }
```

### Navigation 추가 시
- 해당 feature 모듈이 `:feature:main`의 `build.gradle.kts`에 의존성으로 추가되어 있는지 확인
- `settings.gradle.kts`에 모듈 등록 확인

### 빌드 검증
```bash
# KMP 모듈 빌드
./gradlew :{모듈}:compileCommonMainKotlinMetadata

# Android 타겟 빌드
./gradlew :{모듈}:compileDebugKotlinAndroid
```

## 서브에이전트 (`.claude/agents/`)

사용자 요청 시 `.claude/agents/{name}.md` 파일을 읽고, 그 내용을 프롬프트로 Agent tool을 실행한다.

| 에이전트 | 트리거 | 실행 방법 |
|---|---|---|
| `qa-integration.md` | "QA", "통합 검증", "경계면 검사", `/full-feature` Phase 5 | `.claude/agents/qa-integration.md`를 읽고 Agent tool로 실행 |
| `review-viewmodel.md` | "코드 리뷰", "ViewModel 리뷰", "Flow 리뷰" | `.claude/agents/review-viewmodel.md`를 읽고 Agent tool로 실행 |
| `figma-analyzer.md` | `/full-feature` Phase 0 (Figma URL 있을 때) | `/full-feature` 오케스트레이터가 서브에이전트로 호출 |
| `api-analyzer.md` | `/full-feature` Phase 0 (API 스펙 있을 때) | `/full-feature` 오케스트레이터가 서브에이전트로 호출 |
| `data-remote-builder.md` | `/full-feature` Phase 2 (API 있을 때) | `/full-feature` 오케스트레이터가 서브에이전트로 호출 |
| `data-local-builder.md` | `/full-feature` Phase 2 (로컬 저장 필요 시) | `/full-feature` 오케스트레이터가 서브에이전트로 호출 |
