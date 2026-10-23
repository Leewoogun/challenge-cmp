# Claude Code 개발 가이드

Kotlin Multiplatform (KMP) + Compose Multiplatform 기반 애플리케이션. 작업 요청 시 매칭되는 Skill이 있으면 **반드시 해당 Skill을 먼저 호출**한다.

> ⚠️ **다른 프로젝트(PM/오케스트레이터)에서 이 repo 를 수정할 때**: 절대경로 reach-in 편집 금지. 반드시 이 repo 를 cwd 로 하는 세션에 위임한다 (그래야 CLAUDE.md/skills/hooks/메모리가 로드됨). 절차: [`docs/agent-delegation-guide.md`](docs/agent-delegation-guide.md)

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
| "XX 모듈 정리/리팩토링", "컨벤션에 맞게 고쳐줘", "State/ViewModel 컨벤션 점검" | `/refactor-feature` | 기존 feature 모듈 점검 리포트 → 결정 → 레이어별 정리 (동작 유지) |
| "코드 정리", "리팩토링", "중복 제거", "코드 개선" | `simplify` | 변경 코드의 재사용성·품질·효율성 리뷰 후 수정 |
| "QA", "통합 검증", "경계면 검사" | Agent(`qa-integration`) | 레이어 간 경계면 교차 비교 (DTO↔Mapper↔Domain, Koin 등록 등) |
| "코드 리뷰", "ViewModel 리뷰", "Flow 리뷰" | Agent(`review-viewmodel`) | ViewModel Flow 파이프라인 중복 API 호출·예외처리 누락 리뷰 |

### Skill 선택 우선순위

1. `/full-feature` → 2. `/modify-feature` → 3. `/refactor-feature` → 4. `/bugfix` → 5. 레이어별 Skill (`/domain`, `/data-remote`, `/data-local`, `/feature`) → 6. `/viewmodel` → 7. `/navigation` → 8. `/figma-ui` → 9. 테스트 Skill

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
:remote:datasource             # RemoteDataSource 구현체 (Api 호출 + Mapper + ApiResult 처리)
:data:datasource               # DataSource 인터페이스 (remote/ + local/, 도메인 모델 반환)
:data:repositoryImpl           # Repository 구현체 (DataSource 조합)
:local:datastore               # DataStore 저장소 + LocalDataSource 구현체
:local:database                # Room DB + LocalDataSource
:feature:main                  # 메인 Navigation 코디네이터 (BottomNav)
:feature:home                  # 홈 화면
:feature:{name}                # 각 기능별 Feature 모듈
```

> 이전 가이드의 `:core:domain` / `:core:network` / `:core:data` 단일 모듈은 ADR-0003에 따라
> `:domain:*` / `:remote:*` / `:data:*` 다중 모듈로 세분화됨.

### 데이터 레이어 계층

```
RepositoryImpl (:data:repositoryImpl)     캐시 전략·세션 영속화 등 조합 정책만
      ↓ 인터페이스
XxxRemoteDataSource / XxxLocalDataSource (:data:datasource)   도메인 모델 반환
      ↑ 구현
XxxRemoteDataSourceImpl (:remote:datasource) → :remote:api (Ktorfit)
XxxLocalDataSourceImpl  (:local:datastore/datasourceImpl) → XxxDataStore (:local:datastore/datastore)
```

- **Repository 는 Api 를 직접 주입받지 않는다.** DTO 매핑·`ApiResult` 처리는 전부 RemoteDataSource 책임
- `XxxDataStore` 는 DataStore 파일 I/O 전담. Repository 는 `XxxLocalDataSource` 인터페이스만 본다
- Koin: `RemoteDataSourceModule` 이 `ApiModule` 을 include 하고, `DataModule` 이 그것을 include

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
- Composable에서 조건 판별/파생 계산 금지. 표시용 파생값은 State의 `get()` / `fun` 으로 제공
  - 화면 단위 파생값 → Data 의 `get()`
  - 아이템 1개에만 의존하는 파생값 → Data 의 `fun` (호출부: `uiState::isActionable`)
- **문자열 포맷팅은 예외**: 아이템 문자열을 만들려고 도메인 모델을 감싸는 ItemState 를 새로 분리해야 한다면, 분리하지 말고 Composable 에서 모델 값으로 조합한다 (`"${challenge.opponent.nickname}의 미션"`). 분리 없이 Data 의 `get()` 으로 낼 수 있는 문자열은 `get()` 으로 둔다
- **도메인 모델은 그대로 UiState에 담는다** — 게터만 위임하거나 문자열만 만드는 ItemState 는 만들지 않는다
- 도메인 모델은 `compose_compiler_config.conf` 에 등록돼 stable 로 취급되므로 그대로 넘겨도 skipping 이 동작한다

```kotlin
@Immutable
data class Data(
    // ✅ 도메인 모델 그대로
    val challenges: List<Challenge>,
    val now: LocalDateTime,
) : SomeUiState {

    // ✅ 화면 단위 파생값은 Data 의 get()
    val isChallengesEmpty: Boolean get() = challenges.isEmpty()
    val headerText: String get() = "진행 중 ${challenges.size}개"

    // ✅ 아이템 1개 + 모델 밖 값(now 등)이 필요한 파생값은 Data 의 fun
    fun deadlineTextOf(challenge: Challenge): String = challenge.deadline.toRelativeKoreanString(now)
    fun isUrgent(challenge: Challenge): Boolean = ...
}

