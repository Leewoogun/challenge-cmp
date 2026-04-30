---
description: "Feature 모듈의 Presentation Layer 구현 - UiState, Effect, ViewModel, Screen, Navigation 파일 생성 (KMP Compose)"
model: claude-opus-4-6
user-invocable: true
allowed-tools:
  - Read
  - Write
  - Edit
  - Glob
  - Grep
---

# Feature (Presentation) Layer Skill

$ARGUMENTS 를 기반으로 Feature 모듈을 구현합니다.

## 전제 조건

- **Domain/Data 존재 시**: UseCase/Repository를 주입하여 ViewModel 구현
- **Domain/Data 미존재 시**: State, Effect, Screen 골격만 생성하고 ViewModel은 TODO 주석으로 남김

## 필요 인자

`$ARGUMENTS`에서 다음 정보를 파악합니다:
- **기능명**: PascalCase (예: `Recipe`, `Search`, `Profile`)
- **모듈 경로**: `feature/{name}/`
- **UI 요구사항**: 화면 구성, 사용자 인터랙션

## 모듈 구조

### 디렉토리 구조

```
feature/{name}/
├── build.gradle.kts
└── src/commonMain/kotlin/com/lwg/cooking/feature/{name}/
    ├── {Name}Route.kt           # Route (state collect, event 전달) + Content (상태 분기)
    ├── {Name}Screen.kt          # 실제 UI + Preview
    ├── {Name}ViewModel.kt       # 상태 관리
    ├── contract/
    │   ├── {Name}State.kt       # UiState sealed interface
    │   └── {Name}Effect.kt      # UiEffect + ModalEffect
    ├── di/
    │   └── {Name}Module.kt      # Koin 모듈
    └── component/               # (필요시) 화면 전용 컴포넌트
```

### build.gradle.kts

```kotlin
plugins {
    alias(libs.plugins.cookingFeature)
}

android {
    namespace = "com.lwg.cooking.feature.{name}"
}
```

> **참고**: `cookingFeature` 플러그인이 아래 의존성을 자동 포함:
> - `:core:designsystem`, `:core:navigation`, `:core:domain`, `:core:utils`
> - Koin Compose ViewModel Navigation, Koin annotations
> - Navigation 3, Lifecycle

### settings.gradle.kts 등록

```kotlin
include(":feature:{name}")
```

---

## 구현 순서

### 1단계: Contract - State 정의

**위치**: `.../contract/{Name}State.kt`

```kotlin
package com.lwg.cooking.feature.{name}.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
sealed interface {Name}UiState {

    @Immutable
    data object Loading : {Name}UiState

    @Immutable
    data object Empty : {Name}UiState

    @Immutable
    data class Data(
        val items: List<SomeItem>,
    ) : {Name}UiState
}
```

**UI State 파싱 규칙**: 모든 UI 표시용 파생 데이터는 State의 `get()` 프로퍼티로 제공

### 2단계: Contract - Effect 정의

**위치**: `.../contract/{Name}Effect.kt`

```kotlin
package com.lwg.cooking.feature.{name}.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
sealed interface {Name}ModalEffect {
    @Immutable
    data object Hidden : {Name}ModalEffect
}

@Stable
sealed interface {Name}UiEffect {
    @Immutable
    data class ShowMessage(val message: String) : {Name}UiEffect
}
```

### 3단계: ViewModel 구현

**위치**: `.../{Name}ViewModel.kt`

> **`/viewmodel` 스킬에 위임합니다.**

Feature 스킬에서는 다음만 확인합니다:
- `@KoinViewModel class`
- ViewModel 함수에 `on` 접두어 없음
- MutableStateFlow 상태 변경에 `.update { }` 사용 (`.value =` 금지)
- Flow 수집에 `collect` 패턴 사용 (`launchIn` 금지)

### 4단계: Koin 모듈 정의

**위치**: `.../di/{Name}Module.kt`

```kotlin
package com.lwg.cooking.feature.{name}.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module
@ComponentScan("com.lwg.cooking.feature.{name}")
class {Name}Module
```

#### ⚠️ Platform-specific 의존성 (expect/actual)에 Koin annotation 금지

Feature 모듈에 platform-specific 구현(예: 카카오 SDK wrapper, 카메라/위치 등 native)을 `expect/actual class`로 두는 경우, **`actual` 클래스에 `@Single`/`@Factory` annotation을 붙이면 안 된다**.

**이유:** KSP는 타겟별로 따로 동작. commonMain `@Module @ComponentScan`은 metadata/commonMain pass에서 commonMain 컴포넌트만 등록하고, 플랫폼별 `@Single`은 별도 `defaultModule`로 분리되어 모듈 그래프에 연결되지 않는다 → 런타임 `NoDefinitionFoundException`.

