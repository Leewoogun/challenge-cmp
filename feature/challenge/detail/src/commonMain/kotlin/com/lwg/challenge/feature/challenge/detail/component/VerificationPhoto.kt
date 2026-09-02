package com.lwg.challenge.feature.challenge.detail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.VerificationStatus
import org.jetbrains.compose.ui.tooling.preview.Preview

private val PhotoShape = RoundedCornerShape(16.dp)

private const val NOT_VERIFIED_TEXT = "아직 인증하지 않았어요"
private const val VERIFICATION_FAILED_TEXT = "인증하지 못했어요"
private const val LOADING_TEXT = "불러오는 중..."
private const val LOAD_FAILED_TEXT = "사진을 불러오지 못했어요"
private const val STATUS_UNKNOWN_TEXT = "인증 현황을 불러오지 못했어요"
private const val PHOTO_DELETED_TEXT = "탈퇴한 사용자의 사진은 삭제됐어요"

/**
 * 인증 사진 영역.
 *
 * 높이를 160dp 로 고정하는 이유: 미인증 → 로딩 → 성공/실패가 한 화면에서 서로 갈아치우는 자리라
 * 상태마다 높이가 다르면 폴링·재조회 때마다 아래 카드들이 위아래로 튄다.
 *
 * @param status 인증 현황 조회에 실패했으면 `null`. 이때 '미인증' 으로 그리면 이미 인증한 사람의
 *   사진을 없는 것처럼 보여주게 되므로, 사진이 아니라 조회 실패를 드러낸다.
 * @param isLoading 인증 현황 자체를 아직 못 받은 구간. 이미지 로딩(Coil)과는 별개다.
 * @param cacheKey [photoUrl] 을 이미지 로더 캐시에서 식별할 키. 사진을 교체해도 [photoUrl] 이
 *   그대로라 URL 만으로는 캐시에서 옛 사진이 그대로 돌아온다. 기본값을 두지 않는 이유는, 새 호출부가
 *   빠뜨렸을 때 예외도 로그도 없이 화면에만 드러나는 실패가 되기 때문이다.
 * @param photoDeleted 탈퇴로 사진이 영구 삭제된 칸. 다시 받을 수 있는 대상이 없어 재시도를 열지 않는다.
 * @param onRetry [status] 가 `null` 일 때만 쓰인다. 사진 URL 재요청([RemotePhoto] 내부)과 달리
 *   현황 재조회는 이 컴포넌트가 스스로 할 수 없어 호출부에서 받는다. 넘기지 않으면 문구만 남는다.
 */
@Composable
internal fun VerificationPhoto(
    status: VerificationStatus?,
    photoUrl: String?,
    cacheKey: String?,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    photoDeleted: Boolean = false,
    onRetry: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(PhotoShape)
            .background(ChallengeTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        when {
            isLoading -> PhotoMessage(LOADING_TEXT)
            status == null -> if (onRetry == null) {
                PhotoMessage(STATUS_UNKNOWN_TEXT)
            } else {
                PhotoRetryMessage(text = STATUS_UNKNOWN_TEXT, onRetry = onRetry)
            }
            status == VerificationStatus.PENDING -> PhotoMessage(NOT_VERIFIED_TEXT)
            status == VerificationStatus.FAILED -> PhotoMessage(VERIFICATION_FAILED_TEXT)
            // 삭제는 되돌아오지 않는 정상 종결이라 로드 실패의 어휘("불러오지 못했어요" · 재시도)를
            // 쓰면 안 된다. 더 중요한 건 바로 아래 분기가 진짜 데이터 이상을 잡는 감지기라는 점이다 —
            // 두 신호를 한 문구로 합치면 URL 유실 버그가 탈퇴자 뒤에 영영 숨는다.
            photoDeleted -> PhotoMessage(PHOTO_DELETED_TEXT)
            // 인증은 됐는데 URL 이 없는 건 정상 경로가 아니다. 빈 칸으로 두면 원인 없는 공백이 되므로
            // 로드 실패와 같은 문구로 드러낸다(재시도할 대상 URL 이 없어 버튼은 붙이지 않는다).
            photoUrl == null -> PhotoMessage(LOAD_FAILED_TEXT)
            else -> RemotePhoto(url = photoUrl, cacheKey = cacheKey)
        }
    }
}

/**
 * 사진 URL 조인이 어긋나 `//` 가 낀 주소는 서버에서 404 로 돌아오는데, 이건 "상대가 아직 인증 안 함"
 * 의 정상 404 와 응답만 봐선 구분되지 않는다. 실패를 조용히 빈 칸으로 삼키면 그 사고가 QA 를 그대로
 * 통과하므로, 로드 실패는 반드시 화면에 문구로 남기고 재시도까지 열어 둔다.
 */
