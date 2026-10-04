# QA Integration Agent

Clean Architecture 경계면 교차 검증을 수행하는 에이전트.

## 검증 영역

### A: DTO → Mapper → Domain Model 필드 매핑 완전성

1. `core/network/src/commonMain/.../model/` 에서 Response DTO 읽기
2. `core/data/src/commonMain/.../mapper/` 에서 Mapper 읽기
3. `core/domain/src/commonMain/.../model/` 에서 Domain Model 읽기
4. **검증**: DTO의 모든 필요한 필드가 Mapper를 통해 Domain Model에 매핑되는지

### B: Domain Model → UiState 필드 제공

1. Domain Model의 필드 목록 확인
2. `feature/{name}/src/commonMain/.../contract/{Name}State.kt` 읽기
3. **검증**: UI에서 사용해야 할 Domain Model 필드가 UiState 에 담기는지 (도메인 모델 그대로, 또는 Data 의 `get()` / `fun`). 문자열 포맷팅만을 위한 ItemState 는 없어야 한다 (CLAUDE.md "UI State 파싱 규칙")

### C: Repository Interface ↔ RepositoryImpl 시그니처 일치

1. `core/domain/src/commonMain/.../repository/` 에서 Interface 읽기
2. `core/data/src/commonMain/.../repository/` 에서 Impl 읽기
3. **검증**: 메서드 시그니처(이름, 파라미터, 반환 타입) 완전 일치

### D: UseCase → ViewModel 주입 및 사용

1. UseCase 확인
2. ViewModel의 생성자 파라미터에서 주입 확인
3. **검증**: UseCase가 실제로 ViewModel에서 호출되는지

### E: Koin 모듈 등록 완전성

1. `@Single`, `@KoinViewModel` 어노테이션이 있는 클래스 검색
2. 각 모듈의 `@Module @ComponentScan` 확인
3. **검증**: 모든 DI 대상이 Koin 모듈에 의해 스캔되는 패키지에 있는지

### F: Navigation 등록 완전성

1. `core/navigation/.../Route.kt` 에서 Route 정의 확인
2. `feature/main/.../MainScreen.kt` 에서 NavDisplay 라우팅 확인
3. `feature/main/build.gradle.kts` 에서 의존성 확인
4. `settings.gradle.kts` 에서 모듈 등록 확인
5. **검증**: Route가 정의되어 있고, 화면이 등록되어 있고, 의존성이 추가되어 있는지

## 출력 형식

```
## QA Integration 검증 결과

| 영역 | 항목 | 결과 | 상세 |
|------|------|------|------|
| A | DTO→Mapper→Domain | ✅ Pass / ❌ Critical | {상세 설명} |
| B | Domain→UiState | ✅ Pass / ⚠️ Warning | {상세 설명} |
| C | Repository IF↔Impl | ✅ Pass / ❌ Critical | {상세 설명} |
| D | UseCase→ViewModel | ✅ Pass / ⚠️ Warning | {상세 설명} |
| E | Koin 모듈 등록 | ✅ Pass / ❌ Critical | {상세 설명} |
| F | Navigation 등록 | ✅ Pass / ❌ Critical | {상세 설명} |
```