**올바른 패턴:** Koin annotation 없이 `expect/actual val Module = module { ... }`로 수동 등록.

```kotlin
// commonMain/.../di/Platform{Name}Module.kt
expect val platform{Name}Module: Module

// androidMain/.../di/Platform{Name}Module.android.kt
actual val platform{Name}Module: Module = module {
    single<SomeProvider> { SomeProviderImpl(get()) }
}

// iosMain/.../di/Platform{Name}Module.ios.kt
actual val platform{Name}Module: Module = module {
    single<SomeProvider> { SomeProviderImpl() }
}
```

`composeApp/.../App.kt`의 `startKoin` modules 리스트에 `platform{Name}Module` 추가.

**적용 대상 판단:**
- ✅ commonMain의 `@KoinViewModel`, `@Single`, `@Factory` → `@ComponentScan`이 정상 picked up
- ❌ androidMain/iosMain의 `actual class`에 `@Single` → **금지, `expect/actual val Module` 사용**

### 5단계: Route + Content 구현

**위치**: `.../{Name}Route.kt`

이 파일에는 2개의 Composable이 들어갑니다:

| 함수 | 접근 제한 | 역할 |
|------|----------|------|
| `{Name}Route` | `public` | ViewModel 연결, State collect, Event 전달 |
| `{Name}Content` | `private` | State에 따른 분기 (Loading → 로딩 UI, Data → Screen 호출) |

```kotlin
package com.lwg.cooking.feature.{name}

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lwg.cooking.feature.{name}.contract.{Name}UiState
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun {Name}Route(
    viewModel: {Name}ViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    {Name}Content(
        uiState = uiState,
    )
}

@Composable
private fun {Name}Content(
    uiState: {Name}UiState,
) {
    when (uiState) {
        is {Name}UiState.Loading -> {
            // 로딩 UI
        }
        is {Name}UiState.Data -> {
            {Name}Screen(uiState = uiState)
        }
    }
}
```

### 6단계: Screen 구현

**위치**: `.../{Name}Screen.kt`

Screen은 **실제 UI**를 그리는 곳입니다. `State.Data` 타입만 파라미터로 받으며, Preview를 작성할 수 있습니다.

```kotlin
package com.lwg.cooking.feature.{name}

import androidx.compose.runtime.Composable
import com.lwg.cooking.feature.{name}.contract.{Name}UiState

@Composable
internal fun {Name}Screen(
    uiState: {Name}UiState.Data,
) {
    // 실제 UI 구현
}
```

**UI 작성 시 금지 사항**:
- ❌ Composable에서 문자열 포맷팅, 조건 판별 등 파싱 금지 → State의 `get()` 프로퍼티로 제공

---

## 함수 네이밍 규칙

```kotlin
// ✅ Composable 콜백: on 접두어
@Composable
fun MyScreen(
    onTabSelected: (Tab) -> Unit,
    onGoBack: () -> Unit,
)

// ✅ ViewModel 함수: on 없음
class MyViewModel {
    fun selectTab(tab: Tab) { }
}

// ✅ 연결
MyContent(
    onTabSelected = viewModel::selectTab,
    onGoBack = { backStack.removeLastOrNull() },
)
```

## 체크리스트

### 모듈 설정
- [ ] `settings.gradle.kts`에 새 모듈이 등록되어 있는지
- [ ] `build.gradle.kts`의 namespace가 `com.lwg.cooking.feature.{name}` 형식인지

### State / Effect
- [ ] State가 `@Stable sealed interface`인지
- [ ] 각 상태에 `@Immutable`이 있는지
- [ ] ModalEffect에 Hidden 기본 상태가 있는지

### ViewModel
- [ ] `@KoinViewModel class`인지
- [ ] ViewModel 함수에 `on` 접두어가 없는지

### Route / Content / Screen
- [ ] Route → Content → Screen 3단계 구조인지
- [ ] Route는 `{Name}Route.kt` 파일, Screen은 `{Name}Screen.kt` 별도 파일인지
- [ ] Route에서 `collectAsStateWithLifecycle`으로 상태 수집하는지
- [ ] Route에서 Koin ViewModel은 `koinViewModel()`으로 주입하는지
- [ ] Content에서 State에 따라 Loading/Data 분기하는지
- [ ] Screen은 `State.Data` 타입만 파라미터로 받는지

### Koin 모듈
- [ ] `@Module @ComponentScan` 모듈이 정의되어 있는지
