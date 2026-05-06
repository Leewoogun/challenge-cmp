---
description: "서버 API 연동 Data Layer 구현 - DTO, Ktorfit API, Mapper, RepositoryImpl, Koin 모듈 등록 (KMP)"
model: claude-sonnet-4-6
user-invocable: true
allowed-tools:
  - Read
  - Write
  - Edit
  - Glob
  - Grep
---

# Remote Data Layer Skill

$ARGUMENTS 를 기반으로 Remote API Data Layer를 구현합니다.

## 필요 인자

`$ARGUMENTS`에서 다음 정보를 파악합니다:
- **기능명**: PascalCase (예: `Recipe`, `Search`, `Auth`)
- **API 스펙**: 엔드포인트, HTTP 메서드, Request/Response 구조

## 모듈 아키텍처

```
:remote:network        # Ktor + Ktorfit 네트워크 설정, ApiResult
:remote:api            # Ktorfit API 인터페이스
:remote:model          # DTO (Request/Response, BaseResponse)
:remote:mapper         # DTO → Domain Model 변환 확장 함수
                       # 의존: :domain:model, :remote:model
:local:datastore       # DataStore 기반 LocalDataSource (토큰 등)
:data:repositoryImpl   # Repository 구현체, Koin 모듈
                       # 의존: :domain:*, :remote:*, :local:*
:domain:model          # 순수 Domain Model
:domain:repository     # Repository 인터페이스
:domain:usecase        # UseCase
```

## Repository 메서드 반환 규칙 (필수)

Repository / UseCase 메서드의 반환 형태는 다음 두 가지로만 작성한다.

| 케이스 | 시그니처 | 예시 |
|---|---|---|
| **Domain Model을 반환** (값) | `fun foo(...onError: (String) -> Unit): Flow<T>` | `getRecipeList`, `loginWithKakao` |
| **데이터 없이 성공/실패만 반환** | `suspend fun foo(...onSuccess: () -> Unit, onError: (String) -> Unit)` | `saveBizInfo`, `submitDocuments` |

**금지 패턴**:
- ❌ `suspend fun foo(...): T?` — nullable 반환으로 성공/실패 표현
- ❌ `suspend fun foo(..., onSuccess: (T) -> Unit, onError: (String) -> Unit)` — 데이터를 콜백으로 전달
- ❌ Repository에서 DTO(`Response`)를 그대로 반환 — 반드시 Mapper로 Domain Model 변환

**핵심 원칙**: Domain Model은 **Mapper를 통해 변환된 후 Flow로 emit** 한다. 콜백으로 데이터를 직접 넘기지 않는다. 실패는 `onError(message)` 한 가지로만 통지하고, Flow는 emit하지 않는 것으로 실패를 표현한다.

## 구현 순서

### 1단계: DTO 모델 (Request/Response)

**위치**: `remote/model/src/commonMain/kotlin/com/lwg/challenge/remote/model/{feature}/`

**규칙**:
- `@Serializable` 어노테이션 필수
- `commonMain`에 작성
- **Response 필드에 기본값 설정** (null → 기본값 변환)
- snake_case 필드명은 `@SerialName` 사용
- `BaseResponse` 상속 (envelope 응답)

```kotlin
package com.lwg.challenge.remote.model.recipe

import com.lwg.challenge.remote.model.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecipeListResponse(
    val data: RecipeListData = RecipeListData(),
) : BaseResponse()

@Serializable
data class RecipeListData(
    val page: Int = 0,
    val results: List<RecipeResponse> = emptyList(),
    @SerialName("total_pages")
    val totalPages: Int = 0,
)

@Serializable
data class RecipeResponse(
    val id: Int = 0,
    val title: String = "",
    val overview: String = "",
    @SerialName("poster_path")
    val posterPath: String? = null,
)
```

`BaseResponse`는 `:remote:model`에 위치 (네트워크 인프라가 아닌 공통 envelope **DTO**). 모든 Ktorfit 응답 타입은 이를 상속해야 `ApiResultConverterFactory`가 `body.code` 검사를 자동 수행한다.

### 2단계: Ktorfit API 인터페이스

**위치**: `remote/api/src/commonMain/kotlin/com/lwg/challenge/remote/api/`

**규칙**:
- `interface` + Ktorfit 어노테이션 사용
- `suspend fun` 사용
- 패키지: `com.lwg.challenge.remote.api`

```kotlin
package com.lwg.challenge.remote.api

import com.lwg.challenge.remote.model.recipe.RecipeListResponse
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Query

interface RecipeApi {

    @GET("recipe/list")
    suspend fun getRecipeList(
        @Query("page") page: Int = 1,
    ): RecipeListResponse
}
```