@Composable
private fun RemotePhoto(url: String, cacheKey: String?) {
    val context = LocalPlatformContext.current

    // model 이 새 인스턴스여도 Coil 은 data·캐시 키 등 일부만 비교해 재요청 여부를 정하므로, remember
    // 없이도 리컴포지션만으로 다시 로드되지는 않는다. 그래도 감싸는 이유는 그 비교 대상이 라이브러리
    // 내부 사정이라서다 — 재요청 조건은 여기서 remember 키로 직접 못 박는다.
    val request = remember(context, url, cacheKey) {
        ImageRequest.Builder(context)
            .data(url)
            // 메모리·디스크가 URL 을 각자 키로 들고 있어 한쪽만 갈면 다른 층에서 옛 사진이 돌아온다.
            .memoryCacheKey(cacheKey)
            .diskCacheKey(cacheKey)
            .build()
    }

    // 같은 URL 을 다시 요청하려면 AsyncImagePainter 가 새로 만들어져야 해서 key 로 노드를 갈아 끼운다.
    // 사진이 교체되면(캐시 키 변화) 재시도를 세던 대상 이미지 자체가 달라지므로 tick 도 함께 리셋한다.
    var retryTick by remember(url, cacheKey) { mutableIntStateOf(0) }

    key(retryTick) {
        SubcomposeAsyncImage(
            model = request,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            loading = { PhotoMessage(LOADING_TEXT) },
            error = { PhotoRetryMessage(text = LOAD_FAILED_TEXT, onRetry = { retryTick++ }) },
        )
    }
}

@Composable
private fun PhotoMessage(text: String) {
    Text(
        text = text,
        style = ChallengeTheme.typography.medium14,
        color = ChallengeTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp),
    )
}

@Composable
private fun PhotoRetryMessage(text: String, onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PhotoMessage(text)

        Text(
            text = "다시 시도",
            style = ChallengeTheme.typography.medium12,
            color = ChallengeTheme.colorScheme.primary,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onRetry)
                .padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

/** 인증 완료 + 사진 있음. 프리뷰는 네트워크를 못 타 Coil 의 로딩 슬롯까지만 확인된다. */
@Preview
@Composable
private fun VerificationPhotoWithPhotoPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            VerificationPhoto(
                status = VerificationStatus.VERIFIED,
                photoUrl = "http://10.0.2.2:8080/api/v1/challenges/7/photos/opponent",
                cacheKey = "http://10.0.2.2:8080/api/v1/challenges/7/photos/opponent|2026-01-01T09:30",
            )
        }
    }
}

/** 탈퇴로 사진이 삭제됨 — 아래 [VerificationPhotoMissingUrlPreview] 와 문구가 달라야 한다. */
@Preview
@Composable
private fun VerificationPhotoDeletedPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            VerificationPhoto(
                status = VerificationStatus.VERIFIED,
                photoUrl = null,
                cacheKey = null,
                photoDeleted = true,
            )
        }
    }
}

/** 인증했는데 URL 이 없는 데이터 이상 — 삭제가 아니므로 기존 로드 실패 문구가 그대로 나와야 한다. */
@Preview
@Composable
private fun VerificationPhotoMissingUrlPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            VerificationPhoto(
                status = VerificationStatus.VERIFIED,
                photoUrl = null,
                cacheKey = null,
                photoDeleted = false,
            )
        }
    }
}

@Preview
@Composable
private fun VerificationPhotoNotVerifiedPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            VerificationPhoto(status = VerificationStatus.PENDING, photoUrl = null, cacheKey = null)
        }
    }
}

@Preview
@Composable
private fun VerificationPhotoFailedPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            VerificationPhoto(status = VerificationStatus.FAILED, photoUrl = null, cacheKey = null)
        }
    }
}

@Preview
@Composable
private fun VerificationPhotoLoadingPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            VerificationPhoto(
                status = VerificationStatus.VERIFIED,
                photoUrl = null,
                cacheKey = null,
                isLoading = true,
            )
        }
    }
}

/** 인증 현황 조회 실패 — 사진 대신 현황을 못 받았다고 밝히고 전체 재조회를 연다. */
@Preview
@Composable
private fun VerificationPhotoStatusUnknownPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            VerificationPhoto(status = null, photoUrl = null, cacheKey = null, onRetry = {})
        }
    }
}

/** Coil 의 에러 슬롯은 프리뷰에서 재현할 수 없어 슬롯 내용만 같은 160dp 틀에 얹어 확인한다. */
@Preview
@Composable
private fun VerificationPhotoLoadErrorPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(PhotoShape)
                    .background(ChallengeTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                PhotoRetryMessage(text = LOAD_FAILED_TEXT, onRetry = {})
            }
        }
    }
}
