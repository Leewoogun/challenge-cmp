# feature별 API 문서화 + DTO 주석 정리

작성일: 2026-09-24

## 배경

서버 계약(request/response)의 원본이 세 군데로 갈라져 있다.

| 위치 | 성격 | 상태 |
|---|---|---|
| Swagger (`/v3/api-docs`) | 진짜 원본 | 정확 |
| 노션 `📱 챌린지 앱 Mobile` §6 | 개요 + 엔드포인트 표 | 드리프트 |
| DTO KDoc | 응답 JSON 샘플 사본 | 약 230줄, 드리프트 위험 |

코드가 참조하는 `api-contract §3`, `design.md §1.7.3` 등의 문서는 repo 에 존재하지 않아 참조가 전부 끊겨 있다.

## 사전 검증 결과 (2026-09-24 실측)

서버를 띄우고 `POST /auth/test-login` 으로 토큰을 받아 GET 13종의 실제 응답을 캡처해 DTO 와 대조했다.

- **엔드포인트**: 앱이 호출하는 29개가 서버 31개와 경로·메서드 100% 일치. 미사용 2개는 설계상 의도된 것
  (`GET /challenges/{id}/photos/{party}` 는 Coil 이 URL 로 직접 소비, `POST /auth/refresh` 는 Ktor Auth 플러그인)
- **필드**: 최상위 data 클래스 11종 전부 필드 일치. 누락·초과 없음
- **nullability**: 실제로 `null` 이 내려온 12개 필드가 전부 Kotlin 에서도 nullable

**결론: 계약 버그는 없다.** 이 작업은 버그 수정이 아니라 문서 일원화와 중복 제거다.

### 별도로 기록해 둘 것

`VerificationStatusDto` 는 `@Serializable` typed enum 이라 서버가 값을 추가하면 응답 전체의 역직렬화가
깨진다. `Json` 설정에 `coerceInputValues` 가 없어 실제로 예외가 난다. 같은 파일의 `status` / `myResult`
는 이 이유로 이미 String 인데 이 필드만 enum 으로 남아 있다.

코드에 "backend 에 값 추가 계획 확인 중" 으로 기록돼 있고, Swagger 가 현재 `PENDING`/`VERIFIED`/`FAILED`
3개만 선언하므로 **오늘 기준으로는 안전하다.** 이번 작업 범위에 넣지 않고 백엔드 확인 후 별도로 처리한다.

## 목표

1. 노션에 feature 단위 API 페이지를 만들어 "이 화면이 무슨 API 를 부르는가" 를 한 곳에서 본다
2. DTO KDoc 의 JSON 샘플을 노션으로 옮겨 코드에서 사본을 없앤다
3. 끊긴 `§참조` 를 노션 링크로 되살린다

## 노션 구조

```
📱 챌린지 앱 Mobile
 └ 🔌 API 연동
    ├ 공통 (봉투 · 에러코드 · 날짜 포맷 · 인증 헤더 · splash 각주)
    ├ login              ├ home              ├ challenge/create   ├ ranking
    ├ friends/list       ├ friends/search    ├ challenge/oath     ├ mypage
    ├ challenge/detail   ├ challenge/verify  ├ notification       └ main
```

`splash` 는 API 호출이 없어(DataStore 토큰 조회만) 페이지를 만들지 않고 공통 페이지에 각주로 단다.
총 **12개 feature 페이지 + 공통 1개**.

기존 §6 서버 연동은 공통 규약만 남기고 엔드포인트 표는 이 페이지로 넘긴다. 이때 §6 의 드리프트도 함께 고친다:

| 노션 기재 | 실제 |
|---|---|
| `GET /users/record` | `GET /api/v1/record` |
| `GET /notifications/{id}` | 존재하지 않음 |
| (누락) | `POST /auth/test-login` |
| "목록 페이지네이션: 커서 방식" | 커서는 `/notifications` 만. history 는 `month`, rankings·record 는 파라미터 없음 |

### 페이지 형태