### 3단계: Mapper (DTO → Domain)

**위치**: `remote/mapper/src/commonMain/kotlin/com/lwg/challenge/remote/mapper/`

**규칙**:
- 확장 함수로 작성
- 네이밍: `fun {Response}.to{DomainModel}(): DomainModel`
- 패키지: `com.lwg.challenge.remote.mapper`
- **`:remote:mapper` 모듈에 둔다** (`:data:repositoryImpl` 아님). DTO→Domain 변환은 remote 레이어 책임

```kotlin
package com.lwg.challenge.remote.mapper

import com.lwg.challenge.domain.model.recipe.Recipe
import com.lwg.challenge.remote.model.recipe.RecipeResponse

fun RecipeResponse.toRecipe(): Recipe {
    return Recipe(
        id = id,
        title = title,
        overview = overview,
        imageUrl = posterPath?.let { "https://image.tmdb.org/t/p/w500$it" } ?: "",
    )
}
```

### 4단계: RepositoryImpl 구현

**위치**: `data/repositoryImpl/src/commonMain/kotlin/com/lwg/challenge/data/repository/`

**규칙**:
- `@Single` Koin annotation 사용
- API 인터페이스 주입 (`:remote:api`)
- 로컬 저장이 필요하면 `LocalDataSource` 주입 (`:local:datastore`)
- 패키지: `com.lwg.challenge.data.repository`
- **Domain Model 반환 메서드는 반드시 `Flow<T>` + Mapper 사용** — DTO를 직접 반환하거나 nullable로 반환하지 않는다
- 실패 시 `onError(message)` 호출 후 emit하지 않음 (Flow가 비어있는 것이 곧 실패)
- **`suspendOnSuccess` 람다 안에서 직접 `this@flow.emit(...)` 호출** — 임시 `var`에 담았다가 람다 밖에서 emit하지 않는다

```kotlin
package com.lwg.challenge.data.repository

import com.lwg.challenge.domain.model.recipe.Recipe
import com.lwg.challenge.domain.repository.RecipeRepository
import com.lwg.challenge.remote.api.RecipeApi
import com.lwg.challenge.remote.mapper.toRecipe
import com.lwg.challenge.remote.network.util.suspendOnFailureWithErrorHandling
import com.lwg.challenge.remote.network.util.suspendOnSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single

@Single
class RecipeRepositoryImpl(
    private val recipeApi: RecipeApi,
) : RecipeRepository {

    override fun getRecipeList(
        page: Int,
        onError: (String) -> Unit,
    ): Flow<List<Recipe>> = flow {
        recipeApi.getRecipeList(page)
            .suspendOnSuccess {
                this@flow.emit(response.results.map { it.toRecipe() })
            }
            .suspendOnFailureWithErrorHandling(onError)
    }
}
```

**잘못된 예 (안티패턴)**:

```kotlin
// ❌ var 임시 변수로 받아서 람다 밖에서 emit — 의미 없는 한 단계 추가
override fun getRecipeList(...): Flow<List<Recipe>> = flow {
    var result: List<Recipe>? = null
    recipeApi.getRecipeList(page)
        .suspendOnSuccess { result = response.results.map { it.toRecipe() } }
        .suspendOnFailureWithErrorHandling(onError)
    result?.let { emit(it) }
}
```

### 5단계: Koin 모듈 등록

**위치**: `data/repositoryImpl/src/commonMain/kotlin/com/lwg/challenge/data/di/DataModule.kt`

기존 DataModule에 `@ComponentScan`이 설정되어 있으면 `@Single` 어노테이션으로 자동 등록됩니다.

API 인터페이스의 Koin 등록은 `:remote:network`의 NetworkModule에서 Ktorfit builder로 제공합니다.

## 빌드 검증

```bash
./gradlew :remote:mapper:compileCommonMainKotlinMetadata :data:repositoryImpl:compileCommonMainKotlinMetadata
```

## 체크리스트

- [ ] DTO가 `@Serializable`이고 `:remote:model` `commonMain`에 있는지
- [ ] Response 필드에 기본값이 설정되어 있는지 (BaseResponse 상속)
- [ ] API 인터페이스가 `:remote:api`에 있고 Ktorfit 어노테이션을 사용하는지
- [ ] **Mapper가 `:remote:mapper` 모듈에 있는지** (`:data:repositoryImpl` 아님)
- [ ] RepositoryImpl이 `:data:repositoryImpl`에 있고 `@Single` Koin 어노테이션이 있는지
- [ ] 로컬 저장이 필요하면 `:local:datastore`의 LocalDataSource 주입
- [ ] Koin 모듈에 등록되었는지
