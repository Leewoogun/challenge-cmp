---
description: "Compose UI 코드 작성 시 자동 참조되는 디자인 시스템 규칙 (프로젝트 테마 색상, 타이포, 컴포넌트 매핑)"
user-invocable: false
allowed-tools:
  - Read
  - Glob
  - Grep
---

# 디자인 시스템 핵심 규칙 (단일 다크 톤)

UI Composable 코드를 작성할 때 이 규칙을 반드시 따른다. 라이트 모드는 지원하지 않는다.

## 색상 규칙

색에 접근하는 정식 경로는 **세 갈래**다. 다른 길은 없다.

| 종류 | 접근 경로 |
|---|---|
| 시멘틱 색상 (모든 색상) | `ChallengeTheme.colorScheme.<name>` |
| 그라데이션 | `ChallengeTheme.brushes.<name>` |
| 외부 브랜드 (테마 영향 X) | `BrandColors.<name>` |

> Material3 `MaterialTheme.colorScheme.X` 는 **직접 호출 금지**.
> Material3 컴포넌트 호환을 위해 내부적으로 동일 색상이 매핑돼 있을 뿐, Feature 코드는 항상 `ChallengeTheme.colorScheme` 를 사용한다.

### 의미 → 슬롯 매핑 가이드

| 의미 | 슬롯 |
|---|---|
| 메인 CTA / 강조 배경 | `colorScheme.primary` 또는 `brushes.fire` |
| CTA 위 텍스트/아이콘 | `colorScheme.onPrimary` |
| 화면 배경 | `colorScheme.background` |
| 메인 텍스트 | `colorScheme.onBackground` / `onSurface` |
| 카드 / 섹션 배경 | `colorScheme.surface` 또는 `surfaceVariant` |
| 보조 텍스트 / 캡션 | `colorScheme.onSurfaceVariant` |
| 1px 구분선 | `colorScheme.outline` 또는 `colorScheme.border` |
| 에러 텍스트/아이콘 | `colorScheme.error` |
| 성공 (챌린지 완료 등) | `colorScheme.success` / `onSuccess` |
| 경고 (데드라인 임박 등) | `colorScheme.warning` / `onWarning` |
| 차트 1~5 (랭킹/통계) | `colorScheme.chart1` ~ `chart5` |

### 그라데이션 / 외부 브랜드

- 메인 그라데이션: `ChallengeTheme.brushes.fire` (gradientPrimaryStart → gradientPrimaryEnd, 135deg)
- 카드 depth: `ChallengeTheme.brushes.card` (gradientCardStart → gradientCardEnd)
- 후광: `ChallengeTheme.brushes.glow` (radial)
- 카카오 브랜드: `BrandColors.KakaoYellow`, `BrandColors.KakaoLabel` (테마 영향 받지 않음)

```kotlin
// 올바른 사용
Text(
    text = "...",
    color = ChallengeTheme.colorScheme.onBackground,
)
Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background))
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
이후 다음 두 곳에 추가한다:
1. `core/designsystem/theme/Color.kt` 에 raw 토큰 (예: `internal val orange3 = Color(0xFFXXXXXX)`)
2. `core/designsystem/theme/ChallengeColorScheme.kt` 의 `ChallengeColorScheme` 데이터 클래스 + `DefaultChallengeColorScheme` 인스턴스에 시멘틱 슬롯 추가

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
3. **테마 래핑 필수**: Preview 는 반드시 `ChallengeTheme { }` 로 감싸고, 다크 톤 가시성을 위해 `ChallengeTheme.colorScheme.background` 배경을 깐다.
4. **import**: `org.jetbrains.compose.ui.tooling.preview.Preview` (KMP) 사용. `androidx.compose.ui.tooling.preview.Preview` 아님.

```kotlin
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun {Name}Preview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
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
- `ChallengeColorScheme.kt` — 시멘틱 색상 데이터 클래스 + Default 인스턴스
- `ChallengeBrushes.kt` — 그라데이션
- `BrandColors.kt` — 외부 브랜드 (Kakao)
- `Theme.kt` — `ChallengeTheme` 진입점, `ChallengeTheme.colorScheme` getter
- `Typography.kt` — 타이포 스타일
- `components/` — 기존 공용 컴포넌트
