---
description: "Compose UI 코드 작성 시 자동 참조되는 디자인 시스템 규칙 (프로젝트 테마 색상, 타이포, 컴포넌트 매핑)"
user-invocable: false
allowed-tools:
  - Read
  - Glob
  - Grep
---

# 디자인 시스템 핵심 규칙 (다크 / 라이트)

UI Composable 코드를 작성할 때 이 규칙을 반드시 따른다. 앱은 다크(기본) / 라이트 테마를 지원하므로
**모든 색은 테마 토큰으로만** 지정한다 — 하드코딩하면 한쪽 테마에서 깨진다.

## 색상 규칙

색에 접근하는 정식 경로는 **세 갈래**다. 다른 길은 없다.

| 종류 | 접근 경로 |
|---|---|
| 시멘틱 색상 (모든 색상) | `ChallengeTheme.colorScheme.<group>.<name>` |
| 그라데이션 | `ChallengeTheme.brushes.<name>` |
| 외부 브랜드 (테마 영향 X) | `BrandColors.<name>` |

> Material3 `MaterialTheme.colorScheme.X` 는 **직접 호출 금지**.
> Material3 컴포넌트 호환을 위해 내부적으로 동일 색상이 매핑돼 있을 뿐, Feature 코드는 항상 `ChallengeTheme.colorScheme` 를 사용한다.
> 현재 테마가 다크인지는 `ChallengeTheme.isDarkTheme` 로 알 수 있다 (색 분기에는 쓰지 말고 토큰을 쓴다).

### 토큰 구조 (`core/designsystem/theme/color/`)

| 그룹 | 토큰 | 용도 |
|---|---|---|
| `background` | `screen` | 화면 전체 배경 |
| | `card` | 카드 · 상단바 · 시트 · 상태바 |
| | `cardSub` | 카드 안의 보조 영역 |
| | `field` | 입력창 · 아이콘 박스 · 칩 · 서명란 등 카드 위에 채워진 요소 |
| `text` | `primary` / `secondary` | 본문 / 보조 · 캡션 |
| | `point` | 브랜드 오렌지 강조 글씨 |
| | `onPoint` | 오렌지 배경 위 글씨 (라벨 등) |
| `icon` | `primary` / `secondary` / `point` / `onPoint` | 아이콘 tint (기준은 `text` 와 동일) |
| `button` | `background` / `text` | 메인 버튼 배경(오렌지) / 글씨 |
| | `subBackground` / `subText` | 보조 버튼 배경 / 글씨 |
| | `border` | 외곽선 버튼 테두리 |
| `line` | `border` | 카드 · 박스 테두리 |
| | `divider` / `dividerSub` | 구분선 / 옅은 구분선 |
| `inputField` | `background` / `text` / `hint` / `cursor` / `focusedBorder` | 입력창 |
| (base) | `point` | 브랜드 오렌지 — 진행바 · 강조 배경 등 텍스트/아이콘/버튼이 아닌 자리 |
| (base) | `error` / `success` / `warning` | 상태 색 (텍스트 · 아이콘 · 배경 공용) |

**고르는 순서**: 무엇을 칠하는지(텍스트? 아이콘? 버튼? 선? 배경?)로 그룹을 먼저 정하고, 그 안에서 용도를 고른다.
`Text(color = …)` 에는 `text.*`, `Icon(tint = …)` 에는 `icon.*` 를 쓴다 — 라이트 테마에서는
`text.point` 가 `point` 보다 진하게 잡혀 있어 섞어 쓰면 대비가 달라진다.

### 그라데이션 / 외부 브랜드

- 메인 그라데이션: `ChallengeTheme.brushes.fire` (`gradient.fireStart` → `fireEnd`, 135deg)
- 카드 depth: `ChallengeTheme.brushes.card` (`gradient.cardStart` → `cardEnd`)
- 후광: `ChallengeTheme.brushes.glow` (radial)
- 카카오 브랜드: `BrandColors.KakaoYellow`, `BrandColors.KakaoLabel` (테마 영향 받지 않음)

```kotlin
// 올바른 사용
Text(
    text = "...",
    color = ChallengeTheme.colorScheme.text.primary,
)
Icon(imageVector = icon, contentDescription = null, tint = ChallengeTheme.colorScheme.icon.secondary)
Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background.card))
HorizontalDivider(color = ChallengeTheme.colorScheme.line.divider)
Box(modifier = Modifier.background(ChallengeTheme.brushes.fire))
NormalButton(
    containerColor = BrandColors.KakaoYellow,
    contentColor = BrandColors.KakaoLabel,
)
Text(
    text = "성공!",
    color = ChallengeTheme.colorScheme.success,
)

// 절대 금지
Color(0xFF000000)                       // 하드코딩 금지
MaterialTheme.colorScheme.primary       // Material3 직접 호출 금지
```

