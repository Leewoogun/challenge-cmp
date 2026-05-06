---
description: "ViewModel 구현 - StateFlow 파이프라인, 상태 관리, 페이지네이션 패턴 (KMP Koin ViewModel)"
model: claude-opus-4-6
user-invocable: true
allowed-tools:
  - Read
  - Write
  - Edit
  - Glob
  - Grep
---

# ViewModel Skill

$ARGUMENTS 를 기반으로 ViewModel을 구현합니다.

## 필요 인자

`$ARGUMENTS`에서 다음 정보를 파악합니다:
- **기능명**: PascalCase (예: `Recipe`, `Search`, `Profile`)
- **데이터 소스**: 사용할 Repository/UseCase
- **상태 구조**: UiState에 포함할 데이터
- **사용자 인터랙션**: 필터링, 페이지네이션, 새로고침 등

## ViewModel 기본 구조

**위치**: `feature/{name}/src/commonMain/kotlin/com/lwg/cooking/feature/{name}/{Name}ViewModel.kt`

```kotlin
@KoinViewModel
class {Name}ViewModel(
    private val someRepository: SomeRepository,
) : ViewModel() {

    // === State (파생 Flow) ===
    val uiState: StateFlow<{Name}UiState> = ...

    // === Modal Effect (StateFlow - 다이얼로그 상태) ===
    private val _modalEffect = MutableStateFlow<{Name}ModalEffect>({Name}ModalEffect.Hidden)
    val modalEffect: StateFlow<{Name}ModalEffect> get() = _modalEffect

    // === UI Effect (SharedFlow - 일회성 이벤트) ===
    private val _uiEffect = MutableSharedFlow<{Name}UiEffect>()
    val uiEffect: SharedFlow<{Name}UiEffect> get() = _uiEffect

    // === 함수 (on 접두어 없음) ===
    fun selectTab(tab: Tab) { }
    fun dismiss() {
        _modalEffect.update { {Name}ModalEffect.Hidden }
    }

    private fun showMessage(message: String) {
        viewModelScope.launch {
            _uiEffect.emit({Name}UiEffect.ShowMessage(message))
        }
    }
}
```

---

## 파생 Flow 파이프라인 패턴

### 패턴 A: Filter → flatMapLatest → combine → stateIn

**가장 흔한 패턴**. 필터 조건에 따라 여러 데이터 소스를 조합하는 목록 화면.

```kotlin
private val _filterState = MutableStateFlow(FilterState())
val filterState: StateFlow<FilterState> get() = _filterState

val uiState: StateFlow<ListUiState> = _filterState
    .flatMapLatest { filter ->
        combine(
            repository.getList(
                category = filter.category,
                onError = ::showMessage,
            ),
            bookmarkRepository.getBookmarkIds(),
        ) { list, bookmarkIds ->
            ListUiState.Data(items = list, bookmarkIds = bookmarkIds)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ListUiState.Loading,
    )
```

### 패턴 B: combine → stateIn

**상세 화면 패턴**. 여러 데이터 소스를 단순 조합.

```kotlin
val uiState: StateFlow<DetailUiState> = combine(
    repository.getDetail(id, ::showMessage),
    relatedRepository.getRelated(id, ::showMessage),
) { detail, related ->
    DetailUiState.Data(detail = detail, related = related)
}.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = DetailUiState.Loading,
)
```

### 패턴 C: repository → map → stateIn

**단순 목록 패턴**. 단일 데이터 소스 변환.

```kotlin
val uiState: StateFlow<ListUiState> = repository.getItems(::showMessage)
    .map { items ->
        if (items.isEmpty()) ListUiState.Empty
        else ListUiState.Data(items = items)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ListUiState.Loading,
    )
```

### 패턴 D: MutableStateFlow + onStart → stateIn

**수동 상태 관리 패턴**. 복잡한 초기화 + MutableStateFlow 직접 업데이트.