// ✅ 아이템 문자열은 ItemState 를 만들지 않고 Composable 에서 조합
@Composable
fun ChallengeCard(challenge: Challenge, deadlineText: String) {
    Text("${challenge.opponent.nickname}의 미션")
    Text(deadlineText)
}

// ❌ 문자열 포맷팅만을 위해 모델을 감싸는 ItemState
data class ChallengeItemState(private val model: Challenge) {
    val missionLabelText: String get() = "${model.opponent.nickname}의 미션"
}
```

`get()` 주의: 매 호출마다 계산되며 캐시되지 않는다.
- 값 타입(String/Boolean/Int) 반환은 사실상 무료 — 그냥 쓴다
- **새 참조 객체(List/Map/람다)를 반환하면 금지** — 매번 다른 인스턴스라 하위 Composable 의 skipping 이 깨진다. 생성자 `val` 로 고정하거나 ViewModel 매핑 시점에 확정할 것

### 화면 파일 구조
- UiState 분기(Loading / Empty / Error / Data)마다 화면을 `screen/` 폴더에 **1파일 = 1화면**으로 둔다
  - 이름: `{Name}{State}Screen` (Data 는 `{Name}Screen`) — 예: `screen/FriendsScreen.kt`, `screen/FriendsLoadingScreen.kt`, `screen/FriendsEmptyScreen.kt`
  - 각 파일에는 그 화면의 Preview 만 둔다. 한 파일에 여러 화면을 몰아넣지 않는다
- 다이얼로그·하위 UI 는 `component/` 에 둔다

### Compose Preview 규칙 (필수)
- 새 `@Composable` UI를 작성/수정하면 **반드시 같은 파일 하단에 `@Preview` 를 함께 작성한다** (Screen / Component / designsystem 공용 컴포넌트 모두 해당)
- `private @Composable fun {Name}Preview()` 형태, `ChallengeTheme { }` 래핑 + `ChallengeTheme.colorScheme.background` 배경
- 상태 분기(Loading/Data/Empty, 선택/비선택 등)가 있으면 분기별 Preview 작성
- import 는 `org.jetbrains.compose.ui.tooling.preview.Preview` (KMP) 사용
- 세부 규칙은 `design-system` 스킬의 "Preview 필수 규칙" 참조

### 함수 네이밍 규칙
- **Composable 콜백**: `on` 접두어 (`onTabSelected`, `onSearchClick`)
- **ViewModel 함수**: `on` 없음 (`selectTab`, `search`)

### 주석 규칙
- **불필요한 주석 금지**: 네이밍·타입·구조로 파악 가능한 내용은 주석으로 달지 않는다.
- 주석은 **왜(why)** 만 설명한다 — 비자명한 결정, 트레이드오프, 우회(workaround), 외부 제약 등. **무엇을(what)** 다시 옮긴 주석은 금지.
- **분량은 한 줄이 기본, 길어도 두 줄.** 왜에 해당해도 길면 금지 — 코드만 봐선 모르는 결론 하나만 남긴다.
  - 조사 경위·라이브러리 내부 동작·"알려진 한계" 서술은 주석이 아니라 대화/커밋 메시지로 전달한다
  - 같은 사실은 한 곳에만 적는다 (도메인 모델 KDoc 에 있으면 UiState·ViewModel 에 반복하지 않는다)
  - "[X] 와 같은 규칙" 처럼 다른 주석을 가리키기만 하는 주석은 달지 않는다

```kotlin
// ❌ 코드가 그대로 말하는 내용 — 주석 불필요
// 받은 요청에서 제거
list.filterNot { it.requestId == target.requestId }

// ✅ 코드만으로 모르는 이유(why) 를 설명
// 전체 스냅샷 복원 시 동시 진행 중인 다른 mutation 의 낙관 갱신을 덮어쓰므로 1행 단위로 되돌린다
```

- **KDoc**: 공개 API의 비자명한 계약/제약에만. 함수명·시그니처로 자명하면 생략.
- **모델(data class/enum) KDoc**: 한 줄 요약 + `@property`/값(enum·table) 설명은 유지. 단, spec·api-contract 경로 참조와 "백엔드가 ~ 응답한다" 식 서버 동작 설명 prose 는 적지 않는다.

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
