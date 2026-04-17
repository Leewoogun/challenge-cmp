#!/bin/bash

# Core 모듈 자동 생성 스크립트
# 사용법: ./create-core-module.sh [모듈명] [타입]
# 타입: android (기본값) 또는 kotlin

set -e

if [ $# -eq 0 ]; then
    echo "사용법: ./create-core-module.sh [모듈명] [타입]"
    echo ""
    echo "타입:"
    echo "  android  - Android Library 모듈 (기본값)"
    echo "  kotlin   - 순수 Kotlin 모듈"
    echo ""
    echo "예시:"
    echo "  ./create-core-module.sh data android"
    echo "  ./create-core-module.sh domain kotlin"
    exit 1
fi

MODULE_NAME=$1
MODULE_TYPE=${2:-android}

# 타입 검증
if [[ "$MODULE_TYPE" != "android" && "$MODULE_TYPE" != "kotlin" ]]; then
    echo "❌ 오류: 타입은 'android' 또는 'kotlin'만 가능합니다."
    exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
MODULE_PATH="$PROJECT_ROOT/core/$MODULE_NAME"
PACKAGE_PATH="com/lwg/base/$MODULE_NAME"

echo "📦 Core 모듈 생성 중: $MODULE_NAME (타입: $MODULE_TYPE)"

# 1. 모듈 디렉토리가 이미 존재하는지 확인
if [ -d "$MODULE_PATH" ]; then
    echo "❌ 오류: core/$MODULE_NAME 모듈이 이미 존재합니다."
    exit 1
fi

# 2. 디렉토리 구조 생성
echo "📁 디렉토리 구조 생성 중..."
if [ "$MODULE_TYPE" = "android" ]; then
    mkdir -p "$MODULE_PATH/src/commonMain/kotlin/$PACKAGE_PATH"
    mkdir -p "$MODULE_PATH/src/androidMain/kotlin/$PACKAGE_PATH"
    mkdir -p "$MODULE_PATH/src/iosMain/kotlin/$PACKAGE_PATH"
else
    mkdir -p "$MODULE_PATH/src/commonMain/kotlin/$PACKAGE_PATH"
fi

# 3. build.gradle.kts 생성
echo "📝 build.gradle.kts 생성 중..."
if [ "$MODULE_TYPE" = "android" ]; then
    cat > "$MODULE_PATH/build.gradle.kts" << 'EOF'
plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.challengeKotlinMultiplatform)
    alias(libs.plugins.challengeComposeMultiplatform)
}

android {
    namespace = "com.lwg.MODULE_NAME"
}

kotlin {
    sourceSets {
        androidMain.dependencies {

        }
        commonMain.dependencies {

        }
        iosMain.dependencies {

        }
    }
}
EOF
    # namespace 치환
    sed -i '' "s/MODULE_NAME/$MODULE_NAME/g" "$MODULE_PATH/build.gradle.kts"
else
    # kotlin 모듈
    cat > "$MODULE_PATH/build.gradle.kts" << 'EOF'
plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    sourceSets {
        commonMain.dependencies {

        }
    }
}
EOF
fi

# 4. settings.gradle.kts에 모듈 추가
echo "⚙️ settings.gradle.kts에 모듈 추가 중..."
if ! grep -q "include(\":core:$MODULE_NAME\")" "$PROJECT_ROOT/settings.gradle.kts"; then
    echo "include(\":core:$MODULE_NAME\")" >> "$PROJECT_ROOT/settings.gradle.kts"
    echo "✅ settings.gradle.kts에 추가됨"
else
    echo "⚠️ settings.gradle.kts에 이미 존재합니다"
fi

echo ""
echo "✅ Core 모듈 생성 완료! (타입: $MODULE_TYPE)"
echo ""
echo "📂 생성된 구조:"
echo "   core/$MODULE_NAME/"
echo "   ├── build.gradle.kts"
if [ "$MODULE_TYPE" = "android" ]; then
    echo "   │   └── plugins: androidLibrary + KMP + Compose"
else
    echo "   │   └── plugins: KMP only"
fi
echo "   └── src/"
if [ "$MODULE_TYPE" = "android" ]; then
    echo "       ├── commonMain/kotlin/$PACKAGE_PATH/"
    echo "       ├── androidMain/kotlin/$PACKAGE_PATH/"
    echo "       └── iosMain/kotlin/$PACKAGE_PATH/"
else
    echo "       └── commonMain/kotlin/$PACKAGE_PATH/"
fi
echo ""
echo "🔄 다음 단계:"
echo "   1. Android Studio에서 'Sync Project with Gradle Files' 실행"
echo "   2. core/$MODULE_NAME/src/commonMain/kotlin/$PACKAGE_PATH 에 코드 작성 시작"
if [ "$MODULE_TYPE" = "kotlin" ]; then
    echo ""
    echo "💡 순수 Kotlin 모듈은 Android 의존성이 없습니다."
    echo "   Android 기능이 필요하면 'android' 타입으로 다시 생성하세요."
fi