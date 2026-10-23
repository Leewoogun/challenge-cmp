---
description: "기존 feature 모듈을 프로젝트 컨벤션에 맞게 리팩토링 - 점검(감사) 리포트 → 사용자 결정 → 레이어별 수정 → 빌드·테스트 검증. 동작은 유지한다. 'XX 모듈 정리해줘', 'XX 리팩토링', '컨벤션에 맞게 고쳐줘', 'State/ViewModel 컨벤션 확인해봐', '주석 정리', 'screen 분리' 처럼 이미 있는 화면·모듈을 정리하는 요청이면 기능 추가가 아니어도 이 스킬을 쓴다."
user-invocable: true
allowed-tools:
  - Agent
  - Read
  - Write
  - Edit
  - Glob
  - Grep
  - Bash
---

# Feature 리팩토링 Skill

$ARGUMENTS 모듈을 아래 기준으로 정리한다. 목표는 **동작은 그대로, 모양은 다른 모듈과 같게**다.
challenge(detail · oath · verify), friends 리팩토링에서 사용자가 반복해서 요구·교정한 내용을 정리한 것이다.

## 진행 방식

1. **점검 먼저, 수정은 나중.** 모듈 전체(Route / ViewModel / contract / screen / component / test, 그리고 쓰는 Repository·UseCase)를 읽고 아래 체크리스트로 위반을 찾는다.
2. **리포트로 보고하고 결정을 받는다.** 항목마다 "현재 코드 → 어떤 규칙 위반인지(CLAUDE.md·메모리 근거) → 고치면 어떻게 되는지 → 영향 범위"를 쓴다. 동작이 바뀌는 항목(예: Error 변형 제거, 재조회 시점 변경)은 따로 표시하고 사용자에게 고르게 한다. 사용자가 "진행해줘"라고 하면 그 범위만 고친다.
3. **레이어 순서대로 고친다.** Domain/Repository → DataSource/Impl → UseCase(+Koin 등록) → State → ViewModel → Route/Screen/Component → Preview → Test.
4. **검증한다.** 아래 "검증" 명령을 돌리고, 테스트 개수와 통과 여부를 그대로 보고한다.
5. **결과를 표로 요약한다.** "전 → 후", 남긴 것과 그 이유, 아직 커밋 안 했다는 사실.

계획과 다른 사실을 발견하면(예: 옮기려던 문구가 화면 어디에서도 안 쓰임) 조용히 바꾸지 말고 "계획과 달라진 부분"으로 따로 보고한다.

## 체크리스트

### 1. Data / Domain
- 값을 돌려주는 Repository·DataSource 는 `fun foo(..., onError: (String) -> Unit): Flow<T>`. `onSuccess: (T) -> Unit` 로 데이터를 넘기면 위반이고, ViewModel 에 `var loaded` 같은 임시 변수가 생기는 원인이다.
- 여러 Repository 를 합치는 건 UseCase(`combine`). 실패해도 화면이 성립하는 보조 데이터는 UseCase 에서 `onError = {}` + `onEmpty { emit(null) }` 로 채운다.
- UseCase 전용 합성 모델(`XxxData`)은 `:domain:model` 이 아니라 **그 UseCase 파일 하단**에 둔다.
- 시그니처를 바꾸면 모든 구현체·Fake(다른 feature 의 테스트 Fake 포함)를 함께 고친다. `grep -rl ": XxxRepository"` 로 찾는다.

### 2. UiState (`contract/`)
- **도메인 모델을 그대로 담는다.** `Data(detail: ChallengeDetail, verifications: ChallengeVerifications?, ...)`.
- 모델을 감싸 getter 만 위임하는 ItemState / Section / 복사 필드는 지운다(`FriendItemState`, `VerificationSection`, `challengerNickname = opponent.nickname` 같은 것).
- **단순 위임 `get()` 도 두지 않는다.** Screen 이 `uiState.detail.opponent.nickname` 처럼 모델을 직접 읽는다.
- `Data` 에 남기는 것은 계산·판정·문자열 조합뿐이다: `canVerify`, `hasFriends`, `forfeitNoticeText`, `"${...}의 미션"`, null → false 판정 등.
- 아이템 1개 + 모델 밖 값(`now`)이 필요하면 `Data` 의 `fun xxxOf(item)`. 시각은 `now` 를 `Data` 생성자에 담고 `get()` 에서 계산한다.
- Composable 이 하던 조건(`isNotEmpty()`, `== VERIFIED`)은 `Data` 의 `get()` 으로 옮긴다. Route 의 판정도 포함.
- 이름은 도메인 이름을 따른다(`challenger*` → `opponent*`).
- 쓰지 않는 필드·문구·ModalEffect 는 지운다. 단, 지운다고 리포트에 적는다.
- `Error` / `Empty` 변형은 메모리 규칙(Loading + Data)과 다르지만 사용자가 유지를 고른 적이 있다 → 지우지 말고 물어본다.

### 3. ViewModel
- **서버 데이터 화면**은 파이프라인으로 만든다.
  ```kotlin
  val uiState: TriggerStateFlow<XxxUiState> = xxxFlow().triggerStateIn(viewModelScope, started, Loading)

  private fun xxxFlow(): Flow<XxxUiState> = channelFlow {
      uiState.withData<XxxUiState.Error> { send(XxxUiState.Loading) }   // Error 에서 재시도할 때만 스피너
      useCase(id, onError = { trySend(XxxUiState.Error(it)) })
          .collectLatest { data ->
              combine(localFlowA, localFlowB) { a, b -> XxxUiState.Data(data.detail, ..., a, b) }.collect(::send)
          }
  }
  ```
  - `onError` 콜백에서 상태를 내보내거나 `collectLatest` 안에서 보내야 하면 `flow {}` 가 아니라 `channelFlow {}` (`flow {}` 는 다른 코루틴에서 emit 하면 invariant 위반).
  - 사용자 입력·진행 중 플래그(`_myMission`, `_isForfeiting`)는 파이프라인 **밖** `MutableStateFlow` 로 두고 `combine` 한다. 재조회해도 남는다.
  - 재조회는 `uiState.restart()`. mutation 후에는 `onSuccess = uiState::restart`.
