#!/bin/bash

if [ $# -ne 1 ]; then
    echo "Usage: $0 <featureName>"
    echo "Example: $0 recipe"
    echo "Example: $0 RecipeDetail"
    exit 1
fi

RAW_NAME=$1

# 프로젝트 루트 디렉터리 찾기
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# 호환성을 위한 문자열 변환 함수
to_lower() {
    echo "$1" | tr '[:upper:]' '[:lower:]'
}

to_upper_first() {
    echo "$(echo ${1:0:1} | tr '[:lower:]' '[:upper:]')${1:1}"
}

to_lower_first() {
    echo "$(echo ${1:0:1} | tr '[:upper:]' '[:lower:]')${1:1}"
}

LOWER_FEATURE_NAME=$(to_lower "$RAW_NAME")
UPPER_CAMEL_CASE_NAME=$(to_upper_first "$RAW_NAME")
LOWER_CAMEL_CASE_NAME=$(to_lower_first "$RAW_NAME")

BASE_PACKAGE="com.lwg.challenge"
FEATURE_MODULE_DIR="$PROJECT_ROOT/feature/$LOWER_FEATURE_NAME"
SRC_DIR="$FEATURE_MODULE_DIR/src/commonMain/kotlin/com/lwg/base/feature/$LOWER_FEATURE_NAME"

echo "======================================="
echo "  Feature Module Generator (KMP)"
echo "======================================="
echo "Feature Name: $UPPER_CAMEL_CASE_NAME"
echo "Module Path: feature/$LOWER_FEATURE_NAME"
echo "Package: $BASE_PACKAGE.feature.$LOWER_FEATURE_NAME"
echo ""

# 이미 존재하는지 확인
if [ -d "$FEATURE_MODULE_DIR" ]; then
    echo "Error: Feature module '$FEATURE_MODULE_DIR' already exists."
    exit 1
fi

# 디렉터리 생성
mkdir -p "$SRC_DIR/contract"
mkdir -p "$SRC_DIR/di"

# 1. build.gradle.kts
cat > "$FEATURE_MODULE_DIR/build.gradle.kts" << EOF
plugins {
    alias(libs.plugins.challengeFeature)
}

android {
    namespace = "$BASE_PACKAGE.feature.$LOWER_FEATURE_NAME"
}
EOF
echo "✓ build.gradle.kts"

# 2. .gitignore
cat > "$FEATURE_MODULE_DIR/.gitignore" << 'EOF'
/build
EOF

# 3. State
cat > "$SRC_DIR/contract/${UPPER_CAMEL_CASE_NAME}State.kt" << EOF
package $BASE_PACKAGE.feature.$LOWER_FEATURE_NAME.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
sealed interface ${UPPER_CAMEL_CASE_NAME}UiState {

    @Immutable
    data object Loading : ${UPPER_CAMEL_CASE_NAME}UiState

    @Immutable
    data class Data(
        val placeholder: Unit = Unit, // TODO: 실제 데이터 필드로 교체하세요.
    ) : ${UPPER_CAMEL_CASE_NAME}UiState
}
EOF
echo "✓ ${UPPER_CAMEL_CASE_NAME}State.kt"

# 4. Effect
cat > "$SRC_DIR/contract/${UPPER_CAMEL_CASE_NAME}Effect.kt" << EOF
package $BASE_PACKAGE.feature.$LOWER_FEATURE_NAME.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
sealed interface ${UPPER_CAMEL_CASE_NAME}ModalEffect {

    @Immutable
    data object Hidden : ${UPPER_CAMEL_CASE_NAME}ModalEffect
}

@Stable
sealed interface ${UPPER_CAMEL_CASE_NAME}UiEffect {

    @Immutable
    data class ShowMessage(val message: String) : ${UPPER_CAMEL_CASE_NAME}UiEffect
}
EOF
echo "✓ ${UPPER_CAMEL_CASE_NAME}Effect.kt"

# 5. ViewModel
cat > "$SRC_DIR/${UPPER_CAMEL_CASE_NAME}ViewModel.kt" << EOF
package $BASE_PACKAGE.feature.$LOWER_FEATURE_NAME

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import $BASE_PACKAGE.feature.$LOWER_FEATURE_NAME.contract.${UPPER_CAMEL_CASE_NAME}UiState
import $BASE_PACKAGE.feature.$LOWER_FEATURE_NAME.contract.${UPPER_CAMEL_CASE_NAME}UiEffect
import $BASE_PACKAGE.feature.$LOWER_FEATURE_NAME.contract.${UPPER_CAMEL_CASE_NAME}ModalEffect
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class ${UPPER_CAMEL_CASE_NAME}ViewModel(
    // TODO: 의존성을 주입하세요.
) : ViewModel() {

    private val _uiState: MutableStateFlow<${UPPER_CAMEL_CASE_NAME}UiState> = MutableStateFlow(${UPPER_CAMEL_CASE_NAME}UiState.Loading)
    val uiState: StateFlow<${UPPER_CAMEL_CASE_NAME}UiState> get() = _uiState

    private val _modalEffect = MutableStateFlow<${UPPER_CAMEL_CASE_NAME}ModalEffect>(${UPPER_CAMEL_CASE_NAME}ModalEffect.Hidden)
    val modalEffect: StateFlow<${UPPER_CAMEL_CASE_NAME}ModalEffect> get() = _modalEffect

    private val _uiEffect = MutableSharedFlow<${UPPER_CAMEL_CASE_NAME}UiEffect>()
    val uiEffect: SharedFlow<${UPPER_CAMEL_CASE_NAME}UiEffect> get() = _uiEffect

    fun dismiss() {
        _modalEffect.update { ${UPPER_CAMEL_CASE_NAME}ModalEffect.Hidden }
    }

    private fun showMessage(message: String) {
        viewModelScope.launch {
            _uiEffect.emit(${UPPER_CAMEL_CASE_NAME}UiEffect.ShowMessage(message))
        }
    }
}
EOF
echo "✓ ${UPPER_CAMEL_CASE_NAME}ViewModel.kt"

# 6. Route (state collect + event 전달) + Content (상태 분기)
cat > "$SRC_DIR/${UPPER_CAMEL_CASE_NAME}Route.kt" << EOF
package $BASE_PACKAGE.feature.$LOWER_FEATURE_NAME

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import $BASE_PACKAGE.feature.$LOWER_FEATURE_NAME.contract.${UPPER_CAMEL_CASE_NAME}UiState
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ${UPPER_CAMEL_CASE_NAME}Route(
    viewModel: ${UPPER_CAMEL_CASE_NAME}ViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ${UPPER_CAMEL_CASE_NAME}Content(
        uiState = uiState,
    )
}

@Composable
private fun ${UPPER_CAMEL_CASE_NAME}Content(
    uiState: ${UPPER_CAMEL_CASE_NAME}UiState,
) {
    when (uiState) {
        is ${UPPER_CAMEL_CASE_NAME}UiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
        is ${UPPER_CAMEL_CASE_NAME}UiState.Data -> {
            ${UPPER_CAMEL_CASE_NAME}Screen(uiState = uiState)
        }
    }
}
EOF
echo "✓ ${UPPER_CAMEL_CASE_NAME}Route.kt"

# 7. Screen (실제 UI + Preview)
cat > "$SRC_DIR/${UPPER_CAMEL_CASE_NAME}Screen.kt" << EOF
package $BASE_PACKAGE.feature.$LOWER_FEATURE_NAME

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import $BASE_PACKAGE.feature.$LOWER_FEATURE_NAME.contract.${UPPER_CAMEL_CASE_NAME}UiState

@Composable
internal fun ${UPPER_CAMEL_CASE_NAME}Screen(
    uiState: ${UPPER_CAMEL_CASE_NAME}UiState.Data,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        // TODO: UI를 구현하세요.
        Text(
            text = "${UPPER_CAMEL_CASE_NAME}",
            style = MaterialTheme.typography.headlineMedium,
        )
    }
}
EOF
echo "✓ ${UPPER_CAMEL_CASE_NAME}Screen.kt"