**새 색상이 필요한 경우만 임시 하드코딩 허용**:
```kotlin
// TODO: 디자인 시스템에 추가 요청 - [용도]
Color(0xFFXXXXXX)
```
이후 다음 순서로 추가한다:
1. `core/designsystem/theme/Color.kt` 에 raw 토큰 (예: `internal val orange3 = Color(0xFFXXXXXX)`)
2. 알맞은 그룹 클래스(`theme/color/XxxColor.kt`)에 필드 추가 — 없으면 base 필드로
3. `ChallengeColorScheme.kt` 의 **`DarkChallengeColorScheme` · `LightChallengeColorScheme` 둘 다**에 값 지정 (빠뜨리면 컴파일 에러)

외부 브랜드 색이면 `BrandColors.kt` 에 추가.

## 타이포그래피 규칙

`ChallengeTheme.typography.<style>` 사용. 직접 `fontSize`/`fontWeight` 조합 금지.

```kotlin
// 금지
fontSize = 20.sp, fontWeight = FontWeight.Bold

// 허용
style = ChallengeTheme.typography.bold20
```

사용 가능한 스타일은 `core/designsystem/theme/Typography.kt` 참조.

## 컴포넌트 규칙

`core/designsystem/components/` 의 공용 컴포넌트를 우선 사용:
- `NormalButton` — 기본 버튼
- `ChallengeLabel` — 라벨 (solid/gradient 두 종류)
- `ChallengeScaffold` — StatusBar 통합 Scaffold

커스텀 UI가 필요한 경우에만 직접 구현.

## UI 작성 규칙

1. **KMP Compose**: `commonMain`에 작성 (플랫폼 독립)
2. **클릭 영역**: `clip(RoundedCornerShape(N.dp))` 처리
3. **테마**: 단일 다크 톤. 라이트 모드 분기 금지.
4. **Preview 필수**: 새 `@Composable` UI를 작성하면 **반드시 같은 파일 하단에 `@Preview` 를 함께 작성한다** (아래 규칙 참조).

## Preview 필수 규칙 (모든 UI 코드 공통)

`@Composable` UI 함수를 새로 만들거나 수정할 때, **Preview 작성을 생략하지 않는다.** Screen / Component / designsystem 공용 컴포넌트 어디든 동일하게 적용된다.

1. **1 Composable = 1 Preview (최소)**: 새로 만든 표시용 `@Composable` 마다 같은 파일 하단에 `private @Composable fun {Name}Preview()` 를 추가한다.
2. **상태 분기는 분기별 Preview**: 인자에 따라 시각이 달라지면(`isLoading=true/false`, 선택/비선택, Empty/Data 등) 분기마다 별도 Preview 를 만든다.
3. **테마 래핑 필수**: Preview 는 반드시 `ChallengeTheme { }` 로 감싸고(`darkTheme` 인자 없이), 배경 가시성을 위해 `ChallengeTheme.colorScheme.background.screen` 배경을 깐다.
4. **어노테이션은 `@ThemePreviews`**: `import com.lwg.challenge.designsystem.preview.ThemePreviews` 하나로 라이트 / 다크 Preview 가 함께 그려진다 (`ChallengeTheme` 기본값이 Preview 의 `uiMode` 를 따른다). `@Preview` 를 직접 쓰지 않는다 — `org.jetbrains.compose.ui.tooling.preview.Preview` 는 deprecated 라 의존성에서 빠졌다.

```kotlin
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme

@ThemePreviews
@Composable
private fun {Name}Preview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            {Name}(/* 샘플 인자 */)
        }
    }
}
```

## 코드 작성 전 확인

`core/designsystem/theme/`를 먼저 탐색:
- `Color.kt` — raw 토큰 (orange1, black2, gray3 ... 직접 import 금지)
- `ChallengeColorScheme.kt` — 시멘틱 색상 데이터 클래스 + Dark / Light 인스턴스
- `color/` — 색상 그룹 클래스 (Background / Text / Icon / Button / Line / InputField / Gradient)
- `ChallengeBrushes.kt` — 그라데이션
- `BrandColors.kt` — 외부 브랜드 (Kakao)
- `Theme.kt` — `ChallengeTheme` 진입점, `ChallengeTheme.colorScheme` getter
- `Typography.kt` — 타이포 스타일
- `components/` — 기존 공용 컴포넌트