```kotlin
private val _uiState = MutableStateFlow<SomeState>(SomeState.Loading)
val uiState: StateFlow<SomeState> = _uiState.onStart {
    initState()
}.stateIn(
    scope = viewModelScope,
    started = SharingStarted.Lazily,
    initialValue = SomeState.Loading,
)

private fun initState() {
    viewModelScope.launch {
        val data = repository.getData(::showMessage)
        _uiState.update { SomeState.Data(data) }
    }
}
```

> **규칙**: MutableStateFlow + StateFlow 패턴에서 데이터 로드/초기화는 **반드시 `onStart`**를 사용. `init {}` 블록에서 직접 상태를 로드하지 않음.

---

## WhileSubscribed 재구독 패턴

| 전략 | timeout | 사용 시점 |
|------|---------|----------|
| `WhileSubscribed()` | 0ms | 필터/페이지네이션 목록 화면 |
| `WhileSubscribed(5000)` | 5초 | **기본값**. 대부분의 화면 |
| `Lazily` | - | 비용이 큰 초기화, 영구 유지 |

---

## 상태 관리 규칙

### sealed interface UiState 타입 체크: FlowUtil 사용 (필수)

StateFlow의 값을 타입으로 분기할 때는 `core/utils`의 FlowUtil 확장 함수를 사용한다. 직접 `value is Type` 비교 금지.

```kotlin
import com.lwg.challenge.utils.flow.withData
import com.lwg.challenge.utils.flow.updateWithData

// ✅ withData — 특정 타입일 때만 로직 실행 (inline이므로 non-local return 가능)
fun login() {
    _uiState.withData<LoginUiState.Loading> { return }
    // ... Loading이 아닐 때만 실행
}

// ✅ withData — 타입 캐스팅 후 데이터 접근
fun showMessage() {
    uiState.withData<UiState.Data> { data ->
        showToast(data.message)
    }
}

// ✅ updateWithData — 특정 타입일 때만 상태 변형 (다른 타입이면 무시)
fun updateMessage() {
    _uiState.updateWithData<UiState, UiState.Data> { data ->
        data.copy(message = "Hello")
    }
}

// ❌ 직접 타입 비교 금지
if (_uiState.value is LoginUiState.Loading) return
val data = _uiState.value as? UiState.Data ?: return
```

### MutableStateFlow 상태 변경: .update { }

```kotlin
// ✅ update 사용 (thread-safe)
_modalEffect.update { MyModalEffect.Hidden }
_filterState.update { it.copy(offset = 0) }

// ❌ .value = 금지 (race condition 위험)
_modalEffect.value = MyModalEffect.Hidden
```

### Flow 수집: collect 패턴

```kotlin
// ✅ collect
viewModelScope.launch {
    someFlow.collect { value -> handleValue(value) }
}

// ❌ launchIn 금지
someFlow.onEach { handleValue(it) }.launchIn(viewModelScope)
```

### 함수 네이밍: on 접두어 없음

```kotlin
// ✅ ViewModel 함수
fun selectTab(tab: Tab) { }
fun search(query: String) { }

// ❌ on 접두어 금지
fun onTabSelected(tab: Tab) { }
```

---

## 패턴 선택 가이드

```
1. 데이터 소스가 1개인가? → 패턴 C
2. 데이터 소스가 2개 이상, 필터/검색 없음? → 패턴 B
3. 필터/검색 조건이 있는가? → 패턴 A
4. 복잡한 초기화 + 직접 업데이트? → 패턴 D
```

---

## 체크리스트

- [ ] `@KoinViewModel class`인지
- [ ] ViewModel 함수에 `on` 접두어가 없는지
- [ ] 적합한 Flow 패턴을 선택했는지 (A/B/C/D)
- [ ] `SharingStarted` 전략이 적합한지
- [ ] sealed UiState 타입 체크에 `withData` / `updateWithData` 사용하는지 (`value is Type` 직접 비교 금지)
- [ ] MutableStateFlow 상태 변경에 `.update { }` 사용하는지
- [ ] Flow 수집에 `collect` 패턴 사용하는지
- [ ] `commonMain`에 작성했는지