# 8. Koin Module
cat > "$SRC_DIR/di/${UPPER_CAMEL_CASE_NAME}Module.kt" << EOF
package $BASE_PACKAGE.feature.$LOWER_FEATURE_NAME.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module
@ComponentScan("$BASE_PACKAGE.feature.$LOWER_FEATURE_NAME")
class ${UPPER_CAMEL_CASE_NAME}Module
EOF
echo "✓ ${UPPER_CAMEL_CASE_NAME}Module.kt"

# settings.gradle.kts에 모듈 추가
SETTINGS_GRADLE="$PROJECT_ROOT/settings.gradle.kts"

if [ -f "$SETTINGS_GRADLE" ]; then
    if grep -q ":feature:$LOWER_FEATURE_NAME" "$SETTINGS_GRADLE"; then
        echo "Module ':feature:$LOWER_FEATURE_NAME' already exists in settings.gradle.kts"
    else
        # 마지막 include 문 뒤에 새 모듈 추가
        LAST_INCLUDE_LINE=$(grep -n 'include(":feature:' "$SETTINGS_GRADLE" | tail -1 | cut -d: -f1)
        if [ -n "$LAST_INCLUDE_LINE" ]; then
            sed -i '' "${LAST_INCLUDE_LINE}a\\
include(\":feature:$LOWER_FEATURE_NAME\")" "$SETTINGS_GRADLE"
            echo "✓ Added ':feature:$LOWER_FEATURE_NAME' to settings.gradle.kts"
        else
            echo "include(\":feature:$LOWER_FEATURE_NAME\")" >> "$SETTINGS_GRADLE"
            echo "✓ Appended ':feature:$LOWER_FEATURE_NAME' to settings.gradle.kts"
        fi
    fi
else
    echo "Warning: settings.gradle.kts not found"
fi

echo ""
echo "======================================="
echo "✅ Feature '$UPPER_CAMEL_CASE_NAME' 생성 완료!"
echo "======================================="
echo ""
echo "생성된 디렉토리: $FEATURE_MODULE_DIR"
echo ""
echo "다음 단계:"
echo "  1. Gradle Sync 실행"
echo "  2. Route 정의 추가 (core/navigation/.../Route.kt)"
echo "  3. MainScreen NavDisplay에 화면 등록"
echo "  4. feature:main build.gradle.kts에 의존성 추��"
