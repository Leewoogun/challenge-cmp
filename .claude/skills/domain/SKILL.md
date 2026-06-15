---
description: "Domain Layer 구현 - 순수 Kotlin Model 정의, Repository 인터페이스, UseCase 비즈니스 로직 작성 (KMP commonMain)"
model: claude-sonnet-4-6
user-invocable: true
allowed-tools:
  - Read
  - Write
  - Edit
  - Glob
  - Grep
---

# Domain Layer Skill

$ARGUMENTS 를 기반으로 Domain Layer를 구현합니다.

## 필요 인자

`$ARGUMENTS`에서 다음 정보를 파악합니다:
- **기능명**: PascalCase (예: `Recipe`, `Search`, `Profile`)
- **데이터 구조**: 필요한 필드 및 비즈니스 요구사항

## 구현 순서

### 1단계: Domain Model 정의

**위치**: `core/domain/src/commonMain/kotlin/com/lwg/cooking/domain/model/{feature}/`

**규칙**:
- 순수 Kotlin (Android 의존성 절대 금지)
- `commonMain`에 작성 (KMP 공통 코드)
- `data class` 또는 `sealed interface` 사용
- 불변 객체 (`val` 필드만)
- `@Serializable` 허용 (직렬화 필요 시)
- 패키지: `com.lwg.cooking.domain.model.{feature}`

**data class 사용 시기**: 데이터를 담는 모델, 값 객체, API 응답 매핑 결과
**sealed interface 사용 시기**: 제한된 타입 계층, 상태/이벤트/에러 타입 표현
**enum class 사용 시기**: 서버 코드값 매핑, 고정된 상태/타입 구분

#### data class 패턴

```kotlin
package com.lwg.cooking.domain.model.recipe

data class Recipe(
    val id: Int,
    val title: String,
    val overview: String,
    val imageUrl: String,
)
```

#### data class + 계산 프로퍼티 패턴

```kotlin
data class RecipeDetail(
    val id: Int,
    val title: String,
    val cookingTime: Int,
    val servings: Int,
) {
    val cookingTimeText: String
        get() = "${cookingTime}분"

    val servingsText: String
        get() = "${servings}인분"
}
```

#### sealed interface 패턴

```kotlin
sealed interface RecipeCategory {
    data object Korean : RecipeCategory
    data object Western : RecipeCategory
    data object Japanese : RecipeCategory
    data class Custom(val name: String) : RecipeCategory
}
```

#### enum class + fromCode 패턴

```kotlin
enum class DifficultyLevel(val value: Int, val text: String) {
    EASY(1, "쉬움"),
    MEDIUM(2, "보통"),
    HARD(3, "어려움"),
    ;

    companion object {
        fun findByValue(value: Int) = entries.find { it.value == value } ?: EASY
    }
}
```

**Enum companion object 네이밍 규칙**:
- `fromCode(code)` / `findByCode(code)` - 코드값 기반 조회
- `fromValue(value)` / `findByValue(value)` - 값 기반 조회
- 찾지 못하면 기본값 반환 (예외 throw 금지)

### 2단계: Repository Interface 정의

**위치**: `core/domain/src/commonMain/kotlin/com/lwg/cooking/domain/repository/`

**규칙**:
- `interface`로 정의 (구현은 Data Layer에서)
- 패키지: `com.lwg.cooking.domain.repository`
- 도메인 모델만 사용 (DTO, Entity 사용 금지)
- 에러 콜백: `onError: (String) -> Unit`
- **단일 데이터 소스 책임만 가진다** — 다른 Repository 를 의존하거나 그 결과를 `combine`/`zip`/`flatMap` 으로 합치지 않는다. 화면용 통합 데이터는 UseCase 에서 합성한다 (패턴 B 참조). 화면 이름이 들어간 Repository (`HomeRepository`, `ProfileRepository` 등) 금지 — Repository 는 데이터 도메인 단위 (`RecordRepository`, `UserRepository`)

**Flow vs suspend fun 선택**:
- `Flow<T>`: 데이터 조회, 실시간 스트림, 여러 값 방출
- `suspend fun`: 단일 결과, 일회성 CRUD 작업

```kotlin
package com.lwg.cooking.domain.repository

import kotlinx.coroutines.flow.Flow
import com.lwg.cooking.domain.model.recipe.Recipe

interface RecipeRepository {

    fun getRecipeList(
        page: Int = 1,
        onError: (String) -> Unit,
    ): Flow<List<Recipe>>

    suspend fun getRecipeDetail(
        id: Int,
        onError: (String) -> Unit,
    ): Recipe
}
```

### 3단계: UseCase 구현

**위치**: `core/domain/src/commonMain/kotlin/com/lwg/cooking/domain/usecase/{feature}/`

**규칙**:
- 패키지: `com.lwg.cooking.domain.usecase.{feature}`
- `class` 생성자로 Repository/UseCase 주입 (Koin이 자동 주입)
- `operator fun invoke()` 사용
- 단일 책임 원칙

**UseCase를 만드는 기준**:
- 여러 Repository를 조합해야 할 때
- Validation 로직이 필요할 때
- 비즈니스 규칙(필터링, 계산 등)이 있을 때
- 단순 Repository 위임만이면 UseCase 없이 ViewModel에서 직접 호출 가능

#### 패턴 A: Validation + Repository 호출

```kotlin
package com.lwg.cooking.domain.usecase.recipe

import com.lwg.cooking.domain.repository.RecipeRepository

class SearchRecipeUseCase(
    private val recipeRepository: RecipeRepository,
) {
    suspend operator fun invoke(
        query: String,
        onError: (String) -> Unit,
    ): List<Recipe> {
        if (query.isBlank()) {
            onError("검색어를 입력해주세요.")
            return emptyList()
        }
        return recipeRepository.searchRecipe(query, onError)
    }
}
```

#### 패턴 B: 여러 Repository 조합 (combine)

```kotlin
class GetRecipeWithBookmarkUseCase(
    private val recipeRepository: RecipeRepository,
    private val bookmarkRepository: BookmarkRepository,
) {
    operator fun invoke(
        onError: (String) -> Unit,
    ): Flow<List<RecipeWithBookmark>> = combine(
        recipeRepository.getRecipeList(onError = onError),
        bookmarkRepository.getBookmarkIds(),
    ) { recipes, bookmarkIds ->
        recipes.map { recipe ->
            RecipeWithBookmark(
                recipe = recipe,
                isBookmarked = recipe.id in bookmarkIds,
            )
        }
    }
}
```

## Domain Layer 의존성 규칙

### 사용 가능
- Kotlin 표준 라이브러리
- Kotlinx Coroutines (Flow, suspend)
- Kotlinx Serialization (`@Serializable`)

### 사용 금지
- Android Framework (Context, View, Activity 등)
- Data Layer 구현체 (RepositoryImpl, DataSource 등)
- DTO (Response, Request 등)
- UI Layer (ViewModel, Composable 등)

## 체크리스트

- [ ] Domain Model이 순수 Kotlin인지 (Android 의존성 없음)
- [ ] `commonMain`에 작성했는지
- [ ] Enum 조회 실패 시 기본값 반환하는지 (예외 throw 금지)
- [ ] Repository Interface가 Domain Model만 사용하는지
- [ ] UseCase에 `operator fun invoke()` 사용하는지
