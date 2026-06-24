---
description: "Figma 링크에서 디자인을 분석하여 Compose Multiplatform UI 코드로 변환하고, 디자인 시스템 네이밍 불일치 시 리포트 생성"
model: claude-opus-4-6
user-invocable: true
allowed-tools:
  - Read
  - Write
  - Edit
  - Glob
  - Grep
  - mcp__figma-remote-mcp__get_screenshot
  - mcp__figma-remote-mcp__get_design_context
  - mcp__figma-remote-mcp__get_variable_defs
  - mcp__figma-remote-mcp__get_metadata
---

# Figma → Compose UI Skill

$ARGUMENTS 에서 Figma 링크를 받아 디자인을 분석하고, 프로젝트 디자인 시스템에 맞는 Compose Multiplatform UI 코드를 생성합니다.

## 필요 인자

`$ARGUMENTS`에서 다음 정보를 파악합니다:
- **Figma URL**: `https://figma.com/design/:fileKey/:fileName?node-id=:nodeId` 형식
- **기능명** (선택): 생성할 Screen/Component의 이름
- **대상 파일 경로** (선택): 코드를 작성할 위치

## 실행 순서

### 1단계: Figma 디자인 분석

Figma URL에서 `fileKey`와 `nodeId`를 추출합니다.

순서대로 Figma MCP 도구를 호출합니다:

1. `get_screenshot` — 시각적 구조 파악
2. `get_design_context` — UI 코드 힌트 (clientLanguages="kotlin", clientFrameworks="jetpack compose")
3. `get_variable_defs` — 디자인 토큰 확인

### 2단계: 디자인 시스템 매핑

Figma에서 가져온 디자인 토큰을 프로젝트 디자인 시스템과 매핑합니다.
`core/designsystem/` 디렉토리의 테마, 색상, 타이포그래피 파일을 읽어서 현재 사용 가능한 토큰을 확인합니다.

### 3단계: 네이밍 불일치 리포트 생성

매핑이 안 되는 항목이 있으면 `FIGMA_DESIGN_MISMATCH.md`를 생성합니다.
불일치가 없으면 리포트 파일을 생성하지 않습니다.

### 4단계: Compose UI 코드 생성

**코드 작성 규칙**:
- `commonMain`에 작성 (KMP Compose Multiplatform)
- 프로젝트 디자인 시스템의 색상/타이포 사용
- `MaterialTheme` 대신 프로젝트 커스텀 테마 사용 (존재하는 경우)
- 하드코딩 색상 금지 (새 색상 필요 시 TODO 주석 + 임시 하드코딩만 허용)

**레이아웃 변환 규칙**:
- Figma의 px 값은 dp와 동일하게 취급
- Auto Layout → `Row`, `Column`, `Box`
- Figma padding/spacing → `Modifier.padding()`, `Spacer`

### 5단계: Preview 작성 (필수)

생성한 모든 `@Composable` 마다 같은 파일 하단에 `@Preview` 를 **반드시** 추가한다.
- `private @Composable fun {Name}Preview()` 형태
- `ChallengeTheme { }` 로 래핑 + `ChallengeTheme.colorScheme.background` 배경
- 상태 분기(Loading/Data/Empty 등)가 있으면 분기별 Preview 작성
- import 는 `org.jetbrains.compose.ui.tooling.preview.Preview` (KMP)

세부 규칙은 `design-system` 스킬의 "Preview 필수 규칙" 참조.

## 체크리스트

- [ ] `commonMain`에 작성했는지
- [ ] 프로젝트 디자인 시스템 토큰을 우선 사용했는지
- [ ] 하드코딩 색상에 TODO 주석이 있는지
- [ ] 불일치 항목이 있으면 `FIGMA_DESIGN_MISMATCH.md`에 기록했는지
- [ ] 생성한 각 Composable 에 `@Preview` 를 작성했는지 (상태 분기별 포함)