- **`started` 고르기**
  - 다른 화면에서 돌아왔을 때 갱신이 필요하면 `WhileSubscribed(0)` (예: 인증 후 상세 복귀).
  - 한 번 조회 후 retry 로만 재조회하는 폼 화면은 `Lazily`.
  - upstream 이 `uiState` 를 읽으면 `Eagerly` 금지 — `Main.immediate` 에서 생성 중에 수집이 돌아 미대입 `uiState` 로 NPE.
- **서버 데이터가 없는 단계형 화면**(Capture → Confirm → Done)은 `MutableStateFlow + update` 그대로 둔다. 억지로 파이프라인화하지 않는다.
- 타입 분기는 FlowUtil(`withData`, `updateWithData`). `value is X`, `as? X` 금지.
- Effect 발행은 `private fun showMessage(message)`. 범용 `emitEffect`, 테스트용 `internal` 노출 금지.
- 시계는 ViewModel 에서만 읽는다. 주기 갱신은 `flow { emit; while(...) { delay; emit } }.distinctUntilChanged()` 티커.

### 4. 파일 구조
- UiState 분기 화면은 `screen/` 에 **1파일 = 1화면**: `{Name}Screen`(Data), `{Name}LoadingScreen`, `{Name}ErrorScreen`, `{Name}EmptyScreen` ... 각 파일엔 자기 화면 Preview 만.
- 다이얼로그·하위 UI 는 `component/`.
- 이동은 `git mv` 로 해서 이력을 남긴다. 패키지 선언, Route import, 같은 패키지라 import 없이 쓰던 컴포넌트 import 를 같이 고친다.

### 5. Screen / Preview
- Screen 에서 같은 체인이 반복되면 함수 맨 위 지역 변수로 한 번만 꺼낸다(`val myVerification = uiState.verifications?.myVerification`).
- Preview 픽스처는 도메인 모델로 만든다(`ChallengeDetail(...)`, `Contract(...)`). 실제 데이터 조합으로 장면을 재현한다(예: "서명 못 읽음" = `signedAt` 있음 + `signature` null).
- 라이브러리 슬롯을 흉내 내는 Preview 는 실제 제약을 맞춘다(예: Coil 슬롯 = `propagateMinConstraints = true`).

### 6. 주석 (CLAUDE.md 주석 규칙)
- 한 줄 기본, 길어도 두 줄. 왜(why)만, 결론만.
- 지운다: spec·design 경로(`docs/...`, `design.md §`), 작업 경위("이번 변경의 핵심", "다이얼로그 시절"), 다른 주석을 가리키기만 하는 문장("[X] 와 같은 규칙"), 도메인 KDoc 에 이미 있는 내용의 반복, 코드가 말하는 내용.
- 남긴다: 비자명한 결정 한 줄(예: "Eagerly 면 생성 중에 수집이 돌아 미대입 uiState 를 읽는다").

### 7. 테스트
- 기존 테스트의 **검증 내용은 유지**하고 접근 경로만 바꾼다(`state.myMission` → `state.detail.me.mission`).
- `stateIn` 계열로 바꾸면 테스트가 구독해야 값이 생긴다: `backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect { } }`.
- 지킨 동작에 테스트가 없으면 하나 추가하고, 그 동작을 일부러 깨서 그 테스트만 실패하는지 확인한 뒤 되돌린다.
- 테스트 이름에 `,` `(` `)` 를 쓰지 않는다(Kotlin/Native iOS 컴파일 실패).

## 작업 시 주의
- zsh 는 따옴표 없는 변수를 단어로 쪼개지 않는다. 파일 목록은 `xargs` 로 넘긴다.
- zsh 에서 `$M:testDebugUnitTest` 는 `:t` 가 수정자로 먹혀 태스크명이 깨진다. Gradle 태스크 경로는 풀어 쓰거나 `${M}:...` 로 쓴다.
- 정규식 일괄 치환은 패키지 경로까지 바꿀 수 있다(`.contract` → `.detail.contract` 가 import 를 깨뜨린 적 있음). 치환 후 `grep` 으로 import 줄을 확인한다.
- Edit 도구는 Read 로 읽은 파일만 고칠 수 있다. 사용자가 IDE 에서 동시에 고치는 중일 수 있으니 쓰기 전에 현재 내용을 다시 읽는다.
- import 순서는 알파벳 순(IntelliJ 기본: `kotlin.*`/`kotlinx.*` 는 뒤)으로 맞춘다.

## 검증

```bash
# 변경 모듈 단위 테스트 + iOS 컴파일(main/test) + 앱 전체 Android 컴파일
./gradlew :{module}:testDebugUnitTest \
  :{module}:compileKotlinIosSimulatorArm64 :{module}:compileTestKotlinIosSimulatorArm64 \
  :composeApp:compileDebugKotlinAndroid --console=plain

# 테스트 개수 확인
cat {module}/build/test-results/testDebugUnitTest/*.xml | grep -o 'tests="[0-9]*"\|failures="[0-9]*"'
```

Repository·UseCase 시그니처를 바꿨다면 그 인터페이스를 쓰는 다른 모듈(`:remote:datasource`, `:remote:mapper`, 다른 feature 의 Fake)도 테스트를 돌린다.
