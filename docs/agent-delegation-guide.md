# 모바일(challenge-app) 작업 위임 가이드 — 오케스트레이터/PM 에이전트용

> 이 문서는 **다른 프로젝트에서 도는 에이전트(PM/오케스트레이터)가 challenge-app(모바일)을 수정할 때** 반드시 따라야 하는 절차다.
> 핵심 한 줄: **모바일 코드 편집은 이 repo 를 작업 디렉터리(cwd)로 "뿌리내린" 세션에서만 수행한다.**

---

## 1. 문제 — 왜 컨벤션이 안 지켜지나

PM 에이전트가 자기 프로젝트에서 돌면서 이 repo 파일을 **절대경로로 reach-in 편집**하면, 이 repo 의 컨벤션 강제 장치가 **하나도 로드되지 않는다.** 에이전트가 규칙을 어기는 게 아니라, 규칙을 **본 적이 없는** 상태로 코드를 짠다.

| 컨벤션 장치 | 로드 조건 |
|---|---|
| `CLAUDE.md` (스킬 매핑·아키텍처·컨벤션) | 세션 cwd + 상위 디렉터리에서만 로드 |
| `.claude/skills/*` (`design-system`, `/feature`, `/viewmodel`, …) | 그 프로젝트를 cwd 로 세션이 시작될 때만 발견됨 |
| `.claude/agents/*` (`qa-integration`, `review-viewmodel`) | 〃 |
| `.claude/settings.json` 의 SessionStart 훅 (= "반드시 스킬 먼저 호출" 강제) | 〃 |
| 자동 메모리 (`feedback_*` 컨벤션 누적분) | 프로젝트 경로에 묶임 |

> `--add-dir` 로 경로를 추가해도 **파일 접근만** 열릴 뿐, 그 repo 의 skills/hooks/SessionStart/메모리는 따라오지 않는다.

---

## 2. 서브에이전트 위임만으로는 부족한 이유

Task/Agent 도구로 띄운 서브에이전트는 **부모 세션의 cwd·프로젝트 컨텍스트를 그대로 공유**한다.
→ 부모가 다른 repo 에 뿌리내려 있으면, 서브에이전트도 이 repo 의 `CLAUDE.md`/skills/hooks/memory 를 **못 본다.**
→ 위임의 형태(서브에이전트)가 아니라 **cwd 자체가 이 repo 여야** 컨벤션이 로드된다.

---

## 3. 올바른 위임 방법

모바일 작업은 아래 둘 중 하나로 **이 repo 를 cwd 로** 실행한다.

**방법 A (권장) — 이 repo 를 cwd 로 하는 별도 Claude Code 세션에 위임**

```bash
cd /Users/hwamulman/woogunProject/challenge/challenge-app
claude -p "<모바일 작업 지시>"
```

이렇게 하면:
- SessionStart 훅이 발화 → "요청마다 매칭 스킬 먼저 호출" 규칙이 컨텍스트에 주입됨
- `CLAUDE.md` 의 스킬 매핑 테이블 + 아키텍처/컨벤션 로드
- `.claude/skills/*`, `.claude/agents/*` 발견 가능
- 누적된 자동 메모리(`feedback_*`) 로드

**방법 B — IDE/워크스페이스를 이 repo 로 열고 그 안에서 작업**

---

## 4. 위임 전/후 체크리스트

- [ ] 세션 cwd 가 `challenge-app` 루트인가 (`pwd` 확인)
- [ ] 첫 응답에서 스킬 매핑이 인식되는가 (예: 화면 작업 → `/feature`, UI → `design-system` 자동 적용 언급)
- [ ] 작업 후 빌드 검증: `./gradlew :{module}:compileCommonMainKotlinMetadata`
- [ ] 핵심 컨벤션 준수 확인:
  - Clean Architecture 의존 방향 (Feature/Data → Domain, Domain 역참조 금지)
  - StateFlow `.update { }` (`.value =` 금지)
  - Repository 반환 패턴: 값 → `Flow<T>` + `onError: (String) -> Unit`, 데이터 없는 성공/실패 → `onSuccess/onError` 콜백 (stdlib `Result` 금지)
  - UI: `ChallengeTheme.colorScheme/typography` 만 사용 (`MaterialTheme.*` 직접 호출 금지), 새 `@Composable` 엔 `@Preview` 동반
  - ViewModel 함수명 `on` 접두어 금지 / Composable 콜백은 `on` 접두어

---

## 5. PM(오케스트레이터) 프로젝트 쪽에 넣을 규칙 스니펫

> 강제는 **PM 에이전트가 사는 프로젝트**의 `CLAUDE.md`/시스템 프롬프트에 박아야 효력이 있다 (이 repo 의 문서는 PM 세션에 자동 로드되지 않으므로).

```md
## 모바일(challenge-app) 작업 규칙
- challenge-app 코드는 **절대경로 reach-in 편집 금지.**
- 반드시 `cd /Users/hwamulman/woogunProject/challenge/challenge-app` 후
  그 repo 를 cwd 로 하는 세션(`claude -p ...`)에 위임한다.
- 이유: 컨벤션(CLAUDE.md / .claude skills·agents·hooks / 자동 메모리)은
  **cwd 기준으로만 로드**되며, 밖에서 들어오는 세션엔 보이지 않는다.
- 위임 절차/체크리스트: challenge-app `docs/agent-delegation-guide.md` 참조.
```

---

## 6. 더 단단하게 만들려면 (선택 — 추후)

문서/스킬/메모리는 "권고"이고 cwd 에 뿌리내린 세션에서만 보인다. 누가 편집하든(원격 에이전트·사람) 무조건 걸리게 하려면 **산출물(diff/repo) 단에서 강제**해야 한다. 이 repo 엔 이미 detekt 인프라(`build-logic/.../DetektPlugin.kt`, twitter-compose 룰, pre-commit staged-only 로직)가 있으나 **차단 게이트로 연결돼 있지 않다**:

- git **pre-commit 훅** 설치 + **CI** 에서 `./gradlew detekt` (+ compile/test)
- 메모리 컨벤션을 **detekt 커스텀 룰**로 코드화 (`MaterialTheme.colorScheme` 금지, `MutableStateFlow.value =` 금지, ViewModel 네이밍, Repository 반환 패턴 등)

> 이 6장은 이번 작업 범위 밖. 필요해지면 별도로 구축.
