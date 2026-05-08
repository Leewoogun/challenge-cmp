package com.lwg.challenge.feature.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.LabeledDivider
import com.lwg.challenge.designsystem.components.NormalButton
import com.lwg.challenge.designsystem.components.StatusPillBadge
import com.lwg.challenge.designsystem.theme.BrandColors
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.feature.login.contract.LoginUiState

/**
 * 로그인 화면 UI.
 *
 * 디자인 결정 (2026-05-08, `docs/features/auth-kakao/design.md`):
 * - 다크 베이스 + 상단 글로우 후광 + 하단 fire 블롭
 * - Hero: 스탬프 로고 + "SOUL CONTRACT" 뱃지 + 타이틀("영혼" 부분 fire-gradient) + 서브 카피
 * - CTA: LabeledDivider + 카카오 버튼(텍스트 only) + 약관 풋터
 * - 외곽 회전 링/부유 입자/통계 칩/게스트 모드 outline 버튼은 모두 제거됨
 * - Hero/CTA 진입 시 slide-up 애니메이션 (CTA 0.2s delay)
 *
 * 토큰: `ChallengeTheme.colorScheme.*` / `ChallengeTheme.brushes.*` / `BrandColors.*` 만 사용.
 */
@Composable
internal fun LoginScreen(
    uiState: LoginUiState,
    onLoginClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ChallengeTheme.colorScheme.background),
    ) {
        // 배경: 상단 글로우 + 하단 fire 블롭 (z-bg)
        BackgroundDecor()

        // 컨텐츠 (z-content)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp),
            )

            HeroSection()

            Spacer(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
            )

            CtaSection(
                isLoading = uiState is LoginUiState.Loading,
                onLoginClick = onLoginClick,
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        // 로딩 오버레이 — 결정 사항: 현재 placeholder 유지
        if (uiState is LoginUiState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.30f)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = ChallengeTheme.colorScheme.primary)
            }
        }
    }
}

/**
 * 배경 글로우 + 하단 fire 블롭. Lovable 의 z-index 0 레이어 등가.
 *
 * - 상단 글로우: 500dp 원, opacity 30%, `brushes.glow` (radial)
 * - 하단 fire 블롭: 400×300dp 타원 근사(원으로), opacity 20%, `brushes.fire`
 *
 * Compose에 CSS의 `blur-3xl` 등가를 무료로 얻을 수 없으므로 alpha 만으로 근사.
 * (성능/크로스플랫폼 안정성 우선 — 추후 graphicsLayer blur 검토)
 */
@Composable
private fun BoxScope.BackgroundDecor() {
    val glowBrush = ChallengeTheme.brushes.glow
    val fireBrush = ChallengeTheme.brushes.fire

    // 상단 글로우 (500dp, opacity 30%)
    Box(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .size(500.dp)
            .alpha(0.30f)
            .clip(CircleShape)
            .background(brush = glowBrush),
    )

    // 하단 fire 블롭 (400dp, opacity 20%)
    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .size(400.dp)
            .alpha(0.20f)
            .clip(CircleShape)
            .background(brush = fireBrush),
    )
}

@Composable
private fun HeroSection() {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 400)) +
                slideInVertically(
                    animationSpec = tween(durationMillis = 400),
                    initialOffsetY = { it / 6 },
                ),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            // 스탬프 로고
            SoulStampLogo()

            // 타이틀 묶음 (뱃지 + h1 + 서브카피)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatusPillBadge(text = "SOUL CONTRACT")

                HeroTitle()

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "말만 번지르르한 친구에게 던지는",
                        style = ChallengeTheme.typography.light14,
                        color = ChallengeTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "피할 수 없는 1:1 도전장",
                        style = ChallengeTheme.typography.bold14,
                        color = ChallengeTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

/**
 * "영혼을 걸어라. 🔥" 타이틀.
 *
 * - "영혼" 부분만 `brushes.fire` brush 텍스트
 * - 나머지는 `onBackground`
 * - 🔥 이모지에 wiggle 애니메이션 (좌우 -8° ~ +8°)
 *
 * 동일 줄에 brush 와 단색을 섞기 위해 `AnnotatedString` + `SpanStyle(brush=...)` 사용.
 * 🔥 만 별도 Text 로 분리해야 회전 변환을 적용할 수 있음.
 */
@Composable
private fun HeroTitle() {
    val fireBrush = ChallengeTheme.brushes.fire
    val onBackground = ChallengeTheme.colorScheme.onBackground

    val titleStyle = ChallengeTheme.typography.bold48

    val annotated: AnnotatedString = buildAnnotatedString {
        withStyle(SpanStyle(brush = fireBrush, fontWeight = FontWeight.Bold)) {
            append("영혼")
        }
        withStyle(SpanStyle(color = onBackground, fontWeight = FontWeight.Bold)) {
            append("을 \n걸어라.")
        }
    }

    // wiggle: -8° ~ +8°
    val infinite = rememberInfiniteTransition(label = "title-wiggle")
    val rotation by infinite.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_500),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "wiggle-rotation",
    )

    Box(
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = annotated,
                style = titleStyle,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "🔥", // 🔥
                style = ChallengeTheme.typography.bold30,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .rotate(rotation),
            )
        }
    }
}

@Composable
private fun CtaSection(
    isLoading: Boolean,
    onLoginClick: () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        // CTA 0.2s delay
        kotlinx.coroutines.delay(200)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 400)) +
                slideInVertically(
                    animationSpec = tween(durationMillis = 400),
                    initialOffsetY = { it / 4 },
                ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LabeledDivider(
                text = "한 번 서명하면 무를 수 없음",
                modifier = Modifier.fillMaxWidth(),
            )

            // 카카오 버튼 — 텍스트 only, 16dp radius, 52dp 높이
            NormalButton(
                text = "카카오로 시작하기",
                onClick = onLoginClick,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                containerColor = BrandColors.KakaoYellow,
                contentColor = BrandColors.KakaoLabel,
                shape = RoundedCornerShape(16.dp),
                textStyle = ChallengeTheme.typography.bold16,
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp),
            )

            Spacer(modifier = Modifier.height(4.dp))

            FooterAgreementText()
        }
    }
}

@Composable
private fun FooterAgreementText() {
    val muted = ChallengeTheme.colorScheme.onSurfaceVariant
    val emphasized = ChallengeTheme.colorScheme.onBackground

    val text = buildAnnotatedString {
        withStyle(SpanStyle(color = muted)) {
            append("가입과 동시에 ")
        }
        withStyle(SpanStyle(color = emphasized, fontWeight = FontWeight.Bold)) {
            append("이용약관")
        }
        withStyle(SpanStyle(color = muted)) {
            append("·")
        }
        withStyle(SpanStyle(color = emphasized, fontWeight = FontWeight.Bold)) {
            append("개인정보처리방침")
        }
        withStyle(SpanStyle(color = muted)) {
            append("에 동의하며,\n친구의 도발에 응할 마음의 준비를 합니다.")
        }
    }

    Text(
        text = text,
        style = ChallengeTheme.typography.light12,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}