```
# challenge/detail
> 챌린지 상세 — 계약서 · 양쪽 인증 사진 · 결과 · 기권

## 호출 API
| 시점 | 엔드포인트 | 용도 |

## 상세
▸ GET /api/v1/challenges/{id}
   ▸ Request       (path · query · header)
   ▸ Response      (실제 JSON)
   ▸ 실패 코드     (code != 200)
```

토글 안에는 **실제 JSON 샘플**을 넣는다. 필드 표는 만들지 않는다 — 중첩 구조 표현이 어렵고
Swagger 가 이미 타입·nullable 을 제공한다.

### 샘플 출처

- **GET 17종**: 로컬 서버에서 실제 호출해 캡처한 응답 (정확도 최상, 이상치·null 이 그대로 드러남)
- **POST / DELETE**: 호출하지 않는다 — 챌린지를 실제로 생성·기권시키게 된다. 기존 DTO KDoc 샘플 +
  Swagger 스키마로 구성한다

## feature → API 매핑

| feature | 호출 API |
|---|---|
| `splash` | *(로컬 전용)* |
| `login` | `POST /auth/kakao` · `POST /auth/test-login` · `PUT /users/me/fcm-token` |
| `home` | `GET /record` · `GET /users/me` · `GET /challenges/active` · `GET /challenges/received` · `GET /notifications/unread-count` · `POST /challenges/{id}/reject` |
| `friends/list` | `GET /friends` · `GET /friends/requests/received` · `GET /users/me` · `POST /friends/requests/{id}/accept` · `POST /friends/requests/{id}/reject` |
| `friends/search` | `GET /users/search` · `POST /friends/requests` · `POST /friends/requests/{id}/accept` · `DELETE /friends/requests/{id}` |
| `challenge/create` | `GET /friends` · `POST /challenges` |
| `challenge/oath` | `GET /challenges/{id}` · `POST /challenges/{id}/accept` |
| `challenge/detail` | `GET /challenges/{id}` · `GET /challenges/{id}/verifications` · `GET /users/me` · `POST /challenges/{id}/forfeit` |
| `challenge/verify` | `POST /challenges/{id}/verification` (multipart) |
| `ranking` | `GET /rankings/losers` |
| `mypage` | `GET /record` · `GET /users/me` · `GET /challenges/history` · `DELETE /users/me` |
| `notification` | `GET /notifications` · `POST /notifications/read-all` |
| `main` | `DELETE /auth/logout` |

## 코드 정리 규칙

feature 1개당 다음을 수행하고 커밋 1개로 끊는다.

1. DTO KDoc 의 JSON 샘플 삭제 → `/** GET /api/v1/... */` 한 줄만 남김
2. **"왜" 는 유지, "무엇" 은 삭제**
   - 유지: `result` 가 enum 아닌 이유, `forfeitedBy` 가 Boolean 아닌 이유, 드롭 정책의 근거
   - 삭제: `"yyyy-MM-dd"` 포맷 설명, 필드 목록 나열, 서버 동작 prose
   - 근거: CLAUDE.md 주석 규칙 — 모델 KDoc 에 "백엔드가 ~ 응답한다" 식 서버 동작 설명을 적지 않는다
3. 끊긴 `§참조` 를 노션 링크로 교체
4. 중복 DTO 통합 — `AcceptChallengeData` 와 `RejectChallengeData` 는 필드까지 동일
   (`challengeId: Long`, `status: String`), `CancelChallengeData` 는 그 부분집합.
   Swagger 도 이 셋을 `ChallengeStatusData` 하나로 선언하고 있다
5. `./gradlew :{모듈}:compileCommonMainKotlinMetadata` + 해당 모듈 테스트
6. 커밋

## 진행 순서

의존성이 얕은 것부터: `login` → `ranking` → `notification` → `mypage` → `friends/list` →
`friends/search` → `challenge/create` → `challenge/oath` → `challenge/verify` →
`challenge/detail` → `home` → `main`

`home` 이 5개 Repository 를 쓰는 가장 무거운 화면이라 뒤에 둔다.

## 범위 밖

- `VerificationStatusDto` 의 typed enum 문제 (백엔드 확인 필요)
- Swagger 자동 생성 파이프라인 구축
- 매퍼·Repository 로직 리팩토링 — 이번엔 주석과 DTO 중복만 건드린다
