---
description: "로컬 저장소 Data Layer 구현 - DataStore 기반 LocalDataSource 및 Koin 등록 (KMP)"
model: claude-sonnet-4-6
user-invocable: true
allowed-tools:
  - Read
  - Write
  - Edit
  - Glob
  - Grep
---

# Local Data Layer Skill

$ARGUMENTS 를 기반으로 Local Data Layer를 구현합니다.

## 필요 인자

`$ARGUMENTS`에서 다음 정보를 파악합니다:
- **기능명**: PascalCase (예: `Setting`, `Token`, `UserPreference`)
- **데이터 구조**: 저장할 필드

## DataStore 구현 순서

### 1단계: Prefs 모델 정의

**위치**: `core/data/src/commonMain/kotlin/com/lwg/cooking/data/local/model/`

**규칙**:
- `@Serializable data class`
- 모든 필드에 기본값 설정
- `commonMain`에 작성

```kotlin
package com.lwg.cooking.data.local.model

import kotlinx.serialization.Serializable

@Serializable
data class UserPrefs(
    val token: String = "",
    val isAutoLogin: Boolean = false,
    val userName: String = "",
)
```

### 2단계: Prefs Mapper (Prefs ↔ Domain)

> Domain Model이 없으면 이 단계를 건너뜁니다.

**위치**: `core/data/src/commonMain/kotlin/com/lwg/cooking/data/local/mapper/`

```kotlin
package com.lwg.cooking.data.local.mapper

import com.lwg.cooking.data.local.model.UserPrefs
import com.lwg.cooking.domain.model.user.UserInfo

fun UserPrefs.toUserInfo(): UserInfo {
    return UserInfo(
        name = userName,
        isAutoLogin = isAutoLogin,
    )
}

fun UserInfo.toUserPrefs(): UserPrefs {
    return UserPrefs(
        userName = name,
        isAutoLogin = isAutoLogin,
    )
}
```

### 3단계: LocalDataSource 인터페이스

**위치**: `core/domain/src/commonMain/kotlin/com/lwg/cooking/domain/repository/` 또는 별도 인터페이스

```kotlin
interface UserLocalRepository {
    fun observeUser(): Flow<UserInfo?>
    suspend fun getUser(): UserInfo?
    suspend fun saveUser(userInfo: UserInfo)
    suspend fun deleteUser()
}
```

### 4단계: LocalDataSource 구현

**위치**: `core/data/src/commonMain/kotlin/com/lwg/cooking/data/local/`

**규칙**:
- `@Single` Koin annotation 사용
- DataStore 주입
- Flow 조회: `.data.map { prefs -> prefs.toDomain() }` 파이프라인

```kotlin
package com.lwg.cooking.data.local

import com.lwg.cooking.data.local.mapper.toUserInfo
import com.lwg.cooking.data.local.mapper.toUserPrefs
import com.lwg.cooking.data.local.model.UserPrefs
import com.lwg.cooking.domain.model.user.UserInfo
import com.lwg.cooking.domain.repository.UserLocalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
class UserLocalRepositoryImpl(
    private val dataStore: DataStore<UserPrefs>,
) : UserLocalRepository {

    override fun observeUser(): Flow<UserInfo?> {
        return dataStore.data.map { prefs ->
            if (prefs.userName.isEmpty()) null else prefs.toUserInfo()
        }
    }

    override suspend fun saveUser(userInfo: UserInfo) {
        dataStore.updateData { userInfo.toUserPrefs() }
    }
}
```

## ⚠️ Platform-specific 구현 (expect/actual)에 Koin annotation 금지

`@Single`/`@Factory` 같은 Koin annotation은 **commonMain의 `actual` 클래스 또는 commonMain `expect`**에는 정상 동작하지만, **platform-specific source set(`androidMain`/`iosMain`)의 `actual` 클래스**에 붙이면 commonMain의 `@Module @ComponentScan`이 **picked up하지 못합니다**.

### 왜 안 되는가

- KSP는 타겟별로 따로 동작 (metadata/commonMain, androidDebug, iosSimulatorArm64).
- `@Module @ComponentScan`이 commonMain에 정의되면 **metadata/commonMain pass**에서 모듈 코드가 생성되며 commonMain 컴포넌트만 등록됨.
- 플랫폼별 KSP pass에서 발견된 platform-specific `@Single`은 이미 finalize된 commonMain 모듈에 추가할 수 없어 **`defaultModule`이라는 별도 platform-specific 모듈로 분리**됨.
- `defaultModule`은 platform별로 코드가 달라 commonMain 모듈 그래프(`includes = [...]`)에 포함시키기 어렵고, 결국 런타임에 `NoDefinitionFoundException` 발생.

### 올바른 패턴: `expect/actual val Module` 수동 등록

DataStore, SecureStorage 등 **plaftorm별 구현이 다른 컴포넌트**는 Koin annotation 대신 다음 패턴을 사용한다.

```kotlin
// commonMain — interface 또는 expect class만 (Koin annotation 없이)
interface SecureTokenStorage { ... }
expect class SecureTokenStorageImpl : SecureTokenStorage

// androidMain
actual class SecureTokenStorageImpl(
    private val context: Context,
) : SecureTokenStorage { ... }

// iosMain
actual class SecureTokenStorageImpl : SecureTokenStorage { ... }
```

Koin 등록은 별도의 `expect/actual val Module`로:

```kotlin
// commonMain/.../di/PlatformDataModule.kt
expect val platformDataModule: Module

// androidMain/.../di/PlatformDataModule.android.kt
actual val platformDataModule: Module = module {
    single<SecureTokenStorage> { SecureTokenStorageImpl(get()) }
}

// iosMain/.../di/PlatformDataModule.ios.kt
actual val platformDataModule: Module = module {
    single<SecureTokenStorage> { SecureTokenStorageImpl() }
}
```

`composeApp/.../App.kt`의 `startKoin` modules 리스트에 추가:

```kotlin
modules(
    AppModule().module,
    // ...
    platformDataModule,
)
```

### 적용 대상 판단 기준

- ✅ commonMain의 `class` (expect/actual 아님) → `@Single` 사용 OK
- ✅ commonMain의 `interface` + commonMain의 `class` 구현체 → `@Single(binds = [...])` 사용 OK
- ❌ commonMain의 `expect class` + androidMain/iosMain의 `actual class` → **수동 `expect/actual val Module` 필수**

## 체크리스트

- [ ] Prefs 모델이 `@Serializable data class`인지
- [ ] 모든 필드에 기본값이 있는지
- [ ] Mapper가 양방향 변환하는지 (Domain 존재 시)
- [ ] DataSource 구현체에 `@Single` Koin 어노테이션이 있는지 (commonMain 한정)
- [ ] **expect/actual인 경우 `@Single` 대신 `expect/actual val Module` 패턴을 사용했는지**
- [ ] `commonMain`에 작성했는지
